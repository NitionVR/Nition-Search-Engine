# NitionSearch

## Overview
Nition Search Engine is a Java-based search engine that provides web crawling, content indexing, and efficient search capabilities. It features proximity-based ranking, multi-threaded crawling, and a modern web interface.

---

## Features

- **Advanced Search**:
  - Proximity-based ranking algorithm
  - Boolean operators (AND, OR, NOT)
  - Phrase searching
  - Content snippets with highlighting
- **Web Crawler**:
  - Multi-threaded crawling
  - Domain-specific crawling
  - Robots.txt compliance
  - Configurable crawl depth and delays
  - Intelligent URL handling
- **Content Processing**:
  - Automatic content extraction
  - HTML cleaning
  - Duplicate URL detection
  - Term position indexing
---

## Architecture

### Core Components

- **Search Engine Core**:
  - Suffix Trie for term indexing
  - Proximity-based scoring
  - Result highlighting and snippets
  - Query parsing and processing

- **Web Crawler**:
  - URLFrontier for URL management
  - Multi-threaded crawling
  - Content extraction
  - Robot.txt handling

- **Persistence Layer**:
  - SQLite database
  - Page storage and retrieval
  - Efficient indexing

- **Web Interface**:
  - Modern UI with TailwindCSS
  - Real-time search
  - Admin interface for crawler control
  - Search statistics

## Web Interface Screenshots

### Home Page
![Home Page](https://github.com/user-attachments/assets/ce7736fc-e7b0-411c-95db-c0499e24db90)

### Search Results
![Search Results](https://github.com/user-attachments/assets/6544a56e-f1e3-49dc-ab67-86e0fa16b65f)

### Active Crawler View
![Active Crawler](https://github.com/user-attachments/assets/2b01fcb9-8263-4a21-8f80-6f6ed84a339d)



## Setup and Installation


### Prerequisites

- Java 17 or higher
- Maven
- SQLite

### Build Instructions

1. **Clone the repository**:
   ```bash
   git clone https://github.com/NitionVR/NitionSearch.git
   cd NitionSearch
   ```
2. **Build the project**:
   ```bash
   mvn clean install
   ```
3. **Run the application**:
   ```bash
   java -jar target/nition-search-1.0-SNAPSHOT.jar
   ``` 

## Configuration

The application can be configured through `application.yml`:

```yaml
crawler:
  maxDepth: 10
  threadCount: 4
  crawlDelayMillis: 1000
  maxPagesPerDomain: 5000
  userAgent: "NitionBot/1.0"
  connectionTimeout: 10000
  maxRetries: 3

spring:
  datasource:
    url: jdbc:sqlite:database.db
    driver-class-name: org.sqlite.JDBC
```

## API Documentation

### Search API

#### Search Endpoint

```http
GET /api/search
```

##### Query Parameters:

- **`query` (required)**: Search query string  
- **`page` (optional)**: Page number (default: 1)  
- **`pageSize` (optional)**: Results per page (default: 10)  
- **`sortOrder` (optional)**: Sort order (`RELEVANCE`, `DATE_DESC`, `DATE_ASC`)

##### Response:

```json
{
  "items": [
    {
      "page": {
        "url": "https://example.com/page",
        "content": "..."
      },
      "snippet": "...matching content...",
      "highlights": ["...context..."],
      "termFrequencies": {
        "term1": 5,
        "term2": 3
      }
    }
  ],
  "totalResults": 42,
  "totalPages": 5
}
```

### Crawler API

#### Start Crawling

```http
POST /api/crawl
```

##### Request Body:

```json
{
  "url": "https://example.com"
}
```

##### Response:
```json
{
  "status": "success",
  "message": "Crawling started for URL: https://example.com"
}
```

#### Stop Crawling

```http
POST /api/crawl/stop
```

##### Response: 

```json
{
  "status": "success",
  "message": "Crawling stopped"
}
```

#### Get Crawler Status

```http
GET /api/crawl/status
```

##### Response:

```json
{
  "processedPages": 150,
  "failedPages": 3,
  "queueSize": 45,
  "crawlRate": 2.5,
  "status": "running",
  "responseCodes": {
    "200": 145,
    "404": 3,
    "500": 2
  }
}
```

### Statistics API

#### Get Search Statistics

```http
GET /api/stats
```

#### Response:

```json
{
  "totalSearches": 1234,
  "popularQueries": {
    "term1": 50,
    "term2": 30
  },
  "indexedPages": 1500
}
```

## Query Syntax

The search engine supports several advanced query operators

1. **AND Operator**
   
   ```text
   term1 AND term2
   ```
   Finds pages containing both terms
   
3. **OR Operator**
   
   ```text
   term1 OR term2
   ```
   Finds pages containing either term
   
4. **NOT Operator**
   
   ```text
   term1 NOT term2
   ```
   Finds pages containing term1 but not term2
   
5. **Phrase Search**
   
   ```text
   "exact phrase"
   ```
   Finds exact phrase matches

## Error Handling

The API uses standard HTTP status code:

- 200: Success
- 400: Bad Request
- 404: Not Found
- 500: Internal Server Error

Error Response Format:

```json
{
  "status": "error",
  "message": "Error description"
}
```

## Best Practices

### Crawling

- Start with specific domains rather than broad crawling.
- Respect `robots.txt` guidelines.
- Use appropriate crawl delays.

### Searching

- Use specific search terms.
- Utilize Boolean operators for precise results.
- Use phrase search for exact matches.

### Performance

- Keep page size reasonable (10-20 results).
- Monitor crawler status.
- Use caching when appropriate.

   
   
   


