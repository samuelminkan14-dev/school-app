package northstar.school.com.school.app;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class NotificationService {

    // 🔴 PUT YOUR MNOTIFY API KEY HERE LATER 🔴
    private static final String MNOTIFY_API_KEY = "YOUR_MNOTIFY_API_KEY";

    public void sendSms(String phoneNumber, String message) {
        if (MNOTIFY_API_KEY.equals("YOUR_MNOTIFY_API_KEY")) {
            System.out.println("⚠️ [SMS PREVIEW - NOT SENT] To: " + phoneNumber + " | Message: " + message);
            return;
        }

        try {
            String cleanPhone = phoneNumber.replaceAll("[^0-9]", "");
            if (cleanPhone.startsWith("0")) {
                cleanPhone = "233" + cleanPhone.substring(1);
            }

            String escapedMessage = message.replace("\"", "\\\"");

            String jsonPayload = String.format("""
                {
                    "recipient": ["%s"],
                    "sender": "SchoolMS",
                    "message": "%s",
                    "is_schedule": false
                }
                """, cleanPhone, escapedMessage);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mnotify.com/api/sms/quick?key=" + MNOTIFY_API_KEY))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            System.err.println("Failed to send SMS: " + e.getMessage());
        }
    }
}