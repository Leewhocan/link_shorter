package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UrlCleanupService {
    private static final String USERS_JSON = "users.json";
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService scheduler;
    private Runnable cleanupListener;
    private NotificationService notificationService;

    public UrlCleanupService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.scheduler = Executors.newScheduledThreadPool(1);
        startCleanupTaskDelayed();
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    private void startCleanupTaskDelayed() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupAllExpiredLinks();
            } catch (Exception e) {
                System.err.println("Error in cleanup: " + e.getMessage());
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    void cleanupAllExpiredLinks() {
        try {
            Map<String, User> usersMap = readUsersFromFile();
            LocalDateTime now = LocalDateTime.now();
            int totalRemoved = 0;
            boolean changesMade = false;

            for (User user : usersMap.values()) {
                Iterator<Map.Entry<String, Link>> iterator = user.getLinks().entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<String, Link> entry = iterator.next();
                    Link link = entry.getValue();
                    if (now.isAfter(link.getExpiresAt())) {
                        // Отправляем уведомление активному пользователю
                        if (notificationService != null) {
                            String message = "Expired link removed: " + link.getShortUrl();
                            notificationService.sendNotification(user.getId(), message);
                        }

                        iterator.remove();
                        totalRemoved++;
                        changesMade = true;
                    }
                }
            }

            if (changesMade) {
                writeUsersToFile(usersMap);
            }


        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }

    Map<String, User> readUsersFromFile() throws IOException {
        File file = new File(USERS_JSON);

        if (!file.exists() || file.length() == 0) {
            return new HashMap<>();
        }

        String content = new String(Files.readAllBytes(file.toPath()));
        if (content.trim().isEmpty()) {
            return new HashMap<>();
        }

        return objectMapper.readValue(file,
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, User.class));
    }

    void writeUsersToFile(Map<String, User> usersMap) throws IOException {
        objectMapper.writeValue(new File(USERS_JSON), usersMap);
    }

    public void setCleanupListener(Runnable listener) {
        this.cleanupListener = listener;
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}