package it.uniroma1.boubouk;

import java.util.Date;

public class Book {
    private String title;
    private String author;
    private String editor;
    private String isbn;
    private int nPages;
    private int year;
    private String image;
    private String plot;

    public Book(String title, String author, String editor, String isbn, int nPages, int year,
                String image, String plot) {
        this.title = title;
        this.author = author;
        this.editor = editor;
        this.isbn = isbn;
        this.nPages = nPages;
        this.year = year;
        this.image = image;
        this.plot = plot;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getnPages() {
        return nPages;
    }

    public void setnPages(int nPages) {
        this.nPages = nPages;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }
}
