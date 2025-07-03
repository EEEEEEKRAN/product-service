# Utilise une image Maven avec Java 17
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Définit le répertoire de travail dans le conteneur
WORKDIR /app

# Copie le fichier pom.xml
COPY pom.xml .

# Télécharge les dépendances (optimisation du cache Docker)
RUN mvn dependency:go-offline -B

# Copie le code source
COPY src src

# Compile l'application
RUN mvn clean package -DskipTests

# Étape finale avec une image Java légère
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copie le JAR compilé depuis l'étape de build
COPY --from=build /app/target/product-service-1.0.0.jar app.jar

# Expose le port 8081 (port du service produit)
EXPOSE 8081

# Commande pour lancer l'application
CMD ["java", "-jar", "app.jar"]