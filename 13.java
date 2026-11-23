package com.student;

public class Book {
    private String title;
    private String author;
    private String url;

    public Book(String title, String author, String url) {
        this.title = title;
        this.author = author;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getUrl() {
        return url;
    }

    public void printSummary(int index) {
        System.out.println(index + ". " + title + " | Author: " + author);
    }

    public void printDetails() {
        System.out.println("=== Book Details ===");
        System.out.println("Title : " + title);
        System.out.println("Author: " + author);
        System.out.println("URL   : " + url);
    }
}