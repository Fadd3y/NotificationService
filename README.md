 ## Для корректной работы нужно:

- Клонировать оба репозитория.

Микросервис UserService:
https://github.com/Fadd3y/SpringUserService/tree/dev

Микросервис NotifictionService:
https://github.com/Fadd3y/NotificationService

- В NotificationService в файле application.properties заполнить поля 
```
        spring.mail.host=smtp.gmail.com
        spring.mail.port=587
        spring.mail.username=
        spring.mail.password=
```
Я пользовался почтой Gmail поэтому host=smtp.gmail.com, username=(моя почта), password=(пароль сгенерированный в разделе gmail "Пароли приложений"). Извините, но данные своей почты скидывать не буду.

- В SpringUserService в файле application.properties заполнить поля значениями для подключения к локальной базе данных:
```
        spring.datasource.url=jdbc:postgresql://localhost:5432/user_service
        spring.datasource.username=postgres
        spring.datasource.password=admin
```
- Скачать(https://disk.yandex.ru/d/GOJCXpX7qXfLeg) и разархивировать архив с Kafka в папку. Желательно в корень диска, иначе могут быть ошибки типа «The input line is too long. The syntax of the command is incorrect».

- Запустить start.bat(Windows)/start.sh(Linux. сам не проверял), который запускает Kafka. (если пишет что-то типа [2025-06-16 17:53:36,027] INFO [BrokerLifecycleManager id=1] Unable to register broker 1 because the controller returned error DUPLICATE_BROKER_REGISTRATION (kafka.server.BrokerLifecycleManager) не стоит беспокоится, Kafka все равно запустится через какое то время)

- Запустить SpringUserService и NotificationService в отдельных окнах Intellij idea.

- Теперь, вероятно, можно пользоваться.

## API
### SpringUserService http://localhost:8080

- Создание пользователя          __POST /api/users__
```
{
    "name": "example",
    "email": "example@gmail.com",
    "age": 17
}
```
- Получение пользователя         __GET /api/users/{id}__

- Получение всех пользователей   __GET /api/users__     

- Обновление полей пользователя  __PATCH /api/users/{id}__
```
{
    "name": "example",
    "email": "example@gmail.com",
    "age": 17
}
```
- Удаление пользователя          __DELETE /api/users/{id}__

### NotificationService http://localhost:8081

- Отправка email                  __POST /api/send-email__
```
{
    "email": "test@test.ru",
    "subject": "Проверка",
    "text": "Привет, проверка связи"
}
```
