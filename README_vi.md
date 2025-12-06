# Nền tảng học tiếng Anh - Backend API

Backend RESTful API toàn diện cho nền tảng học tiếng Anh trực tuyến, được xây dựng với Spring Boot 3.5.5 và Java 21.

## Tổng quan

Nền tảng này cung cấp giải pháp hoàn chỉnh cho việc học tiếng Anh trực tuyến, bao gồm quản lý khóa học, đánh giá kỹ năng Nói và Viết bằng AI, hệ thống bài kiểm tra, tính năng thương mại điện tử và cộng đồng.

## Tính năng

### Quản lý Học tập
- **Hệ thống khóa học**: Tạo và quản lý khóa học với modules, bài học và nội dung đa phương tiện
- **Hệ thống quiz**: Nhiều loại bài kiểm tra với sections, câu hỏi và tùy chọn cấu hình
- **Đánh giá AI**: Chấm điểm tự động cho bài nói (lưu loát, phát âm, ngữ pháp, từ vựng) và bài viết (đáp ứng yêu cầu, mạch lạc, ngữ pháp, từ vựng)
- **Theo dõi tiến độ**: Quản lý đăng ký với theo dõi chi tiết tiến độ bài học và hoàn thành
- **Kế hoạch học tập**: Lịch học cá nhân hóa với tích hợp Google Calendar

### Người dùng & Giảng viên
- **Quản lý người dùng**: Đăng ký, xác thực (JWT & OAuth2/Google), xác minh email
- **Phân quyền theo vai trò**: Admin, Giảng viên và Học viên
- **Tính năng giảng viên**: Quản lý hồ sơ, hệ thống ví, theo dõi thu nhập, yêu cầu rút tiền, quản lý tài khoản ngân hàng

### Thương mại điện tử
- **Giỏ hàng**: Thêm khóa học vào giỏ trước khi mua
- **Quản lý Đơn hàng**: Chu trình đơn hàng hoàn chỉnh với hỗ trợ nhiều phương thức thanh toán
- **Cổng thanh toán**: PayOS (VND Việt Nam) và PayPal (USD Quốc tế)
- **Tạo hóa đơn**: Tự động tạo hóa đơn PDF cho đơn hàng hoàn thành
- **Phí nền tảng**: Cấu hình phần trăm phí nền tảng cho thu nhập giảng viên

### Cộng đồng
- **Hệ thống Blog**: Tạo và quản lý bài viết với danh mục và bình luận
- **Diễn đàn Thảo luận**: Danh mục diễn đàn, chủ đề, bài đăng với hệ thống kiểm duyệt và báo cáo
- **Đánh giá Khóa học**: Học viên có thể đánh giá và nhận xét khóa học đã đăng ký

### Thông báo
- **Push Notifications**: Tích hợp Firebase Cloud Messaging (FCM)
- **Thông báo Email**: Hệ thống email SMTP cho xác minh và cập nhật

## Công nghệ dử dụng

| Danh mục | Công nghệ |
|----------|-----------|
| Framework | Spring Boot 3.5.5 |
| Ngôn ngữ | Java 21 |
| Cơ sở dữ liệu | PostgreSQL |
| Cache | Redis |
| Bảo mật | Spring Security, JWT, OAuth2 |
| Lưu trữ | AWS S3 Compatible |
| Thanh toán | PayOS, PayPal |
| Thông báo | Firebase FCM, SMTP |
| API Docs | SpringDoc OpenAPI |
| Tích hợp AI | N8N Webhooks |
| Lịch | Google Calendar API |

## Cấu trúc dự án

```
src/main/java/com/english/api/
├── admin/          # Dashboard admin & tổng quan
├── assessment/     # Đánh giá AI cho SW + chấm điểm LR
├── auth/           # Xác thực & phân quyền
├── blog/           # Bài viết, danh mục, bình luận
├── cart/           # Quản lý giỏ hàng
├── common/         # Tiện ích chung & xử lý media
├── course/         # Khóa học, modules, bài học, đánh giá
├── enrollment/     # Đăng ký, tiến độ, kế hoạch học tập
├── forum/          # Chủ đề diễn đàn, bài đăng, báo cáo
├── mail/           # Dịch vụ email
├── notification/   # Thông báo đẩy
├── order/          # Đơn hàng, thanh toán, hóa đơn
├── quiz/           # Loại quiz, sections, câu hỏi
└── user/           # Người dùng, giảng viên
```

## Bắt đầu

### Yêu cầu
- Java 21+
- PostgreSQL 14+
- Redis 6+
- Maven 3.8+

### Biến môi trường

Tạo file `.env` hoặc cấu hình các biến môi trường sau:

```bash
# Cơ sở dữ liệu
DB_HOST=localhost
DB_PORT=5432
DB_NAME=english_db
DB_USERNAME=postgres
DB_PASSWORD=your_password
DB_SSL_MODE=disable

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_USERNAME=
REDIS_PASSWORD=
REDIS_SSL_ENABLED=false

# JWT
JWT_SECRET_KEY=your_jwt_secret
JWT_ACCESSTOKEN_EXP=3600
JWT_REFRESHTOKEN_EXP=604800

# OAuth2 (Google)
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# S3 Storage
S3_ENDPOINT=https://your-s3-endpoint
S3_ACCESS_KEY=your_access_key
S3_SECRET_KEY=your_secret_key
S3_BUCKET=your_bucket
S3_PUBLIC_URL=https://your-public-url

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email
MAIL_PASSWORD=your_app_password

# Thanh toán - PayOS (Việt Nam)
PAYOS_CLIENT_ID=your_client_id
PAYOS_API_KEY=your_api_key
PAYOS_CHECKSUM_KEY=your_checksum_key

# Thanh toán - PayPal
PAYPAL_CLIENT_ID=your_client_id
PAYPAL_CLIENT_SECRET=your_secret

# Firebase
# Đặt file fcm-service-account.json vào src/main/resources/

# Xử lý AI (N8N)
N8N_SPEAKING_WEBHOOK_URL=your_webhook_url
N8N_WRITING_WEBHOOK_URL=your_webhook_url
N8N_CALLBACK_SECRET=your_secret
```

### Build & Chạy

```bash
# Build dự án
./mvnw clean package -DskipTests

# Chạy ứng dụng
./mvnw spring-boot:run

# Hoặc chạy trực tiếp file JAR
java -jar target/api-0.0.1-SNAPSHOT.jar
```

API sẽ khả dụng tại `http://localhost:8080`

## Tài liệu API

Khi được bật, Swagger UI có thể truy cập tại `/swagger-ui.html`

---

*Để xem phiên bản tiếng Anh của README này, xem [README.md](README.md)*
