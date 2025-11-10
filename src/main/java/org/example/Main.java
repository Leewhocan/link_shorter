package org.example;

public class Main {
    public static void main(String[] args) {
        try {
            UrlShortenerService service = new UrlShortenerService();
            CliInterface cli = new CliInterface(service);
            cli.start();
        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
        }
    }
}