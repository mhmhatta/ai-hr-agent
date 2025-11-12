import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class IntentClassifierGemini {

    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final String MODEL = "gemini-2.5-flash";
    private static final String ENDPOINT =
        "https://generativelanguage.googleapis.com/v1/models/" + MODEL + ":generateContent?key=" + API_KEY;

    private final HttpClient client = HttpClient.newHttpClient();
    // === STRUCT ===
    public static class ParsedIntent {
        public String intent;
        public Map<String, String> entities;

        public ParsedIntent(String intent, Map<String, String> entities) {
            this.intent = intent;
            this.entities = entities;
        }
    }

    // === MAIN LOGIC ===
    public ParsedIntent parse(String input) {
        try {
            String prompt = buildPrompt(input);
            String response = callGemini(prompt);
            return parseLLMResponse(response);
        } catch (Exception e) {
            System.out.println("Fallback triggered: " + e.getMessage());
            return new ParsedIntent("unknown", new HashMap<>());
        }
    }

    // === PROMPT BUILDER ===
    private String buildPrompt(String input) {
        return """
        Kamu adalah asisten HR digital yang menganalisis perintah pengguna dan mengembalikan JSON valid.
        Jangan tambahkan teks lain di luar JSON.

        Format output yang harus kamu gunakan:
        {
          "intent": "get_manager|get_leave_balance|get_employee_info|apply_leave|schedule_review|check_leave_status|submit_expense|lookup_colleague|help|unknown",
          "name": "<nama_karyawan>",
          "type": "<jenis_cuti>",
          "start_date": "<YYYY-MM-DD>",
          "end_date": "<YYYY-MM-DD>",
          "reviewer": "<nama_reviewer>",
          "date": "<YYYY-MM-DD>",
          "category": "<kategori_expense>",
          "amount": "<nominal>"
        }

        Contoh:
        Input: "siapa manajer rina"
        Output: {"intent":"get_manager","name":"rina"}

        Input: "cek status pengajuan cuti rina"
        Output: {"intent":"check_leave_status","name":"rina"}

        Input: "ajukan cuti sakit untuk budi dari 2025-10-03 sampai 2025-10-05"
        Output: {"intent":"apply_leave","name":"budi","type":"sakit","start_date":"2025-10-03","end_date":"2025-10-05"}

        Input: "jadwalkan review performa untuk rina dengan santi pada 2025-10-15"
        Output: {"intent":"schedule_review","name":"rina","reviewer":"santi","date":"2025-10-15"}

        Input: "ajukan expense rina makan 150000"
        Output: {"intent":"submit_expense","name":"rina","category":"makan","amount":"150000"}

        Input: "cari info rekan kerja budi"
        Output: {"intent":"lookup_colleague","name":"budi"}

        Input: "help"
        Output: {"intent":"help"}

        Sekarang analisis:
        Input: "%s"
        """.formatted(input);
    }

    // === CALL GEMINI API ===
    private String callGemini(String prompt) throws IOException, InterruptedException {
        JSONObject requestBody = new JSONObject()
                .put("contents", new JSONArray()
                        .put(new JSONObject()
                                .put("parts", new JSONArray()
                                        .put(new JSONObject().put("text", prompt)))));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String body = response.body();
        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini API Error: " + response.statusCode() + " - " + body);
        }

        // Extract text safely
        try {
            JSONObject json = new JSONObject(body);
            JSONArray candidates = json.getJSONArray("candidates");
            JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
            JSONArray parts = content.getJSONArray("parts");
            return parts.getJSONObject(0).getString("text");
        } catch (JSONException e) {
            throw new RuntimeException("Invalid response format: " + e.getMessage());
        }
    }

    // === PARSE GEMINI RESPONSE TO JSON ===
    private ParsedIntent parseLLMResponse(String rawText) {
        try {
            // clean up formatting from LLM
            String text = rawText.replaceAll("(?s)```json", "")
                                 .replaceAll("(?s)```", "")
                                 .trim();

            // find JSON braces if response includes text
            if (!text.trim().startsWith("{")) {
                int start = text.indexOf("{");
                int end = text.lastIndexOf("}");
                if (start >= 0 && end > start) {
                    text = text.substring(start, end + 1);
                } else {
                    throw new JSONException("No valid JSON found in response");
                }
            }

            JSONObject obj = new JSONObject(text);
            String intent = obj.optString("intent", "unknown");

            Map<String, String> entities = new HashMap<>();
            for (String key : obj.keySet()) {
                if (!key.equals("intent")) {
                    entities.put(key, obj.optString(key, ""));
                }
            }

            return new ParsedIntent(intent, entities);

        } catch (Exception e) {
            System.out.println("⚠️ Parse fallback: " + e.getMessage());
            return new ParsedIntent("unknown", new HashMap<>());
        }
    }
}