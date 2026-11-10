package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class StoreController {

    @Autowired private ProductRepository productRepo;
    @Autowired private ProductSaleRepository saleRepo;
    @Autowired private StudentRepository studentRepo;

    @GetMapping("/store")
    public String showStore(Model model) {
        model.addAttribute("products", productRepo.findAll());
        model.addAttribute("sales", saleRepo.findAll());
        model.addAttribute("students", studentRepo.findAll());
        return "store";
    }

    @PostMapping("/addProduct")
    public String addProduct(@RequestParam String productName, 
                             @RequestParam Double price, 
                             @RequestParam Integer stockQuantity) {
        Product product = new Product();
        product.setProductName(productName);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        productRepo.save(product);
        return "redirect:/store";
    }

    @PostMapping("/sellProduct")
    public String sellProduct(@RequestParam Long productId, 
                              @RequestParam Long studentId, 
                              @RequestParam Integer quantity) {
        Product product = productRepo.findById(productId).orElseThrow();
        Student student = studentRepo.findById(studentId).orElseThrow();

        // Check if we have enough stock
        if (product.getStockQuantity() >= quantity) {
            // Reduce stock
            product.setStockQuantity(product.getStockQuantity() - quantity);
            productRepo.save(product);

            // Create sale record
            ProductSale sale = new ProductSale();
            sale.setProduct(product);
            sale.setStudent(student);
            sale.setQuantity(quantity);
            sale.setTotalAmount(product.getPrice() * quantity);
            sale.setSaleDate(LocalDate.now());
            saleRepo.save(sale);
        }

        return "redirect:/store";
    }
}