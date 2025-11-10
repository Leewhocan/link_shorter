# Link Shorter - Сервис сокращения ссылок


## Установка и запуск

### Требования
- Java 17 или выше
- Graddle 9.0.0

### Сборка и запуск

1. **Клонирование репозитория:**


2. **Сборка приложения:**
```bash
./gradlew jar
```

3. **Запуск приложения:**
```bash
java -jar  build/libs/link_shorter-1.0-SNAPSHOT.jar
```


## Использование

### Действия

1. **Регистрация/Авторизация**
   ```
  === URL Shortener Service ===
    1. Register new session
    2. Login by UUID
    3. Exit
    Select option: 


2. **Зареганный пользователь**
   ```
    === URL Shortener Service ===
    1. Register new session
    2. Login by UUID
    3. Exit
    Select option: 1
    Enter your name: name
    Registered: name (User #6)
    
    === URL Shortener Service ===
    Logged in as: name
    1. Create short link
    2. Redirect by short code
    3. Show my links
    4. Delete my link
    5. Update link max clicks
    6. Logout
    7. Exit
    Select option: 
   ```


1. **Генерация ссылки**
   ```
    === URL Shortener Service ===
    Logged in as: name
    1. Create short link
    2. Redirect by short code
    3. Show my links
    4. Delete my link
    5. Update link max clicks
    6. Logout
    7. Exit
    Select option: 1
    Enter long URL to shorten: https://student-lk.skillfactory.ru/my-study
    === Creating new Link ===
    longUrl: https://student-lk.skillfactory.ru/my-study
    shortCode: IR0lg8
   ```

2. **Просмотр моих ссылок**
   ```
   === URL Shortener Service ===
      Logged in as: name
      1. Create short link
      2. Redirect by short code
      3. Show my links
      4. Delete my link
      5. Update link max clicks
      6. Logout
      7. Exit
      Select option: 3
      Your links (name - User #6):
      =========================================
      1. clck.ru/IR0lg8
         Original: https://student-lk.skillfactory.ru/my-study
         Clicks: 0/10
         Created: 2025-11-10T22:11:23.435082700
         Expires: 2025-11-10T22:14:23.435082700
         Active: Yes
      -----------------------------------------
   ```

3. **Изменение параметров ссылки (также и удаление ссылок)**
   ```
      === URL Shortener Service ===
      Logged in as: name
      1. Create short link
      2. Redirect by short code
      3. Show my links
      4. Delete my link
      5. Update link max clicks
      6. Logout
      7. Exit
      Select option: 5
      Update max clicks by:
      1. Short code/URL
      2. Index from list
      Select option: 2
      Your links (name - User #6):
      =========================================
      1. clck.ru/IR0lg8
         Original: https://student-lk.skillfactory.ru/my-study
         Clicks: 0/10
         Created: 2025-11-10T22:11:23.435082700
         Expires: 2025-11-10T22:14:23.435082700
         Active: Yes
      -----------------------------------------
      Enter index of link to update: 1
      Enter new max clicks: 3
      Updated max clicks for clck.ru/IR0lg8: 3 clicks
      
      === URL Shortener Service ===
      Logged in as: name
      1. Create short link
      2. Redirect by short code
      3. Show my links
      4. Delete my link
      5. Update link max clicks
      6. Logout
      7. Exit

   ```

## Архитектура и решения

### Структура проекта

```
src/main/java/org/example/
├── AppConfig.java                  # Конфигурация приложения
├── CliInterface.java               # Основной CLI-интерфейс приложения  
├── Link.java                       # Сущность ссылки
├── LinkService.java                # Сервис работы со ссылками
├── Main.java                       # Точка входа в приложение
├── NotificationService.java        # Сервис уведомлений
├── UrlCleanupService.java          # Сервис очистки устаревших ссылок
├── UrlShortenerService.java        # Основной сервис сокращения URL
├── User.java                       # Сущность пользователя
└── UserService.java                # Сервис работы с пользователями
```

### Ключевые архитектурные решения

1. **Генерация коротких ссылок**
    - Генерация рандмонаго набора из 6 букв, достатчно для того чтобы гдля разных пользователей радомайзер работал

2 **Управление состоянием пользователя**
    -модель аутентификации через UUID

3. **Ограничения ссылок**
    - Максимальное количество использований
    - В случае удаления уведомление получает только владелец
4. **Уведомления**
    - Уведомления получает только владелец в момент удаления ссылки
    - Ссылки проверябтся каждую минуту и удаляются в случае перебора по времени

### Бизнес-логика

1. **Создание ссылки:**
    - Валидация URL
    - Генерация уникального короткого кода
    - Сохранение с указанием владельца

2. **Использование ссылки:**
    - Проверка лимитов использования
    - Проверка срока действия
    - Инкремент счетчика использований

3. **Управление ссылками:**
    - Только владелец может изменять/удалять ссылки
    - Просмотр статистики использования
