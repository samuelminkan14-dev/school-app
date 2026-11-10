package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class LibraryController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookLoanRepository bookLoanRepository;

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping("/library")
    public String showLibraryPage(Model model) {
        model.addAttribute("books", bookRepository.findAll());
        model.addAttribute("loans", bookLoanRepository.findAll());
        model.addAttribute("students", studentRepository.findAll());
        return "library";
    }

    @PostMapping("/addBook")
    public String addBook(@RequestParam String title, 
                          @RequestParam String author, 
                          @RequestParam String isbn) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        bookRepository.save(book);
        return "redirect:/library";
    }

    @PostMapping("/borrowBook")
    public String borrowBook(@RequestParam Long bookId, 
                             @RequestParam Long studentId) {
        Book book = bookRepository.findById(bookId).orElseThrow();
        Student student = studentRepository.findById(studentId).orElseThrow();

        BookLoan loan = new BookLoan();
        loan.setBook(book);
        loan.setStudent(student);
        loan.setBorrowDate(LocalDate.now());
        bookLoanRepository.save(loan);

        return "redirect:/library";
    }

    @GetMapping("/returnBook/{id}")
    public String returnBook(@PathVariable Long id) {
        BookLoan loan = bookLoanRepository.findById(id).orElseThrow();
        loan.setReturnDate(LocalDate.now());
        bookLoanRepository.save(loan);
        return "redirect:/library";
    }
}