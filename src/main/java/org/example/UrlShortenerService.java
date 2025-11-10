package org.example;

import java.awt.*;
import java.net.URI;
import java.util.Map;

public class UrlShortenerService {
    private final UrlCleanupService cleanupService;
    private final UserService userService;
    private final LinkService linkService;
    private final NotificationService notificationService;
    private User currentSession;

    public UrlShortenerService() {
        this.cleanupService = new UrlCleanupService();
        this.userService = new UserService(cleanupService);
        this.linkService = new LinkService(cleanupService);
        this.notificationService = new NotificationService();

        this.cleanupService.setNotificationService(notificationService);
    }

    public void setCleanupListener(Runnable listener) {
        this.cleanupService.setCleanupListener(listener);
    }

    private void cleanupBeforeOperation() {
        cleanupService.cleanupAllExpiredLinks();
    }

    // Регистрация с именем
    public User registerSession(String name) {
        User newUser = userService.createUser(name);
        this.currentSession = newUser;
        this.notificationService.setCurrentActiveUser(newUser.getId());
        System.out.println("Registered: " + name + " (User #" + newUser.getKeySwitch() + ")");
        return newUser;
    }

    // Вход по UUID
    public boolean loginByUUID(String uuid) {
        return userService.findUserById(uuid)
                .map(user -> {
                    this.currentSession = user;
                    this.notificationService.setCurrentActiveUser(user.getId());
                    System.out.println("Logged in as: " + user.getName() + " (User #" + user.getKeySwitch() + ")");
                    return true;
                })
                .orElseGet(() -> {
                    System.out.println("User with UUID " + uuid + " not found");
                    return false;
                });
    }

    // Выход из сессии
    public void logout() {
        if (currentSession != null) {
            System.out.println("Logged out from: " + currentSession.getName());
            this.currentSession = null;
            this.notificationService.setCurrentActiveUser(null);
        } else {
            System.out.println("No active session to logout from");
        }
    }

    public User getCurrentSession() {
        return currentSession;
    }

    public String createShortLink(String longUrl) {
        if (currentSession == null) {
            System.out.println("No active session. Please register or login first.");
            return null;
        }

        cleanupBeforeOperation();

        User currentUser = getCurrentUserFromStorage();

        if (currentUser.getLinks().containsKey(longUrl)) {
            Link existingLink = currentUser.getLinks().get(longUrl);
            System.out.println("Using existing short link for: " + longUrl);
            return existingLink.getShortUrl();
        }

        Link newLink = linkService.createLink(longUrl);
        currentUser.getLinks().put(longUrl, newLink);
        saveCurrentUser(currentUser);

        System.out.println("Created short link: " + newLink.getShortUrl() + " for URL: " + longUrl);
        System.out.println("Expires: " + newLink.getExpiresAt());
        return newLink.getShortUrl();
    }

    public void redirectByShortCode(String input) {
        if (currentSession == null) {
            System.out.println("No active session. Please register or login first.");
            return;
        }

        cleanupBeforeOperation();

        User currentUser = getCurrentUserFromStorage();
        String shortCode = extractShortCode(input);

        linkService.findLinkByShortCode(currentUser, shortCode).ifPresentOrElse(
                link -> handleLinkRedirect(link, shortCode, currentUser),
                () -> System.out.println("Short link not found: " + input)
        );
    }

    private void handleLinkRedirect(Link link, String shortCode, User user) {
        if (linkService.isClickLimitExceeded(link)) {
            String message = "Click limit exceeded for: " + link.getShortUrl();
            notificationService.sendNotification(user.getId(), message);
            System.out.println("Link click limit exceeded: " + link.getCurrentClicks() + "/" + link.getMaxClicks());
            return;
        }

        if (linkService.isLinkExpired(link)) {
            String message = "Expired link removed: " + link.getShortUrl();
            notificationService.sendNotification(user.getId(), message);
            System.out.println("Link has expired");

            linkService.findLinkEntryByShortCode(user, shortCode)
                    .ifPresent(entry -> {
                        linkService.removeLink(user, entry.getKey());
                        saveCurrentUser(user);
                    });
            return;
        }

        if (!link.isActive()) {
            System.out.println("Link is not active");
            return;
        }

        linkService.incrementClickCount(link);
        saveCurrentUser(user);

        try {
            Desktop.getDesktop().browse(new URI(link.getLongUrl()));
            System.out.println("Redirecting to: " + link.getLongUrl());
            System.out.println("Clicks: " + link.getCurrentClicks() + "/" + link.getMaxClicks());
        } catch (Exception e) {
            throw new RuntimeException("Error opening browser", e);
        }
    }

    public void showUserLinks() {
        if (currentSession == null) {
            System.out.println("No active session");
            return;
        }

        cleanupBeforeOperation();

        User currentUser = getCurrentUserFromStorage();
        var links = linkService.getUserLinks(currentUser);

        if (links.isEmpty()) {
            System.out.println("No links found");
            return;
        }

        System.out.println("Your links (" + currentUser.getName() + " - User #" + currentUser.getKeySwitch() + "):");
        System.out.println("=========================================");

        int index = 1;
        for (Link link : links) {
            System.out.println(index + ". " + link.getShortUrl());
            System.out.println("   Original: " + link.getLongUrl());
            System.out.println("   Clicks: " + link.getCurrentClicks() + "/" + link.getMaxClicks());
            System.out.println("   Created: " + link.getCreatedAt());
            System.out.println("   Expires: " + link.getExpiresAt());
            System.out.println("   Active: " + (link.isActive() ? "Yes" : "No"));
            System.out.println("-----------------------------------------");
            index++;
        }
    }

    public boolean deleteUserLink(String shortCodeInput) {
        if (currentSession == null) {
            System.out.println("No active session");
            return false;
        }

        cleanupBeforeOperation();

        User currentUser = getCurrentUserFromStorage();
        String shortCode = extractShortCode(shortCodeInput);

        return linkService.findLinkEntryByShortCode(currentUser, shortCode)
                .map(entry -> {
                    String message = "Link deleted: " + entry.getValue().getShortUrl();
                    notificationService.sendNotification(currentUser.getId(), message);

                    linkService.removeLink(currentUser, entry.getKey());
                    saveCurrentUser(currentUser);
                    System.out.println("Link deleted: " + entry.getValue().getShortUrl());
                    return true;
                })
                .orElseGet(() -> {
                    System.out.println("Link not found: " + shortCodeInput);
                    return false;
                });
    }

    public boolean deleteUserLinkByIndex(int index) {
        if (currentSession == null) {
            System.out.println("No active session");
            return false;
        }

        cleanupBeforeOperation();

        User currentUser = getCurrentUserFromStorage();
        var links = linkService.getUserLinks(currentUser);

        if (index < 1 || index > links.size()) {
            System.out.println("Invalid index: " + index);
            return false;
        }

        Link linkToRemove = links.get(index - 1);
        linkService.findLinkEntryByShortCode(currentUser, linkToRemove.getShortCode())
                .ifPresent(entry -> {
                    String message = "Link deleted: " + linkToRemove.getShortUrl();
                    notificationService.sendNotification(currentUser.getId(), message);

                    linkService.removeLink(currentUser, entry.getKey());
                    saveCurrentUser(currentUser);
                    System.out.println("Link deleted: " + linkToRemove.getShortUrl());
                });

        return true;
    }

    // Вспомогательные методы
    private User getCurrentUserFromStorage() {
        return userService.findUserById(currentSession.getId())
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    private void saveCurrentUser(User user) {
        userService.saveUser(user);
    }

    private String extractShortCode(String input) {
        String baseUrl = AppConfig.getBaseShortUrl();
        return input.startsWith(baseUrl) ? input.substring(baseUrl.length()) : input;
    }


    public boolean updateLinkMaxClicks(String shortCodeInput, int newMaxClicks) {
        if (currentSession == null) {
            System.out.println("No active session");
            return false;
        }

        if (newMaxClicks < 1) {
            System.out.println("Max clicks must be at least 1");
            return false;
        }

        cleanupBeforeOperation();

        User currentUser = getCurrentUserFromStorage();
        String shortCode = extractShortCode(shortCodeInput);

        return linkService.findLinkEntryByShortCode(currentUser, shortCode)
                .map(entry -> {
                    Link link = entry.getValue();

                    // Проверяем, что текущие клики не превышают новый лимит
                    if (link.getCurrentClicks() > newMaxClicks) {
                        System.out.println("Cannot set max clicks lower than current clicks (" +
                                link.getCurrentClicks() + ")");
                        return false;
                    }

                    link.setMaxClicks(newMaxClicks);
                    saveCurrentUser(currentUser);

                    System.out.println("Updated max clicks for " + link.getShortUrl() +
                            ": " + newMaxClicks + " clicks");
                    return true;
                })
                .orElseGet(() -> {
                    System.out.println("Link not found: " + shortCodeInput);
                    return false;
                });
    }

    public boolean updateLinkMaxClicksByIndex(int index, int newMaxClicks) {
        if (currentSession == null) {
            System.out.println("No active session");
            return false;
        }

        if (newMaxClicks < 1) {
            System.out.println("Max clicks must be at least 1");
            return false;
        }

        cleanupBeforeOperation();

        User currentUser = getCurrentUserFromStorage();
        var links = linkService.getUserLinks(currentUser);

        if (index < 1 || index > links.size()) {
            System.out.println("Invalid index: " + index);
            return false;
        }

        Link linkToUpdate = links.get(index - 1);

        // Проверяем, что текущие клики не превышают новый лимит
        if (linkToUpdate.getCurrentClicks() > newMaxClicks) {
            System.out.println("Cannot set max clicks lower than current clicks (" +
                    linkToUpdate.getCurrentClicks() + ")");
            return false;
        }

        linkToUpdate.setMaxClicks(newMaxClicks);
        saveCurrentUser(currentUser);

        System.out.println("Updated max clicks for " + linkToUpdate.getShortUrl() +
                ": " + newMaxClicks + " clicks");
        return true;
    }

    public void shutdown() {
        cleanupService.shutdown();
    }
}