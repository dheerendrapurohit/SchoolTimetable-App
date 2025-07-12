#  School Timetable Management System (Spring Boot + JSON + Excel)

This project is a full-stack backend system for generating, managing, and exporting school timetables. It uses **Spring Boot** for business logic, stores data in **JSON files**, and exports weekly timetables to **Excel using Apache POI**.

---

## 🔧 Tech Stack

- **Java 17**
- **Spring Boot**
- **Apache POI** (for Excel generation)
- **JSON-based Repositories** (no SQL database)
- **REST APIs (GET, POST, PUT, DELETE)**

---

---

## ✅ Features

### 🧾 CRUD Operations

Supports full **GET, POST, PUT, DELETE** operations on:

- **Classrooms**
- **Subjects**
- **Teachers**
- **Periods**

---

### 📅 Timetable Generation

- Weekly timetable generation per class
- Generate timetable for a **custom date range**
- Export weekly timetable to Excel using **Apache POI**
- Timetable is stored in `timetable_entries.json` and saved weekly to Excel (e.g., `timetable_2025-07-07.xlsx`)

---

### 👨‍🏫 Teacher Absence Handling

- Mark **full-day** or **half-day** teacher absences
- Automatically reassigns periods to other **available and capable** teachers
- Records stored in:
  - `teacher_absences.json`
  - `teacher_halfday_leaves.json`

---

### 🧠 Timetable Rules & Constraints

- ✅ Each class gets:
  - **Only 1 Yoga period per week**
  - **Max 3 unique subjects per day**
  - **Max 3 periods per teacher per day**
- ✅ Day-wise period count:
  - **Monday to Friday**: 7 periods
  - **Saturday**: 4 periods
- ✅ Timing:
  - Morning: 4 periods (45 minutes each)
  - Afternoon: 3 periods (40 minutes each)
- ✅ Ensures all periods are assigned

---

### 📤 Export to Excel

- Weekly timetables are saved to Excel using Apache POI
- File stored in `exports/` with filenames like:  


---

## 🚀 Running the Project

### Prerequisites:
- Java 17+
- Maven

### Steps:

```bash
# Clone the repository
git clone https://github.com/dheerendrapurohit/SchoolTimetable-App.git
cd SchoolTimetable-App

# Run the application
./mvnw spring-boot:run
