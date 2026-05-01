import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Book {
    private final String isbn;
    private String title;
    private String subject;
    private String publisher;
    private final LocalDate publicationDate;
    private final List<Author> authors;
    private final String coverImagePath;

    public Book(String isbn, String title, String subject, String publisher, LocalDate publicationDate, List<Author> authors) {
        this(isbn, title, subject, publisher, publicationDate, authors, null);
    }

    public Book(String isbn, String title, String subject, String publisher, LocalDate publicationDate,
                List<Author> authors, String coverImagePath) {
        this.isbn = isbn;
        this.title = title;
        this.subject = subject;
        this.publisher = publisher;
        this.publicationDate = publicationDate;
        this.authors = List.copyOf(authors);
        this.coverImagePath = coverImagePath;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public String getAuthorNames() {
        return authors.stream().map(Author::name).collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        return title + " (" + isbn + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Book book)) {
            return false;
        }
        return Objects.equals(isbn, book.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }
}
