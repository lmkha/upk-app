# Review kiến trúc UPK Android

Ngày review: 2026-11-26, tại commit `f3e5ab7`. Review bằng cách đọc code, chưa chạy build hay test.

Cách đọc:

- ⬜ là chưa làm, ✅ là đã xong. Làm xong mục nào thì thay ⬜ của mục đó bằng ✅.
- Các mục xếp theo mức ưu tiên giảm dần, từ P1 đến P4. Trong mỗi mức, **Fix** đứng trước **Refactor**.
- **Fix**: hành vi đang sai hoặc có rủi ro bảo mật, cần sửa.
- **Refactor**: hành vi vẫn đúng, nhưng cấu trúc cản trở việc mở rộng hoặc gây nhầm lẫn.

## P1 - Sửa ngay (lỗi hành vi và bảo mật)

- ⬜ **Fix - Mất mạng lúc refresh token làm người dùng bị logout.**
  [TokenProviderImpl.refreshToken()](../feature/auth/data/src/main/java/com/example/upk/auth/data/token/TokenProviderImpl.kt) trả `null` cho mọi lỗi, kể cả `IOException`. [TokenAuthenticator](../core/data/src/main/java/com/example/upk/core/data/remote/interceptor/TokenAuthenticator.kt) gặp `null` là gọi `clearTokens()`.
  Hướng sửa: contract của `TokenProvider.refreshToken()` phải phân biệt "server từ chối refresh token" (xoá token) với "lỗi mạng" (ném `IOException`, giữ token).

- ⬜ **Fix - Decode JSON chạy trên Main thread.**
  API trả `Response<ResponseBody>` nên Retrofit không parse trên thread nền. `decodeFromString` trong [RemoteDataSource.executeForResponse](../core/data/src/main/java/com/example/upk/core/data/remote/RemoteDataSource.kt) chạy trên dispatcher của caller, tức là Main khi gọi từ `viewModelScope`.
  Hướng sửa: thêm qualifier cho dispatcher ở `:core:common` (hiện chỉ có `Dispatchers.IO` hardcode trong `CoreCommonModule`), inject dispatcher vào `RemoteDataSource` và bọc phần decode (cả nhánh thành công lẫn `parseError`) bằng `withContext`. Decode là việc CPU trên body đã nằm trong bộ nhớ, nên dùng `Dispatchers.Default`; lời gọi Retrofit `suspend` tự nó đã main-safe, không cần bọc.
  Lưu ý: `withContext` đặt trong `mapCatching` sẽ nuốt `CancellationException`. Viết lại đoạn đó bằng `try/catch` + `ensureActive()` như `executeRaw`.

- ⬜ **Fix - Access/refresh token lưu plaintext và bị đưa vào backup.**
  Token nằm trong Proto DataStore `auth_preferences.pb`, trong khi `allowBackup="true"` và [data_extraction_rules.xml](../app/src/main/res/xml/data_extraction_rules.xml), [backup_rules.xml](../app/src/main/res/xml/backup_rules.xml) còn là file mẫu.
  Hướng sửa: tối thiểu exclude `auth_preferences.pb` khỏi cloud backup và device transfer. Tốt hơn: mã hoá bằng Keystore/Tink.

- ⬜ **Fix - `dev/` hardcode trong đường dẫn API.**
  `BASE_PATH = "dev/auth/api/Auth"` trong [AuthApi](../feature/auth/data/src/main/java/com/example/upk/auth/data/remote/api/AuthApi.kt) và `"dev/payment/api/Payment"` trong [PaymentApi](../feature/payment/data/src/main/java/com/example/upk/payment/data/api/PaymentApi.kt). Build staging và prod vẫn gọi đường dẫn `dev`.
  Hướng sửa: phần phụ thuộc môi trường thuộc về `API_URL` trong `secrets.properties`.

- ⬜ **Fix - `userId` và email cá nhân hardcode trong source.**
  Giá trị mặc định của [CreateBindingRequest](../feature/payment/data/src/main/java/com/example/upk/payment/data/dto/CreateBindingRequest.kt). Xoá khỏi code; lưu ý chúng vẫn còn trong git history.

- ⬜ **Fix - Không có tín hiệu "phiên hết hạn" ra ngoài tầng network.**
  Sau `clearTokens()` UI không biết để đưa người dùng về màn hình đăng nhập. `AuthState` trong `feature/auth/domain` đã có nhưng chưa được dùng.
  Hướng sửa: `auth` expose một `Flow<AuthState>`; `app` quan sát và điều hướng.

## P2 - Nên làm sớm (tổ chức `:core:data` và mô hình lỗi)

- ⬜ **Fix - `PaymentApi` dùng `@UnauthenticatedRetrofit`.**
  [ApiModule](../feature/payment/data/src/main/java/com/example/upk/payment/data/di/ApiModule.kt): request thanh toán đi mà không có `Authorization`. Đổi sang `@AuthenticatedRetrofit`.

- ⬜ **Fix - `NonJsonFormatException` chứa nguyên error body trong message.**
  [NonJsonFormatException](../core/common/src/main/java/com/example/upk/core/common/errors/NonJsonFormatException.kt): body có thể lọt vào log hoặc Crashlytics, trái với rule bảo mật. Giữ body ở một property riêng, không đưa vào `message`.

- ⬜ **Fix - `LocaleManager` gắn `@RequiresApi(33)` trong khi `minSdk = 29`.**
  [AppModule](../app/src/main/java/com/example/upk/di/AppModule.kt): inject ở thiết bị API 29-32 sẽ crash. Hiện chưa ai inject nên chưa lộ.

- ⬜ **Fix - `OkHttpClient` chưa cấu hình timeout.**
  [NetworkModule](../core/data/src/main/java/com/example/upk/core/data/remote/NetworkModule.kt) đang dùng mặc định của OkHttp. Đặt connect/read/write timeout rõ ràng.

- ⬜ **Refactor - Đổi `:core:data` thành `:core:network`.**
  Module chỉ chứa network stack. Tên `data` gợi ý repository và DataStore dùng chung, những thứ không có ở đây.

- ⬜ **Refactor - Xoá class base rỗng.**
  [BaseRepository](../core/data/src/main/java/com/example/upk/core/data/repository/BaseRepository.kt) và [BaseLocalDataSource](../core/data/src/main/java/com/example/upk/core/data/local/BaseLocalDataSource.kt) không có thành viên nào. Kế thừa "để dành" chỉ thêm ràng buộc.

- ⬜ **Refactor - Gỡ dependency và plugin không dùng trong `:core:data`.**
  [core/data/build.gradle.kts](../core/data/build.gradle.kts): plugin `upkProtobuf`, `datastore`, `datastore-preferences`, `protobuf-kotlin-lite`, `appcompat`, `material`, `hilt-navigation-compose`.

- ⬜ **Refactor - Dùng `api` cho những gì module lộ ra ngoài.**
  API công khai của `:core:data` lộ `retrofit2.Response`, `okhttp3.Headers`, `Json` và các kiểu của `:core:common`, nhưng đều khai báo `implementation`. Vì vậy mỗi feature data phải khai báo lại Retrofit, OkHttp, serialization và `:core:common`.

- ⬜ **Refactor - Dùng chung một `OkHttpClient` gốc.**
  Hai client đang dựng độc lập, tức là hai connection pool và hai dispatcher. Dựng client gốc (language interceptor, timeout), rồi `newBuilder()` để thêm auth. Gộp luôn đoạn dựng `Retrofit` đang lặp hai lần.

- ⬜ **Refactor - Mô hình lỗi ở tầng domain.**
  `Result` mang `Throwable` thô đi xuyên domain lên UI: [AddPaymentBindingViewModel](../feature/payment/ui/src/main/java/com/example/upk/payment/ui/AddPaymentBindingViewModel.kt) phải kiểm tra `ApiException` và `IOException`.
  Hướng sửa: một sealed error ở `:core:domain` (ví dụ lỗi mạng, lỗi API kèm `code` + `fieldErrors`, lỗi không xác định), map tại biên data.

- ⬜ **Refactor - Chuyển kiểu wire format ra khỏi `:core:common`.**
  `ApiResponseBody` và `FieldError` là `@Serializable` của envelope JSON, đang nằm ở module mà tầng ui phụ thuộc. Đưa về module network; làm cùng mục mô hình lỗi ở trên.

- ⬜ **Refactor - Dọn `:core:common`.**
  Module đang gom `Json`, `ApplicationScope`, envelope network và exception. [build.gradle.kts](../core/common/build.gradle.kts) kéo theo `appcompat`, `material`, `hilt-navigation-compose` và Retrofit converter mà không cần.

## P3 - Khả năng mở rộng (làm trước khi thêm feature tiếp theo)

- ⬜ **Refactor - Convention plugin theo layer.**
  `build-logic` hiện chỉ chia sẻ SDK version. Mỗi module lặp 30-40 dòng, và cả 16 module đều khai báo `appcompat` + `material` (View), kể cả domain.
  Hướng sửa: thêm `upk.hilt`, `upk.android.library.compose`, và plugin theo layer (`upk.feature.ui`, `upk.feature.data`, `upk.feature.domain`).

- ⬜ **Refactor - Domain module thành Kotlin JVM thuần.**
  Rule ghi "pure Kotlin", nhưng domain là Android library có Hilt + KSP, nên compiler không chặn được `android.*`. Module JVM chỉ cần `javax.inject` và coroutines, build nhanh hơn và rule được cưỡng chế.

- ⬜ **Refactor - Class impl ở feature data thành `internal`.**
  `SettingsRepositoryImpl`, `SettingsLocalDataSourceImpl`, `AuthLocalDataSourceImpl`, `TokenProviderImpl`, `PaymentRepositoryImpl`... đang `public`, nên `app` tham chiếu trực tiếp được. Làm như `:core:analytics`: impl và Hilt module đều `internal`.

- ⬜ **Refactor - Chuyển theme về `:core:ui`.**
  `:core:ui` rỗng, theme nằm ở `app/ui/theme`. Hệ quả: preview trong [SettingsScreen](../feature/settings/ui/src/main/java/com/example/upk/settings/ui/SettingsScreen.kt) phải dùng `MaterialTheme` thay vì `UPKTheme`.

- ⬜ **Refactor - Quyết định số phận `:core:domain`.**
  Module rỗng (chỉ có test mẫu). Hoặc xoá, hoặc dùng làm nơi đặt mô hình lỗi ở P2.

- ⬜ **Refactor - Event analytics do từng feature tự định nghĩa.**
  [AppAnalyticsEvent](../core/analytics/src/main/java/com/example/upk/core/analytics/AppAnalyticsEvent.kt) gom event của mọi feature vào core, nên mỗi event mới phải sửa core và build lại mọi module phụ thuộc. `AnalyticsEvent` đã là interface mở: để `ThemeChanged` nằm ở `feature/settings/ui`.

- ⬜ **Refactor - Navigation do feature tự export.**
  [Destination](../app/src/main/java/com/example/upk/ui/navigation/Destination.kt) và [NavigationAnalytics](../app/src/main/java/com/example/upk/ui/navigation/NavigationAnalytics.kt) nằm ở `app`, nên mỗi màn hình mới phải sửa 3 file ở đó. Để từng feature ui export route và hàm `NavGraphBuilder.xxxScreen()`.

- ⬜ **Refactor - Chia sẻ test fake.**
  `FakeSettingsRepository` có 4 bản, `FakeAnalyticsTracker` 3 bản, `MainDispatcherRule` 2 bản. Các bản sẽ lệch nhau khi interface đổi. Dùng `:core:testing` hoặc `testFixtures`.
  Lưu ý: việc này ngược với quy ước hiện tại trong `AGENTS.md` ("each module keeps its own copy"), nên phải cập nhật rule cùng lúc.

- ⬜ **Refactor - Cân nhắc lại `execute<T>` dạng `inline reified`.**
  Interface Retrofit trả `Response<ResponseBody>` nên không ghi kiểu response; truyền sai `T` vẫn compile và chỉ lỗi lúc chạy. Code cũng được inline vào từng call site. Phương án thay thế: `CallAdapter.Factory` trả kiểu kết quả có type. Không gấp; chỉ đáng làm khi số endpoint tăng nhiều.

## P4 - Dọn dẹp

- ⬜ **Fix - `:feature:auth:data` được include hai lần** trong [settings.gradle.kts](../settings.gradle.kts).

- ⬜ **Refactor - Xoá provider không dùng trong `AppModule`.**
  `NavHostController` dạng singleton từ application context là anti-pattern (và `App` đã dùng `rememberNavController()`). `NotificationManager` và `LocaleManager` cũng chưa ai inject.

- ⬜ **Refactor - `TokenProviderImpl` đi vòng qua local data source.**
  `clearTokens()` ghi thẳng vào `DataStore` trong khi `AuthLocalDataSource.clearAll()` đã có. Bỏ `DataStore` khỏi constructor.

- ⬜ **Refactor - `AuthInterceptor` dùng `addHeader`.**
  Đổi sang `header("Authorization", ...)` để không bao giờ có hai header trùng.

- ⬜ **Refactor - Gỡ dependency thừa ở các module khác.**
  [app/build.gradle.kts](../app/build.gradle.kts) khai báo Retrofit, OkHttp, Stripe nhưng không import chúng. `feature/settings/ui` phụ thuộc `:core:crashlytics` mà không dùng. `buildConfig = true` ở `feature/settings/domain` và `feature/settings/data`, và plugin `upk.secrets` ở `feature/payment/data`, đều không có code nào đọc `BuildConfig`.

- ⬜ **Refactor - `CrashReporter` chưa được inject ở đâu.**
  Hoặc dùng (ví dụ ghi non-fatal trong ViewModel, tầng data), hoặc ghi chú rõ là để dành.

- ⬜ **Refactor - Xoá 28 file `ExampleUnitTest` / `ExampleInstrumentedTest`** sinh từ template.

- ⬜ **Refactor - Nối `feature:payment` vào `app` hoặc ghi chú là đang dở.**
  `app` chưa phụ thuộc module payment nào, nên các binding Hilt của nó chưa từng được kiểm tra trong graph thật.

## Giữ nguyên (đang làm đúng)

- Hướng phụ thuộc `ui -> domain <- data` được giữ đúng; không core module nào phụ thuộc ngược vào feature.
- `TokenProvider` và `AppLanguageProvider` khai báo ở core, feature implement: dependency inversion đúng cách.
- `AuthApi` dùng `@UnauthenticatedRetrofit`, tránh vòng phụ thuộc với `TokenAuthenticator`.
- `RemoteDataSource` xử lý cancellation đúng (`ensureActive()`) và có test đầy đủ.
- `:core:analytics` và `:core:crashlytics`: contract công khai, impl và Hilt module `internal`.
- `feature/settings` là feature mẫu sạch cho cả ba layer.
