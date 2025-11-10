package org.example;

import java.time.LocalDateTime;
import java.util.*;

public class LinkService {
    private final UrlCleanupService cleanupService;

    public LinkService(UrlCleanupService cleanupService) {
        this.cleanupService = cleanupService;
    }

    public String generateShortCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    public Optional<Link> findLinkByShortCode(User user, String shortCode) {
        return user.getLinks().values().stream()
                .filter(link -> link.getShortCode().equals(shortCode))
                .findFirst();
    }

    public Optional<Map.Entry<String, Link>> findLinkEntryByShortCode(User user, String shortCode) {
        return user.getLinks().entrySet().stream()
                .filter(entry -> entry.getValue().getShortCode().equals(shortCode))
                .findFirst();
    }

    public Link createLink(String longUrl) {
        String shortCode = generateShortCode();
        return new Link(longUrl, shortCode);
    }

    public boolean isLinkExpired(Link link) {
        return LocalDateTime.now().isAfter(link.getExpiresAt());
    }

    public boolean isClickLimitExceeded(Link link) {
        return link.getCurrentClicks() >= link.getMaxClicks();
    }

    public void incrementClickCount(Link link) {
        link.setCurrentClicks(link.getCurrentClicks() + 1);
    }

    public void removeLink(User user, String longUrl) {
        user.getLinks().remove(longUrl);
    }

    public List<Link> getUserLinks(User user) {
        return new ArrayList<>(user.getLinks().values());
    }
}