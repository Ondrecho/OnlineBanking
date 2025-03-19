# Online Banking REST API

### Code Quality Checked by SonarCloud
[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=Ondrecho_OnlineBanking)](https://sonarcloud.io/summary/new_code?id=Ondrecho_OnlineBanking)

## Description
The **Online Banking REST API** is a server-side application for online banking, built using **Spring Boot**. This API allows users to manage their accounts, perform transactions, view transaction history, and check currency exchange rates. Administrators can manage users and their accounts.

The project is designed with scalability, security, and maintainability in mind. It follows best practices for RESTful API design and includes features like JWT authentication, role-based access control, and comprehensive logging.

---

## Features

### Users
- **Registration and Authentication** via JWT (planned: OAuth2 support).
- **Account Management**: Create, view, and manage multiple bank accounts.
- **Transactions**: Transfer funds between accounts.
- **Transaction History**: View a detailed log of all transactions.
- **Currency Exchange Rates**: Check real-time currency exchange rates.
- **Personal Dashboard**: View and update personal information.

### Administrators
- **User Management**: Create, update, and delete users.
- **Account Management**: Manage bank accounts (create, close, reopen).
- **Audit Logs**: Monitor user activities and system events.

### Planned Features
- **OAuth2 Authentication**: Integration with third-party identity providers (e.g., Google, GitHub).
- **Two-Factor Authentication (2FA)**: Enhanced security for user accounts.
- **Notifications**: Email and SMS notifications for transactions and account updates.
- **Reporting**: Generate reports for transactions, users, and accounts.

---

## Technology Stack
- **Java 17**: The core programming language.
- **Spring Boot**: Framework for building the application (Spring Web, Spring Security, Spring Data JPA).
- **PostgreSQL**: Relational database for storing data.
- **AOP (Aspect-Oriented Programming)**: For logging and auditing.
- **JWT (JSON Web Tokens)**: For secure authentication and authorization.
- **Swagger/OpenAPI**: For API documentation and testing.
- **Maven**: Build automation and dependency management.
- **SonarCloud**: For code quality and security analysis.

---

## Security
- **JWT Authentication**: Secures API endpoints and controls access.
- **Role-Based Access Control (RBAC)**: Differentiates between `Admin` and `User` roles.
- **Password Encryption**: Uses BCrypt for secure password hashing.
- **Logging and Auditing**: Tracks user actions and system events using AOP.
- **Input Validation**: Ensures all user inputs are validated to prevent injection attacks.

---

## Installation and Setup

### Prerequisites
- **Java 17+**: Ensure Java is installed on your system.
- **PostgreSQL**: Install and configure a PostgreSQL database.
- **Maven**: Required for building the project.

### Steps
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/Ondrecho/OnlineBanking.git
   cd OnlineBanking
   ```

2. **Configure the Database**:
    - Update the `application.properties` file with your PostgreSQL credentials:
      ```properties
      spring.datasource.url=jdbc:postgresql://localhost:5432/online_banking
      spring.datasource.username=your_username
      spring.datasource.password=your_password
      ```

3. **Build the Project**:
   ```bash
   mvn clean install
   ```

4. **Run the Application**:
   ```bash
   mvn spring-boot:run
   ```

5. **Access the API**:
    - The API will be available at `http://localhost:8080/api`.
    - Swagger UI can be accessed at `http://localhost:8080/swagger-ui.html`.

---

## API Documentation (Swagger)
The API is fully documented using **Swagger/OpenAPI**. You can explore the API endpoints, request/response models, and test the API directly from the Swagger UI.

### Access Swagger UI
- **URL**: `http://localhost:8080/swagger-ui.html`
- **Features**:
    - Interactive API documentation.
    - Try-out functionality for testing endpoints.
    - Detailed descriptions of request/response models.

---

### Schema Diagram
<img src="https://github.com/Ondrecho/OnlineBanking/blob/main/pics/diagram.png" alt="Database Schema Diagram" width="400" height="440" />

---
## Development
If you'd like to contribute to the project, follow these steps:
1. **Fork the Repository**.
2. **Create a New Branch** for your changes.
3. **Commit Your Changes** with clear and descriptive messages.
4. **Submit a Pull Request** for review.

---

## License
This project is licensed under the **MIT License**.

---

## Contact
For any questions or feedback, feel free to reach out:
- **Email**: [ondreikoshevchenko@yandex.by]