package org.example;

import java.time.LocalDateTime;

public class Link {
    private String longUrl;
    private String shortUrl;
    private String shortCode;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private int maxClicks;
    private int currentClicks;
    private boolean isActive;


    public Link() {}


    public Link(String longUrl, String shortCode) {
        System.out.println("=== Creating new Link ===");
        System.out.println("longUrl: " + longUrl);
        System.out.println("shortCode: " + shortCode);

        this.longUrl = longUrl;
        this.shortCode = shortCode;
        this.shortUrl = AppConfig.getBaseShortUrl() + shortCode;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = createdAt.plusMinutes(AppConfig.getLinkLifetimeMinutes());
        this.maxClicks = AppConfig.getDefaultMaxClicks();
        this.currentClicks = 0;
        this.isActive = true;

        System.out.println("shortUrl: " + this.shortUrl);
        System.out.println("expiresAt: " + this.expiresAt);
        System.out.println("maxClicks: " + this.maxClicks);
        System.out.println("=========================\n");
    }

    // Геттеры и сеттеры
    public String getLongUrl() { return longUrl; }
    public void setLongUrl(String longUrl) { this.longUrl = longUrl; }

    public String getShortUrl() { return shortUrl; }
    public void setShortUrl(String shortUrl) { this.shortUrl = shortUrl; }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public int getMaxClicks() { return maxClicks; }
    public void setMaxClicks(int maxClicks) { this.maxClicks = maxClicks; }

    public int getCurrentClicks() { return currentClicks; }
    public void setCurrentClicks(int currentClicks) { this.currentClicks = currentClicks; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}