package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Controller
public class ProfileController {

    @Autowired private StudentRepository studentRepository;
    @Autowired private StudentDocumentRepository documentRepository;

    // 1. Show the Profile Page
    @GetMapping("/student-profile/{id}")
    public String showProfile(@PathVariable Long id, Model model) {
        Optional<Student> student = studentRepository.findById(id);
        if (student.isPresent()) {
            model.addAttribute("student", student.get());
            model.addAttribute("documents", documentRepository.findByStudentId(id));
            return "student-profile";
        }
        return "redirect:/";
    }

    // 2. Handle File Upload
    @PostMapping("/uploadDocument/{id}")
    public String uploadDocument(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            Student student = studentRepository.findById(id).orElseThrow();
            
            StudentDocument doc = new StudentDocument();
            doc.setFileName(file.getOriginalFilename());
            doc.setFileType(file.getContentType());
            doc.setData(file.getBytes());
            doc.setStudent(student);
            
            documentRepository.save(doc);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:/student-profile/" + id;
    }

    // 3. Handle File Download
    @GetMapping("/downloadDocument/{id}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        StudentDocument doc = documentRepository.findById(id).orElseThrow();
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(doc.getFileType()))
                .body(doc.getData());
    }
    @GetMapping("/student-id-card/{id}")
    public String showIdCard(@PathVariable Long id, Model model) {
        Student student = studentRepository.findById(id).orElseThrow();
        model.addAttribute("student", student);
        return "id-card";
    }
    @GetMapping("/student-qr/{id}")
    public void generateQRCode(@PathVariable Long id, jakarta.servlet.http.HttpServletResponse response) throws Exception {
        Student student = studentRepository.findById(id).orElseThrow();
        
        // The data to put in the QR code
        String qrData = "Index: " + student.getIndexNumber() + " | Name: " + student.getFullName();
        
        // Generate the QR Code
        com.google.zxing.qrcode.QRCodeWriter qrCodeWriter = new com.google.zxing.qrcode.QRCodeWriter();
        com.google.zxing.common.BitMatrix bitMatrix = qrCodeWriter.encode(qrData, com.google.zxing.BarcodeFormat.QR_CODE, 250, 250);
        
        // Convert to image and send to the browser
        java.awt.image.BufferedImage image = com.google.zxing.client.j2se.MatrixToImageWriter.toBufferedImage(bitMatrix);
        response.setContentType("image/png");
        javax.imageio.ImageIO.write(image, "png", response.getOutputStream());
    }
}