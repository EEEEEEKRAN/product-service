# Utilise une image Java 17 comme base
FROM openjdk:17-jdk-slim

# Définit le répertoire de travail dans le conteneur
WORKDIR /app

# Copie le fichier pom.xml et le wrapper Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .

# Donne les permissions d'exécution au wrapper Maven
RUN chmod +x mvnw

# Télécharge les dépendances (optimisation du cache Docker)
RUN ./mvnw dependency:go-offline -B

# Copie le code source
COPY src src

# Compile l'application
RUN ./mvnw clean package -DskipTests

# Expose le port 8081 (port du service produit)
EXPOSE 8081

# Commande pour lancer l'application
CMD ["java", "-jar", "target/product-service-0.0.1-SNAPSHOT.jar"]