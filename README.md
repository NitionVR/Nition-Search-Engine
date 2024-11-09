# NitionSearch

NitionSearch is a web-crawling and search engine application that crawls web pages, indexes their content, and provides an API for searching through indexed pages. This project uses a modular architecture with different layers for crawling, indexing, and serving search requests, making it highly maintainable and extensible.

---

## Table of Contents
- [Project Overview](#project-overview)
- [Features](#features)
- [Getting Started](#getting-started)
---

## Project Overview

NitionSearch is designed to handle the entire lifecycle of a search engine, from crawling web pages to indexing content and providing an HTTP API for queries. It uses a `WebCrawler` to fetch and index content and a `SearchEngine` to provide fast, relevant search results. The search engine also considers term proximity for scoring to return the best-matching pages.

This README provides an overview of the setup, architecture, and usage of NitionSearch.

---

## Features

- **Web Crawling**: Efficient crawling of web pages to collect and index content.
- **Content Indexing**: Indexes each page's content to allow fast searches.
- **Advanced Search Scoring**: Scores results based on term matching and proximity, giving bonus points for exact phrases.
- **RESTful API**: Provides a user-friendly HTTP API to perform search queries and retrieve results.
- **Modular Design**: Clean architecture with a separation of concerns across crawling, indexing, and API serving.

---

## Getting Started

### Prerequisites

- **Java 17** or higher
- **Maven** (for building and managing dependencies)

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/NitionSearch.git
   cd NitionSearch
