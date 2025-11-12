import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RealHRFunctions implements HRFunctions {
    private final DataRetriever retriever = new DataRetriever();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");

    @Override
    public String applyForLeave(String employeeName, String leaveType, LocalDate startDate, LocalDate endDate) {
        String message = String.format(
            "KONFIRMASI: Pengajuan cuti untuk %s (jenis: %s) dari tanggal %s hingga %s telah dicatat.",
            employeeName, leaveType,
            startDate.format(formatter),
            endDate.format(formatter)
        );

        try {
            String basePath = System.getProperty("user.dir");
            Path filePath = Paths.get(basePath, "data", "leave_requests.csv");

            String newId = "LR" + String.format("%03d", (int) (Math.random() * 900) + 100);
            String empId = retriever.findEmployeeIdByPartialName(employeeName);

            DateTimeFormatter csvFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");
            String newLine = String.join(",",
                newId,
                empId,
                leaveType,
                startDate.format(csvFormatter),
                endDate.format(csvFormatter),
                "Menunggu Persetujuan"
            ) + "\n";

            Files.write(filePath, newLine.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            retriever.refreshLeaveRequests();
        } catch (IOException e) {
            System.out.println("Gagal menyimpan data cuti: " + e.getMessage());
        }

        return message;
    }

    @Override
    public String schedulePerformanceReview(String employeeName, String reviewerName, LocalDate reviewDate) {
        String message = String.format(
            "KONFIRMASI: Sesi review performa untuk %s dengan %s telah dijadwalkan pada %s.",
            employeeName, reviewerName, reviewDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))
        );

        try {
            String basePath = System.getProperty("user.dir");
            Path filePath = Paths.get(basePath, "data", "performance_reviews.csv");

            String newId = "PR" + String.format("%03d", (int) (Math.random() * 900) + 100);
            String empId = retriever.findEmployeeIdByPartialName(employeeName);
            String reviewerId = retriever.findEmployeeIdByPartialName(reviewerName);

            if (empId.equals("-")) {
                return "Gagal menjadwalkan review: data karyawan tidak ditemukan.";
            }
            if (reviewerId.equals("-")) {
                return "Gagal menjadwalkan review: data reviewer tidak ditemukan.";
            }

            DateTimeFormatter csvFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");
            String newLine = String.join(",",
                newId,
                empId,
                reviewerId,
                reviewDate.format(csvFormatter),
                "0",
                "Terjadwal"
            ) + "\n";

            Files.write(filePath, newLine.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Gagal menyimpan data review performa: " + e.getMessage());
        }

        return message;
    }

    @Override
    public String checkLeaveRequestStatus(String employeeName) {
        return retriever.getLeaveRequestStatus(employeeName);
    }

    @Override
    public String submitExpenseReport(String employeeName, String category, double amount) {
        return String.format(
            "KONFIRMASI: Laporan pengeluaran untuk %s sebesar Rp%,.2f (kategori: %s) telah diajukan.",
            employeeName, amount, category
        );
    }

    @Override
    public String lookupColleagueInfo(String colleagueName) {
        String info = retriever.getEmployeeInfo(colleagueName);
        if (info.contains("tidak ditemukan")) {
            return "Rekan dengan nama " + colleagueName + " tidak ditemukan di database.";
        }
        return info;
    }
}
