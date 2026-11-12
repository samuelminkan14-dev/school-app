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

    @Autowired private BookRepository bookRepo;
    @Autowired private BookLoanRepository loanRepo;
    @Autowired private StudentRepository studentRepo;

    @GetMapping("/library")
    public String showLibrary(Model model) {
        model.addAttribute("loans", loanRepo.findAll());
        model.addAttribute("books", bookRepo.findAll());
        model.addAttribute("students", studentRepo.findAll());
        return "library";
    }

    @GetMapping("/library/add-book")
    public String showAddBookForm() {
        return "library-add-book";
    }

    @PostMapping("/addBook")
    public String addBook(@RequestParam String title, @RequestParam String author, @RequestParam String isbn) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        bookRepo.save(book);
        return "redirect:/library";
    }

    @PostMapping("/borrowBook")
    public String borrowBook(@RequestParam Long bookId, @RequestParam Long studentId) {
        Book book = bookRepo.findById(bookId).orElseThrow();
        Student student = studentRepo.findById(studentId).orElseThrow();

        BookLoan loan = new BookLoan();
        loan.setBook(book);
        loan.setStudent(student);
        loan.setBorrowDate(LocalDate.now());
        loanRepo.save(loan);
        return "redirect:/library";
    }

    @GetMapping("/returnBook/{id}")
    public String returnBook(@PathVariable Long id) {
        BookLoan loan = loanRepo.findById(id).orElseThrow();
        loan.setReturnDate(LocalDate.now());
        loanRepo.save(loan);
        return "redirect:/library";
    }
}