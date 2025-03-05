# Blog-Backend mit Text-Validierung über Kafka

Dieses Projekt besteht aus zwei Quarkus-Diensten: 
- **blog-backend** (REST-API mit Datenbankanbindung)
- **text-validator** (Kafka-basierter Validierungsservice)

##  Setup & Installation

### **1️ Voraussetzungen**
- Installiere **Docker** & **Docker Compose** (optional)
- Installiere **cURL** oder **Postman** für API-Tests

### **2️ Netzwerk & Datenbank starten**
Zuerst das Docker-Netzwerk erstellen:

```sh
docker network create blog-nw
```

# 🛠 Setup & Testing für Blog-Backend & Text-Validator

## 1️ **MySQL-Datenbank starten**

```sh
docker run -d --name mysql-db --network blog-nw -e MYSQL_ROOT_PASSWORD=rootpassword -e MYSQL_DATABASE=blogdb -e MYSQL_USER=bloguser -e MYSQL_PASSWORD=blogpassword -p 3306:3306 mysql:latest
```

## 2 **Kafka (Redpanda) Container starten**

```sh
docker run -d --name redpanda-1 --network blog-nw -p 9092:9092 docker.redpanda.com/redpandadata/redpanda:v23.3.5 start --advertise-kafka-addr redpanda-1:9092
```

## 3 **Kafka-Topics erstellen**

```sh
docker exec -it redpanda-1 rpk topic create validation-request --brokers=localhost:9092
docker exec -it redpanda-1 rpk topic create validation-response --brokers=localhost:9092
```

##  **Dienste starten**
### **Blog-Backend starten**

```sh
docker run -d --name=blog-backend --network blog-nw -p 8080:8080 ghcr.io/fanki/blog-backend:1.0.0-SNAPSHOT
```

##  **Text-Validator starten**

```sh
docker run -d --name=text-validator --network blog-nw ghcr.io/fanki/text-validator:1.0.0-SNAPSHOT
```

#  API-Endpunkte & Tests für Blog-Backend & Text-Validator

##  **API-Endpunkte**
### **1 Blog-Eintrag erstellen**

```sh
curl -X POST http://localhost:8080/blogs -H "Content-Type: application/json" \
     -d '{"title": "Mein Blog", "content": "Das ist ein Blog-Eintrag."}'

curl -X POST http://localhost:8080/blogs -H "Content-Type: application/json" \
     -d '{"title": "Test Blog", "content": "hftm sucks."}'
```

##  **Antwort eines erstellten Blog-Eintrags**

```json
{"id":1,"title":"Mein Blog","content":"Das ist ein Blog-Eintrag.","approved":false}
```

# 📄 Blog-Einträge abrufen & Tests

## **Alle Blog-Einträge abrufen**
Abrufen aller gespeicherten Blog-Einträge, inkl. `approved`-Status:

```sh
curl -X GET http://localhost:8080/blogs
```

#  Beispiel-Antwort & Tests

## **Beispiel-Antwort:**
Nach dem Abrufen der Blog-Einträge sollte eine JSON-Antwort wie diese erscheinen:

```json
[
  {"id":1,"title":"Mein Blog","content":"Das ist ein Blog-Eintrag.","approved":true},
  {"id":2,"title":"Test Blog","content":"hftm sucks.","approved":false}
]
```

# 🛠 **Tests**

## **Kafka-Validierung prüfen**
### **Kafka-Request prüfen**  
Überprüfe, ob die **Validierungsanfrage** an Kafka gesendet wurde:

```sh
docker exec -it redpanda-1 rpk topic consume validation-request --brokers=localhost:9092
```

### **Kafka-Response prüfen**
Überprüfe, ob die Validierungsantwort von Kafka empfangen wurde:

```sh
docker exec -it redpanda-1 rpk topic consume validation-response --brokers=localhost:9092
```

## **MySQL-Datenbank prüfen**
### **Datenbank-Abfrage, um den Status der Blog-Einträge zu sehen**  
Prüfe, ob der `approved`-Status in MySQL korrekt gespeichert wurde:

```sh
docker exec -it mysql-db mysql -u bloguser -pblogpassword -e "USE blogdb; SELECT * FROM BlogEntry;"
```

# 🔐 **GHCR Zugriff für `simeonlin`**
Da die Images **privat** bleiben, muss sich `simeonlin`muss sich zuerst bei GHCR authentifizieren:

```sh
echo "GITHUB_PERSONAL_ACCESS_TOKEN" | docker login ghcr.io -u simeonlin --password-stdin
docker pull ghcr.io/fanki/blog-backend:1.0.0-SNAPSHOT
docker pull ghcr.io/fanki/text-validator:1.0.0-SNAPSHOT
```

