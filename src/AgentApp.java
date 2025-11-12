import java.util.Scanner;

public class AgentApp {
    public static void main(String[] args) {
        IntentClassifierGemini clf = new IntentClassifierGemini();
        ActionExecutor executor = new ActionExecutor();
        Scanner sc = new Scanner(System.in);

        // --- Greeting section ---
        System.out.println("====================================================");
        System.out.println("Welcome to Lawencon AI HR Agent");
        System.out.println("====================================================");
        System.out.println("Coba tanya sesuatu seperti:");
        System.out.println("- siapa manajer rina");
        System.out.println("- sisa cuti tahunan budi");
        System.out.println("- tolong ajukan cuti sakit buat rina dari 3 okt sampai 5 okt");
        System.out.println("Ketik 'help' untuk melihat semua perintah, atau 'exit' untuk keluar.\n");

        // --- Main loop ---
        while (true) {
            System.out.print("\u001B[1;36mYou:\u001B[0m ");
            String input = sc.nextLine().trim();

            // --- Exit command ---
            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("keluar")) {
                System.out.println("\nTerima kasih sudah menggunakan Lawencon HR Agent!");
                System.out.println("Semoga harimu produktif");
                break;
            }

            // --- Help command ---
            if (input.equalsIgnoreCase("help")) {
                System.out.println("""
                    Contoh perintah yang bisa kamu coba:
                    - siapa manajer rina
                    - sisa cuti rina
                    - sisa cuti tahunan budi
                    - ajukan cuti sakit buat rina tanggal 3 okt sampai 5 okt
                    - jadwalkan review performa untuk rina dengan bu santi
                    - info budi
                    """);
                continue;
            }

            // --- Empty input handling ---
            if (input.isBlank()) {
                System.out.println("Oops, sepertinya Kamu belum ngetik apa-apa\n");
                continue;
            }

            // --- Intent classification ---
            var parsed = clf.parse(input);

            // --- Execution ---
            String result = executor.execute(parsed.intent, parsed.entities);

            // --- Output ---
            System.out.println(result + "\n");
        }

        sc.close();
    }
}
