package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired 
    private JavaMailSender mailSender;

    public boolean processPayment(Double amount, String phone) {
        System.out.println("💳 Connecting to Payment Gateway...");
        System.out.println("Processing " + amount + " for " + phone);
        
        try {
            Thread.sleep(1000); // Simulate network delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("✅ Payment Successful!");
        return true;
    }

    public String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public void sendReceiptEmail(String email, String studentName, Double amount, String receiptNo, String currency) {
        if (email == null || email.isEmpty()) {
            System.out.println("⚠️ [EMAIL PREVIEW] No email provided for " + studentName);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Payment Receipt - " + receiptNo);
            message.setText("Hello,\n\nWe have received your payment of " + currency + " " + amount + 
                            " for " + studentName + ".\n\nReceipt Number: " + receiptNo + 
                            "\n\nThank you,\nSchool Management");
            
            mailSender.send(message);
            System.out.println("✅ Email Receipt sent to " + email);
        } catch (Exception e) {
            System.out.println("⚠️ EMAIL FAILED TO SEND. (Check Gmail App Password).");
            System.out.println("📧 [EMAIL PREVIEW] To: " + email + " | Subject: Payment Receipt - " + receiptNo + 
                               " | Body: We received " + currency + " " + amount + " for " + studentName);
        }
    }
}