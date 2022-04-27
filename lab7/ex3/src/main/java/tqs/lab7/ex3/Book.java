package tqs.lab7.ex3;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "book")
public class Book {

    @Id
    private Long id;

    @Column(name = "title") private String title;
    @Column(name = "author") private String author;

    public Book() {}

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }

}
