package org.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppConfig {
    private static final String BASE_SHORT_URL;
    private static final int LINK_LIFETIME_MINUTES;
    private static final int DEFAULT_MAX_CLICKS;

    static {
        System.out.println("=== AppConfig Initialization ===");

        // Пробуем получить свойства из манифеста JAR
        Package mainPackage = AppConfig.class.getPackage();
        String manifestBaseUrl = mainPackage.getImplementationTitle(); // Это не сработает, нужно по-другому

        // Вместо этого будем читать из системных свойств или config.properties
        Properties props = loadConfigFromFile();

        // Получаем значение из системных свойств (через Gradle или командную строку)
        String url = System.getProperty("base.short.url");
        if (url == null || url.trim().isEmpty()) {
            url = props.getProperty("base.short.url", "clck.ru/");
        }
        System.out.println("Using base URL: " + url);
        BASE_SHORT_URL = url;

        // Получаем время жизни ссылки
        String lifetimeStr = System.getProperty("link.lifetime.minutes");
        if (lifetimeStr == null) {
            lifetimeStr = props.getProperty("link.lifetime.minutes", "3");
        }
        int lifetime = parseWithDefault(lifetimeStr, 3, "link lifetime");
        LINK_LIFETIME_MINUTES = lifetime;

        // Получаем максимальное количество кликов
        String maxClicksStr = System.getProperty("default.max.clicks");
        if (maxClicksStr == null) {
            maxClicksStr = props.getProperty("default.max.clicks", "10");
        }
        int maxClicks = parseWithDefault(maxClicksStr, 10, "max clicks");
        DEFAULT_MAX_CLICKS = maxClicks;

        System.out.println("=== Final AppConfig Values ===");
        System.out.println("BASE_SHORT_URL: " + BASE_SHORT_URL);
        System.out.println("LINK_LIFETIME_MINUTES: " + LINK_LIFETIME_MINUTES);
        System.out.println("DEFAULT_MAX_CLICKS: " + DEFAULT_MAX_CLICKS);
        System.out.println("================================\n");
    }

    private static Properties loadConfigFromFile() {
        Properties props = new Properties();
        try {
            // Пробуем загрузить из config.properties рядом с JAR
            props.load(new FileInputStream("config.properties"));
            System.out.println("Loaded configuration from config.properties");
        } catch (IOException e) {
            System.out.println("config.properties not found, using defaults");

            // Пробуем загрузить из ресурсов (если config был включен в JAR)
            try {
                props.load(AppConfig.class.getClassLoader().getResourceAsStream("config.properties"));
                System.out.println("Loaded configuration from embedded config.properties");
            } catch (Exception ex) {
                // Игнорируем, используем значения по умолчанию
            }
        }
        return props;
    }

    private static int parseWithDefault(String value, int defaultValue, String propertyName) {
        try {
            int result = value != null ? Integer.parseInt(value) : defaultValue;
            System.out.println("Using " + propertyName + ": " + result);
            return result;
        } catch (NumberFormatException e) {
            System.out.println("Error parsing " + propertyName + ", using default: " + defaultValue);
            return defaultValue;
        }
    }

    public static String getBaseShortUrl() {
        return BASE_SHORT_URL;
    }

    public static int getLinkLifetimeMinutes() {
        return LINK_LIFETIME_MINUTES;
    }

    public static int getDefaultMaxClicks() {
        return DEFAULT_MAX_CLICKS;
    }
}