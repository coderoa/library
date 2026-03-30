import java.util.Date;
import java.util.List;

public class Book {
    private String ISBN;
    private String title;
    private String subject;
    private String publisher;
    private String language;
    private int numberOfPages;
    private Date publicationDate;
    private List<Author> authors;

    //Constructor
    public Book(String ISBN, String title, String subject, String publisher,
                String language, int numberOfPages, Date publicationDate, List<Author> authors) {
        this.ISBN = ISBN;
        this.title = title;
        this.subject = subject;
        this.publisher = publisher;
        this.language = language;
        this.numberOfPages = numberOfPages;
        this.publicationDate = publicationDate;
        this.authors = authors;
    }

    //Getters
    public String getISBN() { return ISBN; }
    public String getTitle() { return title; }
    public String getSubject() { return subject; }
    public String getPublisher() { return publisher; }
    public String getLanguage() { return language; }
    public int getNumberOfPages() { return numberOfPages; }
    public Date getPublicationDate() { return publicationDate; }
    public List<Author> getAuthors() { return authors; }

    //Setters
    public void setISBN(String ISBN) { this.ISBN = ISBN; }
    public void setTitle(String title) { this.title = title; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public void setLanguage(String language) { this.language = language; }
    public void setNumberOfPages(int numberOfPages) { this.numberOfPages = numberOfPages; }
    public void setPublicationDate(Date publicationDate) { this.publicationDate = publicationDate; }
    public void setAuthors(List<Author> authors) { this.authors = authors; }
}