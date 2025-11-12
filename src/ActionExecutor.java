import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ActionExecutor {
    private final DataRetriever retriever = new DataRetriever();
    private final HRFunctions hr = new RealHRFunctions();

    public String execute(String intent, Map<String, String> entities) {
        switch (intent) {
            case "get_manager":
                return retriever.getManager(entities.getOrDefault("name", ""));
            case "get_leave_balance":
                if (entities.containsKey("type") && !entities.get("type").isEmpty())
                    return retriever.getLeaveByType(entities.get("name"), entities.get("type"));
                return retriever.getLeaveBalance(entities.get("name"));
            case "get_employee_info":
                return retriever.getEmployeeInfo(entities.getOrDefault("name", ""));
            case "apply_leave":
                return handleApplyLeave(entities);
            case "check_leave_status":
                return hr.checkLeaveRequestStatus(capitalize(entities.getOrDefault("name", "")));
            case "schedule_review":
                return handleScheduleReview(entities);
            case "submit_expense":
                return handleSubmitExpense(entities);
            case "lookup_colleague":
                return hr.lookupColleagueInfo(capitalize(entities.getOrDefault("name", "")));
            case "help":
                return showHelp();
            default:
                return "Maaf, aku belum tahu cara menangani permintaan itu.";
        }
    }

    // === APPLY LEAVE ===
    private String handleApplyLeave(Map<String, String> entities) {
        String name = entities.getOrDefault("name", "");
        String type = entities.getOrDefault("type", "tahunan");
        String start = entities.getOrDefault("start_date", "");
        String end = entities.getOrDefault("end_date", "");

        if (name.isEmpty()) return "Mohon sebutkan nama karyawan yang ingin mengajukan cuti.";

        try {
            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);
            return hr.applyForLeave(capitalize(name), capitalize(type), startDate, endDate);
        } catch (DateTimeParseException e) {
            return "Format tanggal tidak valid. Gunakan format YYYY-MM-DD.";
        }
    }

    // === SCHEDULE REVIEW ===
    private String handleScheduleReview(Map<String, String> entities) {
        String name = entities.getOrDefault("name", "");
        String reviewer = entities.getOrDefault("reviewer", "Manajer");
        String date = entities.getOrDefault("date", "");

        if (name.isEmpty()) return "Mohon sebutkan nama karyawan untuk review performa.";

        try {
            LocalDate reviewDate = LocalDate.parse(date);
            return hr.schedulePerformanceReview(capitalize(name), capitalize(reviewer), reviewDate);
        } catch (DateTimeParseException e) {
            return "Format tanggal tidak valid. Gunakan format YYYY-MM-DD.";
        }
    }

    // === SUBMIT EXPENSE ===
    private String handleSubmitExpense(Map<String, String> entities) {
        String name = entities.getOrDefault("name", "");
        String category = entities.getOrDefault("category", "lainnya");
        double amount;

        try {
            amount = Double.parseDouble(entities.getOrDefault("amount", "0"));
        } catch (NumberFormatException e) {
            return "Nominal pengeluaran tidak valid.";
        }

        return hr.submitExpenseReport(capitalize(name), category, amount);
    }

    // === HELP ===
    private String showHelp() {
        return """
        *Panduan Perintah HR Agent:*
        - siapa manajer <nama>
        - sisa cuti <nama>
        - sisa cuti tahunan <nama>
        - info <nama>
        - ajukan cuti <nama> dari <tanggal> sampai <tanggal>
        - cek status cuti <nama>
        - jadwalkan review performa <nama> dengan <reviewer> pada <tanggal>
        - ajukan expense <nama> <kategori> <nominal>
        - cari info rekan <nama>
        - exit → keluar dari aplikasi
        """;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        String[] words = str.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words)
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1).toLowerCase()).append(" ");
        return sb.toString().trim();
    }
}
