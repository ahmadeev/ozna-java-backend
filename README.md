Главный репозиторий:
https://github.com/ahmadeev/ozna-app

---

Рабочая ссылка после запуска:
http://localhost:8080/java-backend-1.0-SNAPSHOT/api/hello-world

---

Сборка образа:
```bash
docker build -t ozna-java-backend .
```

Запуск контейнера (привязан к терминалу, localhost:8080):
```bash
docker run --name ozna-java-backend -p 8080:8080 ozna-java-backend

```

Остановка контейнера:
```
Ctrl+C

```
