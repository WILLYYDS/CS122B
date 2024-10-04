const autocompleteInput = $("#autocomplete");
const autocompleteResults = $("#autocomplete-results");
const fuzzySearchCheckbox = $("#fuzzy-search");

let autocompleteTimeout;
let cachedResults = {};

const handleAutocompleteSearch = () => {
    const query = autocompleteInput.val().trim();
    const fuzzy = fuzzySearchCheckbox.is(":checked");

    if (query.length < 3) {
        autocompleteResults.empty();
        return;
    }

    if (cachedResults[query]) {
        console.log("Using cached results for query:", query);
        displayAutocompleteResults(cachedResults[query]);
        return;
    }

    console.log("Initiating autocomplete search for query:", query);
    const startTime = Date.now();

    $.ajax({
        url: "api/full-text-search",
        method: "GET",
        data: { query: query, fuzzy: fuzzy },
        success: (resultData) => {
            const endTime = Date.now();
            const searchTime = endTime - startTime;
            console.log("Autocomplete search results:", resultData);
            console.log("Autocomplete search time:", searchTime, "ms");
            localStorage.setItem(query, JSON.stringify(resultData));
            displayAutocompleteResults(resultData);
        },
        error: (jqXHR, textStatus, errorThrown) => {
            console.error("Error:", textStatus, errorThrown);
        }
    });
};

const displayAutocompleteResults = (resultData) => {
    autocompleteResults.empty();

    resultData.forEach(movie => {
        const li = $("<li></li>").text(movie.title);
        li.click(() => {
            window.location.href = `single-movie.html?id=${movie.id}`;
        });
        autocompleteResults.append(li);
    });
};

const handleAutocompleteSubmit = (event) => {
    event.preventDefault();
    const query = autocompleteInput.val().trim();

    if (query.length < 3) {
        window.location.href = `movie-list.html?num=10&page=1&sort=r0t1&input=title:${query}`;
    } else {
        const selectedResult = autocompleteResults.find(".selected");
        if (selectedResult.length) {
            selectedResult.click();
        } else {

            const exactMatch = cachedResults[query] && cachedResults[query].find(movie => movie.title.toLowerCase() === query.toLowerCase());
            if (exactMatch) {
                window.location.href = `single-movie.html?id=${exactMatch.id}`;
            } else {
                window.location.href = `movie-list.html?num=10&page=1&sort=r0t1&input=title:${query}`;
            }
        }
    }
};


autocompleteInput.on("input", () => {
    clearTimeout(autocompleteTimeout);
    autocompleteTimeout = setTimeout(handleAutocompleteSearch, 300);
});

autocompleteInput.on("keydown", (event) => {
    if (event.keyCode === 13) {
        handleAutocompleteSubmit(event);
    } else if (event.keyCode === 38) {
        const selectedResult = autocompleteResults.find(".selected");
        if (selectedResult.length) {
            selectedResult.removeClass("selected");
            const prevResult = selectedResult.prev();
            if (prevResult.length) {
                prevResult.addClass("selected");
                autocompleteInput.val(prevResult.text());
            }
        }
    } else if (event.keyCode === 40) {
        const selectedResult = autocompleteResults.find(".selected");
        if (selectedResult.length) {
            selectedResult.removeClass("selected");
            const nextResult = selectedResult.next();
            if (nextResult.length) {
                nextResult.addClass("selected");
                autocompleteInput.val(nextResult.text());
            }
        } else {
            const firstResult = autocompleteResults.find("li:first-child");
            if (firstResult.length) {
                firstResult.addClass("selected");
                autocompleteInput.val(firstResult.text());
            }
        }
    }
});

$("#search-button").click(handleAutocompleteSubmit);