import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DataRetriever {

    private final Map<String, String> idToName = new HashMap<>();
    private final Map<String, String> nameToId = new HashMap<>();
    private final Map<String, String> employeeToManager = new HashMap<>();
    private final Map<String, Map<String, Integer>> leaveBalances = new HashMap<>();
    private final Map<String, Map<String, String>> employeeInfo = new HashMap<>();
    private final Map<String, String> leaveRequestStatus = new HashMap<>();

    public DataRetriever() {
        loadEmployees();
        loadLeaveBalances();
        loadLeaveRequests();
    }

    // === LOADERS ===
    private void loadEmployees() {
        loadCSV("data/employees.csv", line -> {
            String[] p = line.split("[,\t]");
            if (p.length < 7) return;

            String id = p[0].trim();
            String name = p[1].trim().toLowerCase();
            idToName.put(id, name);
            nameToId.put(name, id);

            Map<String, String> info = new HashMap<>();
            info.put("email", safeGet(p, 2));
            info.put("jabatan", safeGet(p, 3));
            info.put("departemen", safeGet(p, 4));
            info.put("id_manajer", safeGet(p, 5));
            info.put("tanggal_bergabung", safeGet(p, 6));
            info.put("status", p.length > 7 ? p[7].trim() : "Aktif");
            employeeInfo.put(name, info);
        });

        // Convert manager IDs → names
        for (String emp : employeeInfo.keySet()) {
            String managerId = employeeInfo.get(emp).get("id_manajer");
            String managerName = idToName.getOrDefault(managerId, "Tidak diketahui");
            employeeToManager.put(emp, managerName);
        }
    }

    private void loadLeaveBalances() {
        loadCSV("data/leave_balances.csv", line -> {
            String[] p = line.split("[,\t]");
            if (p.length < 3) return;

            String empId = p[0].trim();
            String type = p[1].trim().toLowerCase();
            int days = parseIntSafe(p[2]);
            String name = idToName.get(empId);
            if (name != null)
                leaveBalances.computeIfAbsent(name, k -> new HashMap<>()).put(type, days);
        });
    }

    private void loadLeaveRequests() {
        leaveRequestStatus.clear();
        String path = resolvePath("data/leave_requests.csv");
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 6) continue;

                String empId = parts[1].trim();
                String status = parts[5].trim();

                String empName = idToName.getOrDefault(empId, "").toLowerCase();
                if (!empName.isEmpty()) {
                    leaveRequestStatus.put(empName, status);
                } else {
                }
            }
        } catch (IOException e) {
            System.out.println("rror loading leave_requests.csv: " + e.getMessage());
        }
    }


    // === RETRIEVAL ===
    public String getManager(String name) {
        name = normalize(name);
        for (String emp : employeeToManager.keySet())
            if (emp.contains(name))
                return "Manajer " + capitalize(emp) + " adalah " + capitalize(employeeToManager.get(emp)) + ".";
        return "Tidak ditemukan data manajer untuk " + capitalize(name) + ".";
    }

    public String getLeaveBalance(String name) {
        name = normalize(name);
        Map<String, Integer> balances = findEmployeeLeave(name);
        if (balances == null)
            return "Data cuti untuk " + capitalize(name) + " tidak ditemukan.";

        StringBuilder sb = new StringBuilder("Sisa cuti " + capitalize(name) + ":\n");
        for (Map.Entry<String, Integer> e : balances.entrySet())
            sb.append("- Cuti ").append(capitalize(e.getKey()))
              .append(": ").append(e.getValue()).append(" hari\n");
        return sb.toString();
    }

    public String getLeaveByType(String name, String type) {
        name = normalize(name);
        type = type.toLowerCase();

        Map<String, Integer> balances = findEmployeeLeave(name);
        if (balances == null)
            return "Karyawan dengan nama " + capitalize(name) + " tidak ditemukan.";

        Integer days = balances.get(type);
        return (days != null)
                ? "Sisa cuti " + capitalize(type) + " " + capitalize(name) + " adalah " + days + " hari."
                : "Data cuti " + capitalize(type) + " untuk " + capitalize(name) + " tidak ditemukan.";
    }

    public String getEmployeeInfo(String name) {
        name = normalize(name);
        for (String emp : employeeInfo.keySet()) {
            if (emp.contains(name)) {
                Map<String, String> info = employeeInfo.get(emp);
                return String.format("""
                        Informasi Karyawan:
                        Nama: %s
                        Jabatan: %s
                        Departemen: %s
                        Email: %s
                        Status: %s
                        Tanggal Bergabung: %s
                        Manajer: %s
                        """,
                        capitalize(emp),
                        info.getOrDefault("jabatan", "-"),
                        info.getOrDefault("departemen", "-"),
                        info.getOrDefault("email", "-"),
                        info.getOrDefault("status", "-"),
                        info.getOrDefault("tanggal_bergabung", "-"),
                        capitalize(employeeToManager.getOrDefault(emp, "Tidak diketahui"))
                );
            }
        }
        return "Karyawan dengan nama " + capitalize(name) + " tidak ditemukan.";
    }

    public String getLeaveRequestStatus(String name) {
        name = normalize(name);
        for (String emp : leaveRequestStatus.keySet()) {
            if (emp.contains(name)) {
                String status = leaveRequestStatus.get(emp);
                return "Status pengajuan cuti terakhir untuk " + capitalize(emp) + " adalah: " + status + ".";
            }
        }
        return "Tidak ada data pengajuan cuti untuk " + capitalize(name) + ".";
    }


    public List<String> getAllEmployees() {
        List<String> names = new ArrayList<>(employeeInfo.keySet());
        names.sort(String::compareTo);
        return names;
    }

    // === HELPERS ===
    private void loadCSV(String relativePath, CSVLineProcessor processor) {
        String path = resolvePath(relativePath);
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null)
                processor.process(line);
        } catch (IOException e) {
            System.out.println("⚠️ Error loading " + relativePath + ": " + e.getMessage());
        }
    }

    private String normalize(String str) {
        return str == null ? "" : str.toLowerCase().trim();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "-";
        String[] words = str.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words)
            if (!w.isEmpty())
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1).toLowerCase()).append(" ");
        return sb.toString().trim();
    }

    private String safeGet(String[] arr, int idx) {
        return idx < arr.length ? arr[idx].trim() : "";
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return 0; }
    }

    private Map<String, Integer> findEmployeeLeave(String name) {
        for (String emp : leaveBalances.keySet())
            if (emp.contains(name)) return leaveBalances.get(emp);
        return null;
    }

    private String resolvePath(String relativePath) {
        Path base = Paths.get(System.getProperty("user.dir"));
        return base.resolve(relativePath).toString();
    }

    public String findEmployeeIdByPartialName(String partialName) {
        if (partialName == null || partialName.isEmpty()) return "-";
        String lower = partialName.toLowerCase().trim();

        // Cari partial match
        for (Map.Entry<String, String> entry : nameToId.entrySet()) {
            String nameKey = entry.getKey().toLowerCase();
            if (nameKey.contains(lower)) {
                return entry.getValue();
            }
        }
        return "-";
    }

    public void refreshLeaveRequests() {
        leaveRequestStatus.clear();
        loadLeaveRequests();
    }

    @FunctionalInterface
    private interface CSVLineProcessor {
        void process(String line);
    }
}