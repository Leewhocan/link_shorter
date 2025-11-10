package org.example;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class UserService {
    private final UrlCleanupService cleanupService;

    public UserService(UrlCleanupService cleanupService) {
        this.cleanupService = cleanupService;
    }

    // Основной метод для работы с пользователями
    private Map<String, User> getUsersMap() {
        try {
            return cleanupService.readUsersFromFile();
        } catch (IOException e) {
            throw new RuntimeException("Error reading users", e);
        }
    }

    private void saveUsersMap(Map<String, User> usersMap) {
        try {
            cleanupService.writeUsersToFile(usersMap);
        } catch (IOException e) {
            throw new RuntimeException("Error saving users", e);
        }
    }

    // Основные публичные методы
    public User createUser(String name) {
        Map<String, User> usersMap = getUsersMap();
        int nextKeySwitch = usersMap.size() + 1;
        User newUser = new User(nextKeySwitch, name);
        usersMap.put(newUser.getId(), newUser);
        saveUsersMap(usersMap);
        return newUser;
    }

    public Optional<User> findUserById(String userId) {
        return Optional.ofNullable(getUsersMap().get(userId));
    }

    public Map<String, User> getAllUsers() {
        return getUsersMap();
    }

    public void saveUser(User user) {
        Map<String, User> usersMap = getUsersMap();
        usersMap.put(user.getId(), user);
        saveUsersMap(usersMap);
    }
}