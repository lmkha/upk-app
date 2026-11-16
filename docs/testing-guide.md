# Ghi chú học Testing trên Android (Kotlin)

Tổng hợp kiến thức đã học qua việc viết test cho feature `settings` của project UPK.
Phần giải thích bằng tiếng Việt, toàn bộ code bằng tiếng Anh.

---

## 1. Unit test là gì

Unit test là một đoạn code nhỏ **kiểm tra tự động** một hàm hoặc class có chạy đúng như mong đợi không.
Thay vì mở app, bấm nút và nhìn bằng mắt, ta viết code mô tả "đưa vào X thì phải ra Y" rồi để máy chạy và báo xanh (pass) hoặc đỏ (fail).

Đặc điểm:

- Chạy trên JVM của máy tính, không cần emulator. Rất nhanh (mili giây).
- Mỗi test kiểm tra **một hành vi** cụ thể.
- Test phải **độc lập**: không phụ thuộc thứ tự chạy hay kết quả của test khác.
- Một test fail làm cả build fail. Trên CI, code sai sẽ không được merge.

### Cấu trúc AAA

Hầu hết test đều theo khuôn này:

```kotlin
@Test
fun `valueOf returns matching enum for a valid name`() {
    // Arrange: chuẩn bị dữ liệu
    val input = "DARK"

    // Act: gọi hàm cần kiểm tra
    val result = ThemeMode.valueOf(input)

    // Assert: so sánh kết quả thực tế với mong đợi
    assertEquals(ThemeMode.DARK, result)
}
```

---

## 2. JUnit 4 cơ bản

| Thành phần | Ý nghĩa |
| --- | --- |
| `@Test` | Đánh dấu một hàm là test |
| `assertEquals(expected, actual)` | So sánh. **Giá trị mong đợi đứng trước** |
| `assertTrue`, `assertFalse`, `assertNull` | Các assert khác hay dùng |
| `@Test(expected = X::class)` | Test pass khi hàm ném đúng exception X |
| `@Before` | Hàm chạy **trước mỗi** `@Test`, dùng để tạo dữ liệu sạch |
| `@After` | Hàm chạy sau mỗi test, dùng để dọn dẹp |
| `@get:Rule` | Gắn một Rule chạy quanh mỗi test (xem mục 4) |

Kotlin cho phép đặt tên test bằng câu có khoảng trắng trong dấu backtick:

```kotlin
@Test
fun `multiple updates keep the last value`() { ... }
```

Khi test fail, thông báo cho biết chính xác mong đợi gì và nhận được gì:

```
java.lang.AssertionError: expected:<LIGHT> but was:<DARK>
```

---

## 3. Fake: thay thế dependency thật

Vấn đề: `UpdateThemeModeUseCase` cần một `SettingsRepository`, nhưng repository thật đọc DataStore trên Android, không chạy được trên máy tính.

Giải pháp: viết một **Fake**, là class cùng interface nhưng chỉ lưu dữ liệu trong bộ nhớ.

```kotlin
class FakeSettingsRepository : SettingsRepository {
    private val themeMode = MutableStateFlow(ThemeMode.SYSTEM)

    var setThemeModeCallCount = 0
        private set

    override fun getThemeMode(): Flow<ThemeMode> = themeMode

    override suspend fun setThemeMode(mode: ThemeMode) {
        setThemeModeCallCount++
        themeMode.value = mode
    }
    // ...
}
```

Bài học kiến trúc: việc tách interface `SettingsRepository` ở tầng domain chính là để use case không cần biết repository thật hoạt động ra sao. Nhờ đó code **dễ test**.

Fake có thể được trang bị thêm "công tắc" để giả lập tình huống khó tái hiện:

```kotlin
var getThemeModeError: Throwable? = null        // getThemeMode ném lỗi
var setThemeModeError: Throwable? = null        // setThemeMode ném lỗi
var setThemeModeGate: CompletableDeferred<Unit>? = null  // setThemeMode "treo" tới khi test cho phép
```

Một số fake đã viết trong project:

- `FakeSettingsRepository` (domain test, ui test, ui androidTest)
- `FakeSettingsLocalDataSource` (data test)
- `FakeDataStore<T>` (data test), chỉ cần 2 thành phần: `data` và `updateData`

Lưu ý: thư mục `androidTest` không nhìn thấy code trong `test`, nên fake phải chép sang nếu cần.

---

## 4. Test code dùng coroutine

### `runBlocking`

Hàm `suspend` phải gọi trong coroutine. Cách đơn giản nhất:

```kotlin
@Test
fun `update calls repository exactly once`() = runBlocking {
    updateThemeMode(ThemeMode.LIGHT)
    assertEquals(1, fakeRepository.setThemeModeCallCount)
}
```

### `runTest` + `UnconfinedTestDispatcher`

Cách chuẩn khi test code có coroutine, từ thư viện `kotlinx-coroutines-test`:

```kotlin
@Test
fun `updates when stored language changes`() = runTest {
    val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
    val provider = SettingsAppLanguageProviderImpl(localDataSource, snapshot, scope)

    localDataSource.setLanguageTag("ja-JP")

    assertEquals("ja-JP", provider.getLanguageTag())
}
```

`UnconfinedTestDispatcher` làm coroutine **chạy ngay khi launch**, không cần "advance" thủ công, nên có thể assert ngay sau đó.

### `MainDispatcherRule` cho ViewModel

`viewModelScope` chạy trên `Dispatchers.Main`, mà unit test không có main thread của Android. Rule này thay Main bằng test dispatcher trước mỗi test và trả lại sau test:

```kotlin
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

// Trong test class:
@get:Rule
val mainDispatcherRule = MainDispatcherRule()
```

### "Đóng băng" hàm suspend bằng `CompletableDeferred`

Dùng để quan sát trạng thái trung gian như `isUpdatingTheme`:

```kotlin
@Test
fun `isUpdatingTheme is true during update and false afterwards`() = runTest {
    val gate = CompletableDeferred<Unit>()
    repository.setThemeModeGate = gate
    val viewModel = createViewModel()

    viewModel.onIntent(SettingsIntent.ThemeModeSelected(ThemeMode.DARK))
    assertTrue(viewModel.uiState.value.isUpdatingTheme)   // đang treo ở gate

    gate.complete(Unit)                                     // thả gate
    assertFalse(viewModel.uiState.value.isUpdatingTheme)
}
```

---

## 5. Test Flow

| Cần gì | Cách làm |
| --- | --- |
| Lấy giá trị hiện tại | `flow.first()` |
| Lấy toàn bộ giá trị (flow hữu hạn) | `flow.toList()` |
| Tạo flow phát sẵn vài giá trị | `flowOf(a, b, c)` |
| Flow không bao giờ phát | `flow { CompletableDeferred<Unit>().await() }` |
| Flow ném lỗi | `flow { throw error }` |

Mẹo: dùng **interface delegation** (`by`) để tạo nhanh một fake chỉ ghi đè một hàm:

```kotlin
val dataSource = object : SettingsLocalDataSource by localDataSource {
    override fun observeThemeMode(): Flow<ThemeProto> =
        flowOf(ThemeProto.THEME_UNSPECIFIED, ThemeProto.SYSTEM, ThemeProto.DARK, ThemeProto.DARK)
}

val result = SettingsRepositoryImpl(dataSource).getThemeMode().toList()

assertEquals(listOf(ThemeMode.SYSTEM, ThemeMode.DARK), result)  // distinctUntilChanged hoạt động
```

---

## 6. Test theo từng tầng

### Domain (use case)

Đơn giản nhất: use case + fake repository. Kiểm tra giá trị trả về và số lần gọi.

### Data (repository, data source, serializer)

- **Repository**: test mapping hai chiều proto ↔ domain, kể cả các case biên (`THEME_UNSPECIFIED`, `UNRECOGNIZED` đều về `SYSTEM`), và chuẩn hoá dữ liệu (trim, chuỗi rỗng, `"en-us"` thành `"en-US"`).
- **Local data source**: dùng `FakeDataStore`. Kiểm tra ghi theme không làm mất language và ngược lại.
- **Serializer**: ghi rồi đọc lại phải giống nhau, dữ liệu rỗng trả về mặc định, dữ liệu hỏng phải ném `CorruptionException`.

### ViewModel

Tạo ViewModel với **use case thật** và **fake repository**. Tạo trong từng test (không phải `@Before`) khi cần cấu hình fake trước.

Những gì nên test ở ViewModel:

- Trạng thái ban đầu và sau khi load xong.
- State đổi theo dữ liệu từ repository.
- Mỗi intent: gọi đúng dependency, state đổi đúng.
- Các điều kiện bỏ qua (chọn lại theme cũ, đang loading, đang cập nhật).
- Lỗi: message hiển thị, message mặc định khi exception không có message, lỗi cũ bị xoá khi thao tác mới.

---

## 7. Chạy test

```bash
# Một module
./gradlew :feature:settings:domain:testDevDebugUnitTest

# Ép chạy lại dù code không đổi
./gradlew :feature:settings:domain:testDevDebugUnitTest --rerun

# Chỉ một class
./gradlew :feature:settings:domain:testDevDebugUnitTest --tests "*ThemeModeTest"

# Toàn bộ project (mọi module đều có flavor dev / staging / prod, ở đây chạy flavor dev)
./gradlew testDevDebugUnitTest
```

Hành vi mặc định của Gradle cần biết:

- **Chỉ in test fail**, test pass im lặng và chỉ báo `BUILD SUCCESSFUL`.
- **Không chạy lại nếu code không đổi** (`up-to-date`), dùng `--rerun` để ép.
- Báo cáo HTML: `<module>/build/reports/tests/testDevDebugUnitTest/index.html`.
- Báo cáo XML: `<module>/build/test-results/testDevDebugUnitTest/`.

Trong Android Studio: bấm nút ▶ cạnh tên class hoặc tên hàm test.

---

## 8. Code coverage

Chạy bằng "Run with Coverage" trong Android Studio. Cho biết test đã chạy qua bao nhiêu phần trăm code.

| Cột | Đo cái gì |
| --- | --- |
| Class % | Số class có ít nhất một dòng được chạy |
| Method % | Số hàm được gọi ít nhất một lần |
| Line % | Số dòng code được thực thi (con số hay dùng nhất) |
| Branch % | Số nhánh rẽ (`if/else`, `when`) được đi qua. Khắt khe hơn Line |

Cách đọc đúng:

- Coverage trả lời "**chỗ nào chưa có test chạy qua**", không trả lời "test có đúng không". Test không assert vẫn làm coverage tăng.
- Các module 0% có thể chỉ vì lần chạy đó không gồm test của chúng.
- `0/0` hiển thị 100% là do không có gì để đếm (interface, class không có nhánh).
- Có những phần unit test không bao giờ phủ được: Hilt module cần `Context`, Composable cần render. Không nên cố ép lên 100%.
- Mở file nguồn sau khi chạy coverage: lề xanh là đã chạy qua, đỏ là chưa, vàng là nhánh chỉ đi một phía.

Kết quả thực tế của feature settings: `SettingsViewModel` 100% line và branch, `settings.data` 95% line (3 dòng thiếu là Hilt module), `SettingsScreen` 0% vì là Compose.

---

## 9. androidTest (instrumented test)

| | Unit test (`src/test`) | Instrumented test (`src/androidTest`) |
| --- | --- | --- |
| Chạy ở đâu | JVM trên máy tính | Emulator hoặc thiết bị thật |
| Android framework | Không có | Đầy đủ |
| Tốc độ | Mili giây | Vài giây đến vài phút, phải cài APK |
| Dùng cho | Logic thuần: use case, repository, ViewModel | Compose UI, DataStore ghi file thật, Room, navigation |
| Lệnh Gradle | `testDevDebugUnitTest` | `connectedDevDebugAndroidTest` |

Nguyên tắc: thứ gì unit test được thì unit test. Chỉ đẩy xuống androidTest những gì bắt buộc cần Android.

### Compose UI test

Dependency: `androidTestImplementation(libs.androidx.ui.test.junit4)` và `debugImplementation(libs.androidx.ui.test.manifest)`.

```kotlin
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()   // import androidx.compose.ui.test.junit4.v2.createComposeRule

    @Test
    fun clickingAnOptionUpdatesRepositoryAndSelection() {
        composeRule.setContent { SettingsScreen(viewModel = createViewModel()) }

        composeRule.onNodeWithText("Dark").performClick()

        composeRule.onNodeWithText("Dark").assertIsSelected()
        composeRule.runOnIdle { assertEquals(1, repository.setThemeModeCallCount) }
    }
}
```

Các API hay dùng:

- Tìm node: `onNodeWithText("...")`, `onNode(matcher)`.
- Hành động: `performClick()`.
- Kiểm tra: `assertIsDisplayed()`, `assertDoesNotExist()`, `assertIsSelected()`, `assertIsEnabled()`, `assertIsNotEnabled()`.
- `runOnIdle { }` chờ UI ổn định rồi mới chạy assert trên dữ liệu ngoài UI.
- Tìm vòng xoay loading: `SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo.Indeterminate)`.

Lưu ý: dùng bản `v2` của `createComposeRule`, bản cũ đã deprecated. Màn hình được render với ViewModel thật và fake repository, không cần Hilt.

### Test DataStore thật

Dùng `DataStoreFactory.create` với một file tạm duy nhất cho mỗi test, xoá trong `@After`. Điều fake không kiểm chứng được mà test này làm được: **dữ liệu còn sau khi đóng rồi mở lại DataStore**.

Lưu ý: trước khi mở lại cùng file, phải `scope.cancel()` rồi `scope.coroutineContext.job.join()` để DataStore cũ đóng hẳn, nếu không sẽ bị lỗi hai instance cùng mở.

Chạy:

```bash
./gradlew :feature:settings:ui:connectedDevDebugAndroidTest :feature:settings:data:connectedDevDebugAndroidTest
```

---

## 10. Sơ đồ file test trong project

```
feature/settings/
├── domain/src/test/.../settings/domain/
│   ├── FakeSettingsRepository.kt
│   ├── ThemeModeTest.kt
│   └── usecases/
│       ├── GetThemeModeUseCaseTest.kt
│       └── UpdateThemeModeUseCaseTest.kt
├── data/
│   ├── src/test/.../settings/data/
│   │   ├── FakeDataStore.kt
│   │   ├── language/
│   │   │   ├── LanguageSnapshotTest.kt
│   │   │   └── SettingsAppLanguageProviderImplTest.kt
│   │   ├── local/datasource/
│   │   │   ├── FakeSettingsLocalDataSource.kt
│   │   │   └── SettingsLocalDataSourceImplTest.kt
│   │   ├── local/datastore/AppSettingsPreferencesSerializerTest.kt
│   │   └── repository/SettingsRepositoryImplTest.kt
│   └── src/androidTest/.../settings/data/
│       └── SettingsDataStoreInstrumentedTest.kt
└── ui/
    ├── src/test/.../settings/ui/
    │   ├── MainDispatcherRule.kt
    │   ├── FakeSettingsRepository.kt
    │   └── SettingsViewModelTest.kt
    └── src/androidTest/.../settings/ui/
        ├── FakeSettingsRepository.kt
        └── SettingsScreenTest.kt
```

Dependency đã thêm vào `gradle/libs.versions.toml`: `kotlinx-coroutines-test`.

---

## 11. Quy ước

- Tên test, comment, chuỗi trong code: **tiếng Anh**.
- Tên test mô tả hành vi, đọc như một câu: `` `selecting the current theme does not call repository` ``.
- Mỗi test một hành vi. Nhiều assert trong một test là bình thường nếu cùng kiểm tra một hành vi.
- Fake đặt cạnh test dùng nó. Fake dùng chung thì tách file riêng.
- Không test interface, data class thuần, hay Hilt module.

---

## 12. Bước tiếp theo

- **MockK**: tạo fake tự động thay vì viết tay, hữu ích khi interface lớn.
- **Turbine**: test Flow phát nhiều giá trị theo thời gian gọn hơn `toList()`.
- **Robolectric**: chạy Compose UI test trên JVM, không cần emulator.
- Viết test cho các feature khác: `auth`, `payment`, `home`.
