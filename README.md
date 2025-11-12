# AI-Powered HR Agent
A simple **AI-powered HR assistant** built using Java and Gemini API integration.  
This project simulates how an intelligent HR chatbot can process natural language queries and connect them to structured enterprise data.

---

## ⚙️ Project Overview
This project was designed to simulate how an **AI-powered HR assistant** could operate inside a real company environment — capable of handling HR queries through natural language and connecting them with structured data sources.

From the beginning, the system was designed with **realism, modularity, and clarity** in mind.  
Instead of returning static or hardcoded answers, each interaction (like applying for leave or checking performance schedules) is connected to **actual HR data stored in CSV files** — just like a lightweight company database.

The system flow works like this:
1. **User interacts** via CLI using natural language (Bahasa Indonesia supported).
2. The **Gemini API** classifies the intent (e.g., `get_manager`, `apply_leave`, `schedule_review`).
3. The **ActionExecutor** maps that intent to the appropriate HR logic.
4. The **DataRetriever** handles structured data from CSV files.
5. The **RealHRFunctions** layer simulates real HR processes — writing to `leave_requests.csv` or `performance_reviews.csv`, just like an actual HR information system.

This layered approach makes the system both **data-driven** and **AI-aware**, demonstrating how large language models (LLMs) can be integrated with structured data sources in a production-like workflow.

---

## Features

| Feature | Description |
|----------|--------------|
| **Manager Lookup** | Find an employee’s manager based on employee data. |
|**Leave Balance Check** | Retrieve remaining leave days (by type or total). |
|**Apply for Leave** | Record leave requests directly into `leave_requests.csv`. |
|**Leave Request Status** | Check approval status of the latest leave request. |
|**Expense Submission** | Submit simulated expense reports with category and amount. |
|**Colleague Lookup** | Retrieve colleague job titles and emails from `employees.csv`. |
|**Performance Review Scheduler** | Schedule performance reviews and log them into `performance_reviews.csv`. |

---

## System Architecture

| Component | Description |
|------------|-------------|
| `AgentApp.java` | Main CLI entry point, handles user input/output. |
| `IntentClassifierGemini.java` | Connects to Gemini API to classify intents. |
| `ActionExecutor.java` | Executes specific HR actions based on classified intent. |
| `DataRetriever.java` | Handles CSV reading and data mapping for employees and leaves. |
| `RealHRFunctions.java` | Writes new records (leave requests, reviews) to CSV files. |
| `HRFunctions.java` | Defines the abstract interface for HR function contracts. |

---

## Data Structure

| File | Description |
|------|--------------|
| `employees.csv` | Master employee data (ID, name, department, manager). |
| `leave_balances.csv` | Tracks leave quotas for each employee. |
| `leave_requests.csv` | Logs leave applications with status. |
| `performance_reviews.csv` | Records scheduled performance reviews. |

---

## 🚀 How to Run

Make sure you have:
- **Java JDK 19+**
- **Apache Maven 3.9+**
- A valid **Gemini API Key** from [Google AI Studio](https://makersuite.google.com/app/apikey)

Check installations:
```bash
java -version
mvn -version
```

- Set your Gemini API key as an environment variable:
```bash
setx GEMINI_API_KEY "your_api_key_here"
```

- Build and Run
```bash
mvn clean compile exec:java
```

### Example Commands
```
- siapa manajer rina  
- sisa cuti tahunan budi  
- ajukan cuti sakit untuk rina dari 2025-10-03 sampai 2025-10-05  
- cek status cuti rina  
- jadwalkan review performa untuk budi dengan andre pada 2025-12-01  
- ajukan expense rina kategori akomodasi sebesar 500000  
- cari info rekan budi  
- exit
```

### Example Output
```
====================================================
Welcome to Lawencon AI HR Agent
====================================================
Coba tanya sesuatu seperti:
- siapa manajer rina
- sisa cuti tahunan budi
- tolong ajukan cuti sakit buat rina dari 3 okt sampai 5 okt
Ketik 'help' untuk melihat semua perintah, atau 'exit' untuk keluar.

You: siapa manajer rina
Manajer Rina Wijaya adalah Santi Putri.

You: ajukan cuti sakit untuk budi dari 2025-09-15 sampai 2025-09-17
KONFIRMASI: Pengajuan cuti untuk Budi (jenis: Sakit) dari tanggal 15 September 2025 hingga 17 September 2025 telah dicatat.
```