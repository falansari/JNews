# 🗞 JNews
Java Spring Boot newsletter campaign management app. Get subscribers to sign up to your newsletter,
manage your newsletter publications, and e-mail them to your subscribers. Utilizes multithreading and concurrency techniques
for efficient mailing and subscriber management operations. Completely self-contained and self-hostable app, no third party
dependency management needed.

The mailing feature can be used even from local only server, however the un/subscription endpoints need to be live for organic sign-ups.
You can otherwise import a list of subscriber e-mails and message those instead.

---

## 📖 JNews API Endpoints

### Authentication
- **Security Scheme**: Bearer Token (`Authorization: Bearer <token>`)

| Category         | Method | Endpoint                       | Description                                                         | Example Request Body / Params                                                                                  |
|------------------|--------|--------------------------------|---------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| **Users**        | POST   | `/auth/users/register`         | Register new user                                                   | `{ "email": "user@example.com", "password": "123" }`                                                           |
|                  | POST   | `/auth/users/login`            | Login user                                                          | `{ "email": "user@example.com", "password": "123" }`                                                           |
|                  | DELETE | `/auth/users/{userId}`         | Soft delete user by ID. Admin only.                                 | Path param: `userId=1`                                                                                         |
|                  | POST   | `/auth/users/change-password`  | Change logged-in user’s password                                    | `{ "oldPassword": "abc", "newPassword": "xyz", "confirmNewPassword": "xyz" }`                                  |
|                  | POST   | `/auth/users/forgot-password`  | Send reset password token to verified email                         | `{ "email": "user@example.com" }`                                                                              |
|                  | POST   | `/auth/users/reset`            | Reset user’s password                                               | `{ "token": "uuid", "password": "newPass", "confirmPassword": "newPass" }`                                     |
|                  | POST   | `/auth/users/register/default` | Register system default admin. First account in new app only.       | `{ "email": "admin@example.com", "password": "@dm!nP@$$" }`                                                    |
|                  | POST   | `/auth/users/register/admin`   | Register admin user (requires logged-in admin)                      | `{ "email": "admin2@example.com", "password": "123" }`                                                         |
| **Verification** | POST   | `/auth/users/verify`           | Verify user’s email via token                                       | Query param: `token=uuid`                                                                                      |
|                  | POST   | `/auth/users/token`            | Reissue verification token                                          | Query param: `token=uuid`                                                                                      |
| **Profile**      | GET    | `/profile`                     | Get logged-in user’s profile                                        | –                                                                                                              |
|                  | POST   | `/profile`                     | Create logged-in user's profile                                     | `{ "firstName": "Ahmed", "lastName": "Ali" }`                                                                  |
|                  | PATCH  | `/profile`                     | Update logged-in user's profile                                     | `{ "firstName": "Ahmed", "lastName": "Alali" }`                                                                |
|                  | GET    | `/profile/photo`               | Download logged-in user's profile photo                             | –                                                                                                              |
|                  | POST   | `/profile/photo`               | Upload logged-in user's profile photo (PNG/JPEG)                    | FormData: `file=@photo.png`                                                                                    |
|                  | DELETE | `/profile/photo`               | Delete logged-in user's profile photo (reset to placeholder)        | –                                                                                                              |
| **Newsletters**  | GET    | `/newsletters/{id}`            | Get newsletter by ID                                                | Path param: `id=6`                                                                                             |
|                  | GET    | `/newsletters`                 | Get newsletter by title                                             | Query param: `title=Spring Update`                                                                             |
|                  | GET    | `/newsletters/list`            | Get all newsletters (async)                                         | –                                                                                                              |
|                  | GET    | `/newsletters/{id}/html`       | Download newsletter HTML body                                       | Path param: `id=6`                                                                                             |
|                  | GET    | `/newsletters/{id}/text`       | Download newsletter plain text body                                 | Path param: `id=6`                                                                                             |
|                  | POST   | `/newsletters/add`             | Create newsletter (multipart form data)                             | FormData: `title=Spring`, `subject="Studio Update"`, `body_html=@newsletter.html`, `body_text=@newsletter.txt` |
|                  | PATCH  | `/newsletters/{id}`            | Update newsletter by ID (multipart form data)                       | FormData: `title=NewTitle`, `subject="Updated Subject"`, `body_html=@updated.html`, `body_text=@updated.txt`   |
|                  | DELETE | `/newsletters/{id}`            | Hard delete newsletter (irrecoverable)                              | Path param: `id=6`                                                                                             |
| **Subscribers**  | GET    | `/subscribers/{id}`            | Get subscriber by ID                                                | Path param: `id=150`                                                                                           |
|                  | GET    | `/subscribers`                 | Get subscriber by email                                             | Query param: `email=user@example.com`                                                                          |
|                  | POST   | `/subscribers/subscribe`       | Create new subscriber. No authentication required.                  | `{ "email": "user@example.com", "name": "Optional", "status": "SUBSCRIBED" }`                                  |
|                  | PATCH  | `/subscribers/{id}`            | Update subscriber info                                              | `{ "email": "new@example.com", "name": "Updated Name" }`                                                       |
|                  | PATCH  | `/subscribers/subscribe`       | Subscribe existing unsubscribed member. No authentication required. | Query param: `email=user@example.com`                                                                          |
|                  | PATCH  | `/subscribers/unsubscribe`     | Unsubscribe existing subscribed member. No authentication required. | Query param: `email=user@example.com`                                                                          |
|                  | DELETE | `/subscribers/{id}`            | Hard delete subscriber                                              | Path param: `id=150`                                                                                           |
|                  | POST   | `/subscribers/add`             | Create subscribers from CSV/plain-text file                         | FormData: `file=@subscribers.csv`                                                                              |
|                  | GET    | `/subscribers/list`            | Get all subscribers (async)                                         | –                                                                                                              |
|                  | GET    | `/subscribers/list/{status}`   | Get subscribers filtered by status                                  | Path param: `status=SUBSCRIBED`                                                                                |
|                  | GET    | `/subscribers/export`          | Download subscribers CSV file (async)                               | –                                                                                                              |
|                  | POST   | `/subscribers/import`          | Import subscribers from CSV/plain-text file                         | FormData: `file=@subscribers.csv`                                                                              |
|                  | DELETE | `/subscribers/delete`          | Delete all subscribers (async)                                      | –                                                                                                              |
|                  | DELETE | `/subscribers/delete/{status}` | Delete subscribers by status (async)                                | Path param: `status=UNSUBSCRIBED`                                                                              |
| **Emails**       | POST   | `/emails/send`                 | Send newsletter email to subscribers filtered by status             | Query params: `newsletterId=6`, `subscriberStatus=SUBSCRIBED`                                                  |

### Notes
- All endpoints require **Bearer token authentication** unless otherwise specified.
- Use `application/json` for request bodies.
- Responses are not detailed in this spec — adapt based on your implementation.

---
