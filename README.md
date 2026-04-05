# Avito Internship API Tests — Java

Автоматизированные тесты для микросервиса объявлений на Java.

## Быстрый старт

### Предварительные требования

- Java 17+
- Maven 3.8+
- Allure Commandline (опционально, для отчётов)

### Установка и запуск

```bash
git clone <https://github.com/LordOfTheRobots/Avito-internship-spring-2026-qa.git>
cd avito-api-tests-java

mvn test

mvn test -Dcheckstyle.skip=true -Dspotless.check.skip=true

mvn test -Dtest=ItemApiTest#testCreateItemSuccess

mvn test -Dgroups=positive
mvn test -Dgroups=negative
mvn test -Dgroups='!slow'
```

### Allure отчёты

```bash
mvn test

mvn allure:report
mvn allure:serve

allure generate target/allure-results -o target/allure-report --clean
allure open target/allure-report
```

### Форматирование и ошибки кода

```bash
mvn checkstyle:check

mvn spotless:check

mvn spotless:apply

mvn checkstyle:check -Dcheckstyle.skip=false
```
