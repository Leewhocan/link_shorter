package org.example;

public class NotificationService {
    private String currentActiveUserId;

    public NotificationService() {

    }

    public void setCurrentActiveUser(String userId) {
        this.currentActiveUserId = userId;
        // При смене пользователя НЕ показываем никаких уведомлений
        // Уведомления только в реальном времени для активного пользователя
    }

    public void sendNotification(String targetUserId, String message) {
        // Отправляем уведомление ТОЛЬКО если:
        // 1. Это активный пользователь
        // 2. ID совпадает
        if (targetUserId.equals(currentActiveUserId)) {
            System.out.println("\n[NOTIFICATION] " + message);
        }
        // Если пользователь не активен - уведомление просто игнорируется
    }

    public String getCurrentActiveUserId() {
        return currentActiveUserId;
    }

    // Метод для проверки, является ли пользователь активным
    public boolean isUserActive(String userId) {
        return userId.equals(currentActiveUserId);
    }
}