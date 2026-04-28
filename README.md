# Asia Medical Centre (AMC) Management System

## Overview
The **Asia Medical Centre (AMC)** is a Java-based desktop application designed to manage the daily operations of a medical clinic. Built using Java and Swing, the system features a robust, role-based Graphical User Interface (GUI) that streamlines tasks for various clinic personnel, from administrative staff to doctors and patients.

## 🚀 Features
- **Role-Based Access Control (RBAC)**: A secure login system that directs users to customized dashboards based on their role.
- **Five Multi-Tier User Roles**:
  - **Super Manager**: High-level administrative access and oversight.
  - **Manager**: Oversees general clinic operations and generates reports.
  - **Doctor**: Manages schedules, views patient records, and handles medical appointments.
  - **Staff**: Handles daily operations including payments, reception duties, and scheduling.
  - **Customer (Patient)**: Books appointments, views personal medical history, and provides feedback.
- **Appointment Management**: Seamless scheduling, updating, and tracking of medical appointments.
- **Payment Processing**: Handling and tracking of medical bills and patient payments.
- **Feedback System**: Allows customers to submit feedback on their clinic experience.
- **Reporting**: Automated generation and viewing of clinic operational reports.
- **File-Based Storage**: Utilizes flat `.txt` files for lightweight, persistent data storage (e.g., users, appointments, feedback, payments).

## 🛠️ Tech Stack
- **Language**: Java
- **GUI Framework**: Java Swing (Designed with NetBeans GUI Builder `.form` files)
- **Data Storage**: Text-file based (`.txt`)
- **Build Tool**: Ant (`build.xml`)
- **IDE Recommendation**: Apache NetBeans

## 🗂️ Project Structure
- `src/amc/`: Contains the main GUI classes and forms (e.g., `LoginPage`, `DoctorMenu`, `CustomerMenu`).
- `src/model/`: Core Java Domain Models (e.g., `User`, `Appointment`, `Payment`, `Feedback`).
- `src/service/`: Business logic and file handling services (`UserService`, `AppointmentService`, etc.).
- `src/data/`: Text files acting as the database for the application.
- `src/util/` & `src/validation/`: Utility classes for input validation and helpers.
- `build/` & `nbproject/`: NetBeans and Ant build configuration files.

## ⚙️ Getting Started

### Prerequisites
- **Java Development Kit (JDK)**: Version 8 or higher.
- **NetBeans IDE**: Highly recommended for viewing and editing the Swing `.form` designs natively.

### Installation & Setup
1. Clone the repository to your local machine:
   ```bash
   git clone <your-repository-url>
   ```
2. Open the project in NetBeans IDE:
   - Go to `File` -> `Open Project` and select the `AMC` folder.

> [!WARNING]
> **Hardcoded File Paths:** 
> The current project setup includes hardcoded absolute file paths for data storage (e.g., `C:\Users\ASUS\Desktop\AMC\src\data\...`). Before running the project, you **must** update these paths in the `service` classes (like `UserService.java`, `AppointmentService.java`, etc.) to point to your local machine's directory, or ideally, refactor them to use relative paths (e.g., `./src/data/`).

### Running the Application
1. Ensure you have resolved the file path issues mentioned above.
2. Build the project using the "Clean and Build" tool in NetBeans.
3. Run `LoginPage.java` (located in the `amc` package) to launch the AMC system.
4. Default login credentials for different roles can be found by inspecting the `.txt` files located in the `src/data/` directory.