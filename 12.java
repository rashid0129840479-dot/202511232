package com.student;

import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class App {
    private static final int MAX_BOOKS = 25;
    private static final Book[] exploredBooks = new Book[MAX_BOOKS];
    private static int exploredCount = 0;

    private static final Book[] readingList = new Book[MAX_BOOKS];
    private static int readingCount = 0;

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        int choice;
        do {
            printMenu();
            choice = readIntInput("Enter your choice: ");

            switch (choice) {
                case 1 -> exploreBooks();
                case 2 -> {
                    System.out.print("Enter title keyword: ");
                    String title = scanner.nextLine().trim();
                    searchBook(title);
                }
                case 3 -> {
                    System.out.print("Enter author keyword: ");
                    String author = scanner.nextLine().trim();
                    searchBook(author, true);
                }
                case 4 -> addBook();
                case 5 -> viewReadingList();
                case 6 -> removeBook();
                case 7 -> displayBookDetails();
                case 0 -> System.out.println("Exiting Gutenberg Book Explorer. Goodbye!");
                default -> System.out.println("Invalid choice. Please select a valid option.");
            }
            System.out.println();
        } while (choice != 0);
    }

    private static void printMenu() {
        System.out.println("=== GUTENBERG BOOK EXPLORER ===");
        System.out.println("1. Explore Books");
        System.out.println("2. Search by Title");
        System.out.println("3. Search by Author");
        System.out.println("4. Add Book to Reading List");
        System.out.println("5. View Reading List");
        System.out.println("6. Remove Book");
        System.out.println("7. Display Book Details");
        System.out.println("0. Exit");
    }

    private static int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    static void exploreBooks() {
        exploredCount = 0;
        String url = "https://books.toscrape.com/";

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(15000)
                    .get();

            Elements items = doc.select("article.product_pod");

            for (Element item : items) {
                if (exploredCount >= MAX_BOOKS) {
                    break;
                }

                Element link = item.selectFirst("h3 a");
                if (link == null) {
                    continue;
                }

                String title = link.attr("title").trim();
                if (title.isEmpty()) {
                    title = link.text().trim();
                }

                String absUrl = link.absUrl("href").trim();
                if (title.isEmpty() || absUrl.isEmpty()) {
                    continue;
                }

                // منع تكرار نفس الكتاب في مصفوفة exploredBooks
                boolean duplicate = false;
                for (int i = 0; i < exploredCount; i++) {
                    if (exploredBooks[i].getUrl().equalsIgnoreCase(absUrl)) {
                        duplicate = true;
                        break;
                    }
                }
                if (duplicate) {
                    continue;
                }

                String author = "Books to Scrape";

                exploredBooks[exploredCount] = new Book(title, author, absUrl);
                exploredBooks[exploredCount].printSummary(exploredCount + 1);
                exploredCount++;
            }

            if (exploredCount == 0) {
                System.out.println("No books found on the page.");
            } else {
                System.out.println("Loaded " + exploredCount + " books.");
            }
        } catch (Exception e) {
            System.out.println("Connection error while exploring books: " + e.getMessage());
        }
    }

    static void searchBook(String title) {
        searchBook(title, false);
    }

    static void searchBook(String keyword, boolean searchByAuthor) {
        if (keyword == null || keyword.trim().isEmpty()) {
            System.out.println("Search keyword cannot be empty.");
            return;
        }

        if (exploredCount == 0) {
            System.out.println("No books loaded yet. Please run '1. Explore Books' first.");
            return;
        }

        String query = keyword.trim().toLowerCase();
        int matches = 0;

        System.out.println(searchByAuthor
                ? "=== Search Results by Author: \"" + keyword.trim() + "\" ==="
                : "=== Search Results by Title: \"" + keyword.trim() + "\" ===");

        for (int i = 0; i < exploredCount; i++) {
            Book b = exploredBooks[i];
            if (b == null) {
                continue;
            }

            String field = searchByAuthor ? b.getAuthor() : b.getTitle();
            if (field != null && field.toLowerCase().contains(query)) {
                b.printSummary(i + 1);
                matches++;
            }
        }

        if (matches == 0) {
            System.out.println("No matching books found.");
        }
    }

    static void addBook() {
        if (readingCount >= MAX_BOOKS) {
            System.out.println("Reading list is full (maximum " + MAX_BOOKS + " books).");
            return;
        }

        if (exploredCount == 0) {
            System.out.println("No books available to add. Please run '1. Explore Books' first.");
            return;
        }

        System.out.println("=== Available Explored Books ===");
        for (int i = 0; i < exploredCount; i++) {
            exploredBooks[i].printSummary(i + 1);
        }

        int choice = readIntInput("Enter book number to add (or 0 to cancel): ");
        if (choice == 0) {
            System.out.println("Cancelled.");
            return;
        }

        if (choice < 1 || choice > exploredCount) {
            System.out.println("Invalid book number.");
            return;
        }

        Book selected = exploredBooks[choice - 1];

        for (int i = 0; i < readingCount; i++) {
            if (readingList[i].getUrl().equalsIgnoreCase(selected.getUrl())) {
                System.out.println("This book is already in your reading list.");
                return;
            }
        }

        readingList[readingCount] = selected;
        readingCount++;
        System.out.println("Added to reading list: " + selected.getTitle());
    }

    static void viewReadingList() {
        if (readingCount == 0) {
            System.out.println("Your reading list is currently empty.");
            return;
        }

        System.out.println("=== Personal Reading List (" + readingCount + ") ===");
        for (int i = 0; i < readingCount; i++) {
            readingList[i].printSummary(i + 1);
        }
    }

    static void removeBook() {
        if (readingCount == 0) {
            System.out.println("Your reading list is empty. Nothing to remove.");
            return;
        }

        viewReadingList();
        int choice = readIntInput("Enter book number to remove (or 0 to cancel): ");
        if (choice == 0) {
            System.out.println("Cancelled.");
            return;
        }

        if (choice < 1 || choice > readingCount) {
            System.out.println("Invalid book number.");
            return;
        }

        int indexToRemove = choice - 1;
        String removedTitle = readingList[indexToRemove].getTitle();

        for (int i = indexToRemove; i < readingCount - 1; i++) {
            readingList[i] = readingList[i + 1];
        }

        readingList[readingCount - 1] = null;
        readingCount--;

        System.out.println("Removed from reading list: " + removedTitle);
    }

    static void displayBookDetails() {
        if (readingCount > 0) {
            System.out.println("1. From Reading List");
            System.out.println("2. From Explored Books");
            int source = readIntInput("Choose list to view details from: ");

            if (source == 1) {
                viewReadingList();
                int choice = readIntInput("Enter book number (or 0 to cancel): ");
                if (choice == 0) {
                    return;
                }
                if (choice < 1 || choice > readingCount) {
                    System.out.println("Invalid book number.");
                    return;
                }
                readingList[choice - 1].printDetails();
                return;
            } else if (source != 2) {
                System.out.println("Invalid choice.");
                return;
            }
        }

        if (exploredCount == 0) {
            System.out.println("No books available to display details.");
            return;
        }

        System.out.println("=== Explored Books ===");
        for (int i = 0; i < exploredCount; i++) {
            exploredBooks[i].printSummary(i + 1);
        }

        int choice = readIntInput("Enter book number (or 0 to cancel): ");
        if (choice == 0) {
            return;
        }
        if (choice < 1 || choice > exploredCount) {
            System.out.println("Invalid book number.");
            return;
        }

        exploredBooks[choice - 1].printDetails();
    }
}