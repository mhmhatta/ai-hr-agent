# Lawencon AI HR Agent 🤖
A lightweight HR assistant powered by Java, CSV-based data management, and LLM-like prompt handling.

## 🚀 Features
- Query employee info, leave balance, manager hierarchy
- Submit and check leave requests (CSV persistent)
- Schedule performance reviews and record results
- Handle simple expense submissions

## 🧩 Tech Stack
- Java 19
- CSV file system
- Maven build system
- Gemini / LLM integration endpoint (for intent classification)

## 💡 Example Queries
- siapa manajer rina
- ajukan cuti sakit untuk budi dari 2025-09-15 sampai 2025-09-17
- jadwalkan review performa untuk rina dengan santi pada 2025-12-20

## 🗂 Data Format
All records stored in `/data/*.csv`:
- employees.csv
- leave_balances.csv
- leave_requests.csv
- performance_reviews.csv