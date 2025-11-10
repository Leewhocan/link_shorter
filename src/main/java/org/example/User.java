package org.example;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class User {
    private String id;
    private String name;
    private LocalDateTime createdAt;
    private int keySwitch;
    private Map<String, Link> links = new HashMap<>();

    // Конструктор по умолчанию для Jackson
    public User() {
    }

    // Конструктор для создания новых пользователей
    public User(int keySwitch, String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.createdAt = LocalDateTime.now();
        this.keySwitch = keySwitch;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getKeySwitch() { return keySwitch; }
    public void setKeySwitch(int keySwitch) { this.keySwitch = keySwitch; }

    public Map<String, Link> getLinks() { return links; }
    public void setLinks(Map<String, Link> links) { this.links = links; }
}