package org.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppConfig {
    private static final String BASE_SHORT_URL;
    private static final int LINK_LIFETIME_MINUTES;
    private static final int DEFAULT_MAX_CLICKS;

    static {
        Properties props = loadConfigFromFile();

        // Получаем значение из системных свойств (через Gradle или командную строку)
        String url = System.getProperty("base.short.url");
        if (url == null || url.trim().isEmpty()) {
            url = props.getProperty("base.short.url", "clck.ru/");
        }
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
    }

    private static Properties loadConfigFromFile() {
        Properties props = new Properties();
        try {
            // Пробуем загрузить из config.properties рядом с JAR
            props.load(new FileInputStream("config.properties"));
        } catch (IOException e) {
            // Пробуем загрузить из ресурсов (если config был включен в JAR)
            try {
                props.load(AppConfig.class.getClassLoader().getResourceAsStream("config.properties"));
            } catch (Exception ex) {
                // Игнорируем, используем значения по умолчанию
            }
        }
        return props;
    }

    private static int parseWithDefault(String value, int defaultValue, String propertyName) {
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
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