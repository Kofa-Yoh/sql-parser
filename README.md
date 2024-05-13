# SQL query parser

The project presents a CLI application that parses an entered SELECT query.

## Stack

- Java 21
- Lombok
- JUnit 5
- Gradle 8.5

## Quick Start

### 1. Download

```
git clone Kofa-Yoh/sql-parser
cd sql-parser
```

### 2 Build and run

```
./gradlew build
java -jar build/libs/sql-parser.jar
```

### 3. Exit
```
exit
```

## Samples of SELECT query
```
SELECT p.id, COUNT(*) docs_count
FROM person p, document d
WHERE p.id = d.person_id AND d.type_id = 3
GROUP BY p.id
HAVING COUNT(*) > 1;
```
```
SELECT p.name as name, p.id, age
FROM person p
LEFT JOIN document d ON d.person_id = p.id
WHERE p.age < 18 OR d.type_id = 3
ORDER BY p.age DESC, name
LIMIT 5
OFFSET 10;
```