document.addEventListener('DOMContentLoaded', () => {
    const searchInput = document.getElementById('searchInput');
    const searchButton = document.getElementById('searchButton');
    const searchResults = document.getElementById('searchResults');
    const resultsList = document.getElementById('resultsList');
    const resultsStats = document.getElementById('resultsStats');
    const pagination = document.getElementById('pagination');
    const loader = document.getElementById('loader');
    const searchContainer = document.querySelector('.search-container');
    let currentPage = 1;

    class SearchUI {
        static createResultCard(result) {
            const resultCard = document.createElement('div');
            resultCard.className = 'result-card bg-white p-6 rounded-lg shadow-sm hover:shadow-md';

            const titleLink = document.createElement('a');
            titleLink.href = result.page.url;
            titleLink.className = 'text-lg text-blue-600 hover:underline font-medium';
            titleLink.textContent = result.page.url;

            const snippet = document.createElement('p');
            snippet.className = 'text-gray-600 mt-2';
            snippet.innerHTML = result.snippet; // Use snippet with highlights

            const highlights = document.createElement('div');
            highlights.className = 'text-sm text-gray-500 mt-2';
            result.highlights.forEach(highlight => {
                const span = document.createElement('span');
                span.className = 'mr-2';
                span.innerHTML = `...${highlight}...`;
                highlights.appendChild(span);
            });

            resultCard.appendChild(titleLink);
            resultCard.appendChild(snippet);
            resultCard.appendChild(highlights);

            return resultCard;
        }

        static async performSearch(query, page = 1) {
            if (!query.trim()) return;

            loader.style.display = 'block';

            try {
                const response = await fetch(`/api/search?query=${encodeURIComponent(query)}&page=${page}&pageSize=10`);
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                const data = await response.json();

                resultsStats.textContent = `Found ${data.totalResults} results (${data.totalPages} pages)`;
                resultsList.innerHTML = '';

                if (data.items && Array.isArray(data.items)) {
                    data.items.forEach(result => {
                        resultsList.appendChild(SearchUI.createResultCard(result));
                    });
                }

                SearchUI.updatePagination(data.totalPages, page);
                searchResults.style.opacity = '1';
                searchContainer.classList.add('results-shown');

            } catch (error) {
                console.error('Search failed:', error);
                resultsStats.textContent = 'An error occurred while searching. Please try again.';
            } finally {
                loader.style.display = 'none';
            }
        }

        static updatePagination(totalPages, currentPage) {
            pagination.innerHTML = '';
            if (totalPages <= 1) return;

            if (currentPage > 1) {
                SearchUI.addPaginationButton('Previous', currentPage - 1);
            }

            for (let i = 1; i <= totalPages; i++) {
                if (i === currentPage) {
                    SearchUI.addPaginationButton(i, i, true);
                } else if (i <= 3 || i >= totalPages - 2 || Math.abs(i - currentPage) <= 1) {
                    SearchUI.addPaginationButton(i, i);
                } else if (Math.abs(i - currentPage) === 2) {
                    SearchUI.addPaginationEllipsis();
                }
            }

            if (currentPage < totalPages) {
                SearchUI.addPaginationButton('Next', currentPage + 1);
            }
        }

        static addPaginationButton(text, page, isActive = false) {
            const button = document.createElement('button');
            button.textContent = text;
            button.className = `px-3 py-1 rounded ${isActive ?
                'bg-blue-600 text-white' :
                'text-blue-600 hover:bg-blue-50'}`;
            button.addEventListener('click', () => {
                currentPage = page;
                SearchUI.performSearch(searchInput.value, page);
            });
            pagination.appendChild(button);
        }

        static addPaginationEllipsis() {
            const span = document.createElement('span');
            span.textContent = '...';
            span.className = 'px-2 text-gray-500';
            pagination.appendChild(span);
        }
    }

    // Event Listeners
    searchButton.addEventListener('click', () => {
        currentPage = 1;
        SearchUI.performSearch(searchInput.value);
    });

    searchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            currentPage = 1;
            SearchUI.performSearch(searchInput.value);
        }
    });
});