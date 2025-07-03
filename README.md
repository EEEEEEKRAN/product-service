# Product Service - Gestion des produits

Le service qui gère tout le catalogue produits : création, modification, recherche, prix, stock... Bref, tout ce qui touche aux produits de notre petit e-commerce

## C'que ça fait

### Fonctionnalités principales

- **Catalogue produits** : Création, modification, suppression
- **Gestion des prix** : Prix unitaires, promotions
- **Stock** : Suivi des quantités disponibles
- **Recherche** : Par nom, catégorie, prix
- **Catégories** : Organisation des produits
- **Images** : URLs des photos produits

### Endpoints disponibles

#### Publics
- `GET /api/products` - Liste tous les produits
- `GET /api/products/{id}` - Récupère un produit par ID
- `GET /api/products/search` - Recherche par nom
- `GET /api/products/category/{category}` - Produits par catégorie
- `GET /api/products/price-range` - Produits dans une fourchette de prix
- `GET /api/products/test` - Test que le service tourne

#### Protégés (auth admin requise)
- `POST /api/products` - Crée un nouveau produit
- `PUT /api/products/{id}` - Met à jour un produit
- `DELETE /api/products/{id}` - Supprime un produit
- `PUT /api/products/{id}/stock` - Met à jour le stock
- `GET /api/products/stats` - Statistiques des produits

#### Internes (pour les autres services)
- `GET /internal/products/{id}` - Infos produit allégées
- `POST /internal/products/validate` - Validation d'existence
- `POST /internal/products/batch` - Récupération en lot
- `PUT /internal/products/{id}/reserve-stock` - Réservation de stock

## Stack technique

- **Spring Boot 3.2** : Framework principal
- **MongoDB** : Base de données
- **Spring Data MongoDB** : ORM pour MongoDB
- **Validation API** : Validation des données
- **WebClient** : Communication avec les autres services
- **Jackson** : Sérialisation JSON
- **Spring Cache** : Cache en mémoire

## Comment lancer ?

### Prérequis
- Java 17+
- Maven
- MongoDB qui tourne (port 27017)

### Lancement

```bash
# Depuis le dossier product-service
mvn spring-boot:run
```

Le service démarre sur le port **8080**.

### Avec Docker

```bash
# Build l'image
docker build -t product-service .

# Run le container
docker run -p 8080:8080 product-service
```

## Configuration

### Variables d'environnement

- `MONGODB_URI` : URI de connexion MongoDB (défaut: mongodb://localhost:27017/productdb)
- `SERVER_PORT` : Port du service (défaut: 8080)
- `CACHE_TTL` : Durée de vie du cache en secondes (défaut: 300)

### Base de données

Le service crée automatiquement :
- Collection `products` : Données produits
- Index sur le nom pour la recherche
- Index sur la catégorie
- Quelques produits de démo (si la base est vide)

## Modèle de données

### Product
```json
{
  "id": "string",
  "name": "string",
  "description": "string",
  "price": "number",
  "category": "string",
  "stock": "number",
  "imageUrl": "string",
  "isActive": "boolean",
  "createdAt": "datetime",
  "updatedAt": "datetime",
  "tags": ["string"]
}
```

## Exemples d'utilisation

### Récupérer tous les produits
```bash
curl http://localhost:8080/api/products
```

### Rechercher des produits
```bash
# Par nom
curl "http://localhost:8080/api/products/search?name=laptop"

# Par catégorie
curl http://localhost:8080/api/products/category/electronics

# Par fourchette de prix
curl "http://localhost:8080/api/products/price-range?min=100&max=500"
```

### Créer un produit (admin)
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ton-jwt-token" \
  -d '{
    "name": "MacBook Pro",
    "description": "Laptop Apple dernière génération",
    "price": 2499.99,
    "category": "electronics",
    "stock": 10,
    "imageUrl": "https://example.com/macbook.jpg",
    "tags": ["apple", "laptop", "premium"]
  }'
```

### Mettre à jour le stock
```bash
curl -X PUT http://localhost:8080/api/products/ton-product-id/stock \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ton-jwt-token" \
  -d '{"stock": 25}'
```

## Gestion du stock

- **Réservation** : Le stock peut être temporairement réservé lors des commandes
- **Validation** : Vérification automatique de la disponibilité
- **Seuils** : Alertes quand le stock est bas (< 5)
- **Historique** : Suivi des mouvements de stock

## Cache et performance

- **Cache produits** : Les produits populaires sont mis en cache
- **TTL configurable** : Durée de vie du cache ajustable
- **Invalidation** : Cache vidé lors des modifications
- **Pagination** : Résultats paginés pour les grandes listes

## Logs et monitoring

- Logs structurés avec Logback
- Métriques Spring Boot Actuator
- Health check sur `/actuator/health`
- Métriques custom : nombre de produits, stock total, etc.

## Problèmes courants

**Service ne démarre pas ?**
→ Vérifie que MongoDB tourne et que le port 8080 est libre

**Produit non trouvé ?**
→ Check que l'ID est correct et que le produit existe

**Stock négatif ?**
→ Normal, le service empêche les stocks négatifs automatiquement

**Recherche ne fonctionne pas ?**
→ Vérifie que les index MongoDB sont bien créés

**Cache pas à jour ?**
→ Le cache se vide automatiquement après modification, sinon restart le service

---
