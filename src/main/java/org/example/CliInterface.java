package org.example;

import java.net.URI;
import java.util.Scanner;

public class CliInterface {
    private final UrlShortenerService service;
    private final Scanner scanner;
    private boolean running;

    public CliInterface(UrlShortenerService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
        this.running = true;
    }

    public void start() {
        System.out.println("=== URL Shortener Service Started ===");

        this.service.setCleanupListener(this::redrawInterface);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down service...");
            service.shutdown();
        }));

        while (running) {
            try {
                printMenu();
                System.out.print("Select option: ");

                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    continue;
                }

                try {
                    int choice = Integer.parseInt(input);
                    processChoice(choice);


                    if (!running) {
                        break;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid number.");
                }

                System.out.println();
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                break;
            }
        }

        scanner.close();
        System.out.println("Service stopped successfully.");
    }

    private void processChoice(int choice) {
        boolean isLoggedIn = service.getCurrentSession() != null;

        if (!isLoggedIn) {
            // Меню для неавторизованного пользователя
            switch (choice) {
                case 1:
                    registerSession();
                    break;
                case 2:
                    loginByUUID();
                    break;
                case 3:
                    exit();
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } else {
            // Меню для авторизованного пользователя
            switch (choice) {
                case 1:
                    createShortLink();
                    break;
                case 2:
                    redirectByShortCode();
                    break;
                case 3:
                    showUserLinks();
                    break;
                case 4:
                    deleteUserLink();
                    break;
                case 5:
                    updateLinkMaxClicks();
                    break;
                case 6:
                    logout();
                    break;
                case 7:
                    exit();
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void printMenu() {
        boolean isLoggedIn = service.getCurrentSession() != null;

        System.out.println("=== URL Shortener Service ===");

        if (isLoggedIn) {
            // Меню для авторизованного пользователя
            User currentUser = service.getCurrentSession();
            System.out.println("Logged in as: " + currentUser.getName());
            System.out.println("1. Create short link");
            System.out.println("2. Redirect by short code");
            System.out.println("3. Show my links");
            System.out.println("4. Delete my link");
            System.out.println("5. Update link max clicks");
            System.out.println("6. Logout");
            System.out.println("7. Exit");
        } else {
            // Меню для неавторизованного пользователя
            System.out.println("1. Register new session");
            System.out.println("2. Login by UUID");
            System.out.println("3. Exit");
        }
    }

    private void redrawInterface() {
        System.out.println();
        printMenu();
        System.out.print("Select option: ");
    }

    private void registerSession() {
        System.out.print("Enter your name: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }

        service.registerSession(name);
    }

    private void loginByUUID() {
        System.out.print("Enter your UUID: ");
        String uuid = scanner.nextLine().trim();

        if (uuid.isEmpty()) {
            System.out.println("UUID cannot be empty.");
            return;
        }

        service.loginByUUID(uuid);
    }

    private void logout() {
        service.logout();
    }

    private void redirectByShortCode() {
        System.out.print("Enter short URL or code: ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            System.out.println("Input cannot be empty.");
            return;
        }

        try {
            service.redirectByShortCode(input);
        } catch (Exception e) {
            System.out.println("Error redirecting: " + e.getMessage());
        }
    }

    private void createShortLink() {
        System.out.print("Enter long URL to shorten: ");
        String longUrl = scanner.nextLine().trim();

        if (longUrl.isEmpty()) {
            System.out.println("URL cannot be empty.");
            return;
        }

        if (!isValidUrl(longUrl)) {
            System.out.println("Invalid URL format.");
            return;
        }

        try {
            String shortUrl = service.createShortLink(longUrl);
            if (shortUrl != null) {
                System.out.println("Short link created: " + shortUrl);
            }
        } catch (Exception e) {
            System.out.println("Error creating short link: " + e.getMessage());
        }
    }

    private boolean isValidUrl(String url) {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                return false;
            }
            new URI(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void showUserLinks() {
        try {
            service.showUserLinks();
        } catch (Exception e) {
            System.out.println("Error showing links: " + e.getMessage());
        }
    }

    private void deleteUserLink() {
        System.out.println("Delete by:");
        System.out.println("1. Short code/URL");
        System.out.println("2. Index from list");
        System.out.print("Select option: ");

        if (scanner.hasNextInt()) {
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                deleteByShortCode();
            } else if (choice == 2) {
                deleteByIndex();
            } else {
                System.out.println("Invalid option");
            }
        } else {
            System.out.println("Please enter a valid number");
            scanner.nextLine();
        }
    }

    private void deleteByShortCode() {
        System.out.print("Enter short code or URL to delete: ");
        String shortCode = scanner.nextLine().trim();
        service.deleteUserLink(shortCode);
    }

    private void deleteByIndex() {
        service.showUserLinks();
        System.out.print("Enter index of link to delete: ");
        if (scanner.hasNextInt()) {
            int index = scanner.nextInt();
            scanner.nextLine();
            service.deleteUserLinkByIndex(index);
        } else {
            System.out.println("Invalid index");
            scanner.nextLine();
        }
    }

    private void updateLinkMaxClicks() {
        System.out.println("Update max clicks by:");
        System.out.println("1. Short code/URL");
        System.out.println("2. Index from list");
        System.out.print("Select option: ");

        if (scanner.hasNextInt()) {
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                updateMaxClicksByShortCode();
            } else if (choice == 2) {
                updateMaxClicksByIndex();
            } else {
                System.out.println("Invalid option");
            }
        } else {
            System.out.println("Please enter a valid number");
            scanner.nextLine();
        }
    }

    private void updateMaxClicksByShortCode() {
        System.out.print("Enter short code or URL to update: ");
        String shortCode = scanner.nextLine().trim();

        System.out.print("Enter new max clicks: ");
        if (scanner.hasNextInt()) {
            int newMaxClicks = scanner.nextInt();
            scanner.nextLine();
            service.updateLinkMaxClicks(shortCode, newMaxClicks);
        } else {
            System.out.println("Please enter a valid number");
            scanner.nextLine();
        }
    }

    private void updateMaxClicksByIndex() {
        service.showUserLinks();
        System.out.print("Enter index of link to update: ");
        if (scanner.hasNextInt()) {
            int index = scanner.nextInt();
            scanner.nextLine();

            System.out.print("Enter new max clicks: ");
            if (scanner.hasNextInt()) {
                int newMaxClicks = scanner.nextInt();
                scanner.nextLine();
                service.updateLinkMaxClicksByIndex(index, newMaxClicks);
            } else {
                System.out.println("Please enter a valid number");
                scanner.nextLine();
            }
        } else {
            System.out.println("Invalid index");
            scanner.nextLine();
        }
    }

    private void exit() {
        System.out.println("Exiting...");
        this.running = false;
        scanner.close();
        service.shutdown();
        System.exit(0); // Должно быть последним
    }
}