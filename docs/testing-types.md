# Các loại test phổ biến và ai thực hiện

Tài liệu tham khảo đi kèm `testing-guide.md`. Một test có thể thuộc nhiều nhóm cùng lúc, ví dụ một unit test vừa là functional test vừa là automated test.

---

## 1. Theo phạm vi (kim tự tháp test)

Càng lên cao càng gần người dùng thật, nhưng càng chậm, càng dễ flaky và càng tốn công bảo trì. Số lượng test nên giảm dần từ đáy lên đỉnh.

| Loại | Kiểm tra gì | Ai viết | Trong project UPK |
| --- | --- | --- | --- |
| Unit test | Một class hoặc hàm, dependency thay bằng fake | Developer, cùng lúc với code | 60 test trong `src/test` |
| Integration test | Vài thành phần thật ghép nhau: repository + DataStore, screen + ViewModel, API client + server giả | Developer | `SettingsDataStoreInstrumentedTest`, `SettingsScreenTest` |
| Contract test | Client và server có đồng ý với nhau về format API không, chạy ở cả hai phía | Developer backend và mobile | Chưa có, hợp với `RemoteDataSource` |
| End-to-end (E2E) / UI test | Cả app như người dùng, qua nhiều màn hình, có thể chạm backend thật | QA automation hoặc developer, tùy team | Chưa có |
| System test | Toàn hệ thống đã deploy, gồm app, backend, hạ tầng | QA | Chưa có |

---

## 2. Theo mục đích

| Loại | Trả lời câu hỏi | Ai thực hiện |
| --- | --- | --- |
| Functional | Tính năng làm đúng yêu cầu không | Developer (tầng thấp), QA (tầng cao) |
| Regression | Sửa chỗ này có làm hỏng chỗ khác không. Thực chất là chạy lại toàn bộ test đã có | CI tự chạy, QA chạy bộ regression thủ công trước release |
| Smoke test | Bản build có chạy được tối thiểu không: mở app, đăng nhập, màn chính hiện | QA hoặc CI, ngay sau mỗi build |
| Sanity test | Sau một fix nhỏ, đúng chỗ đó hoạt động chưa | QA |
| Acceptance test (UAT) | Khách hàng hoặc product owner chấp nhận không | Product owner, khách hàng, business analyst |
| Exploratory | Tìm lỗi không có trong kịch bản bằng cách dùng app tự do | QA, đôi khi cả team |
| Usability | Người dùng thật có hiểu và dùng được không | UX researcher, với người dùng thật |

---

## 3. Phi chức năng (non-functional)

| Loại | Kiểm tra gì | Ai thực hiện |
| --- | --- | --- |
| Performance / Load / Stress | Tốc độ, độ trễ, chịu được bao nhiêu người dùng, vỡ ở đâu | Performance engineer, backend developer |
| Security / Penetration | Lỗ hổng bảo mật, tấn công thử | Security engineer, pentester bên ngoài |
| Accessibility | Người khuyết tật dùng được không: TalkBack, độ tương phản, kích thước chạm | QA, developer, có tool tự động như Accessibility Scanner |
| Compatibility | Chạy đúng trên các máy, phiên bản OS, kích thước màn hình | QA, device farm như Firebase Test Lab |
| Localization | Bản dịch, định dạng ngày và tiền, chữ dài làm vỡ layout | QA, người bản xứ |

---

## 4. Theo cách thực hiện

| Loại | Đặc điểm | Ai |
| --- | --- | --- |
| Manual | Người bấm tay theo test case viết sẵn | QA / tester |
| Automated | Code hoặc script tự chạy | Developer cho unit và integration, QA automation cho E2E |
| Static testing | Không chạy code: code review, lint, detekt, phân tích tĩnh | Developer, tool trên CI |
| Snapshot / Screenshot test | Chụp UI và so với ảnh chuẩn, phát hiện thay đổi giao diện ngoài ý muốn | Developer, với Paparazzi hoặc Roborazzi trên Android |
| Property-based / Fuzz test | Sinh ngẫu nhiên hàng nghìn input để tìm case lạ | Developer, security engineer |
| A/B test | Hai phiên bản tính năng, đo người dùng thật phản ứng ra sao | Product, data analyst |
| Canary / Beta | Phát hành cho một nhóm nhỏ trước | Release manager, người dùng beta |

---

## 5. Theo giai đoạn phát hành

- **Alpha**: nội bộ team dùng thử.
- **Beta**: người dùng ngoài tình nguyện, qua Google Play internal/closed testing.
- **Production monitoring**: không hẳn là test, nhưng crash report (Crashlytics) và analytics là tầng phát hiện lỗi cuối cùng.

---

## 6. Ai làm gì, tóm lại

| Vai trò | Loại test phụ trách |
| --- | --- |
| Developer | Unit, integration, snapshot, contract, static. Viết cùng lúc với code, chạy trên CI |
| QA / Tester | Manual, regression, smoke, exploratory, compatibility, localization. Viết test case từ yêu cầu nghiệp vụ |
| QA Automation / SDET | E2E tự động với Appium, Maestro, Espresso, Playwright. Bảo trì framework test |
| Chuyên gia riêng | Performance, security, accessibility, UX research |
| Product / khách hàng | Acceptance, A/B, beta |

Ranh giới giữa các vai mờ dần ở team nhỏ, developer thường kiêm cả E2E.

---

## 7. Thứ tự đầu tư cho project UPK

1. Unit và integration test: đã có cho feature `settings`, mở rộng sang `auth`, `payment`, `home`.
2. CI (GitHub Actions) chạy `./gradlew testDebugUnitTest` mỗi lần mở PR, chặn merge khi fail.
3. Screenshot test cho Compose bằng Roborazzi hoặc Paparazzi.
4. E2E cho vài luồng chính (mở app, đổi theme, thanh toán) khi app đã ổn định.

Thuật ngữ "automation testing" khi nói về vị trí công việc thường chỉ tầng E2E do QA automation làm. Unit và integration test cũng là test tự động, nhưng do developer viết cùng code.
