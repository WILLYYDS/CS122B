const searchForm = $("#search");

const handleGenreResult = resultData => {
    console.log("handleResult: check out page");
    const homeElement = $("#home");
    homeElement.append(`<li><a href="shopping-cart.html">Check Out</a></li>`);
    homeElement.append(`<li><a href="login.html">Log Out</a></li>`);

    console.log("handleGenreResult: populating genre list from resultData");
    const genreBodyElement = $("#genre_body");
    resultData.forEach(genre => {
        genreBodyElement.append(`<a href="movie-list.html?num=10&page=1&sort=r0t1&input=genre:${genre['genre']}">${genre["genre"]}</a>`);
    });

    console.log("handleAlphaResult: populating alphabet list from resultData");
    const alphaBodyElement = $("#alpha_body");
    [...Array(10).keys()].forEach(i => {
        alphaBodyElement.append(`<a href="movie-list.html?num=10&page=1&sort=r0t1&input=alpha:${i}">${i}</a>`);
    });
    [...Array(26).keys()].map(i => String.fromCharCode(i + 65)).forEach(char => {
        alphaBodyElement.append(`<a href="movie-list.html?num=10&page=1&sort=r0t1&input=alpha:${char}">${char}</a>`);
    });
    alphaBodyElement.append(`<a href="movie-list.html?num=10&page=1&sort=r0t1&input=alpha:*">*</a>`);
};

const handleSearch = searchEvent => {
    console.log("submit search form");
    searchEvent.preventDefault();

    $.ajax({
        url: "api/index",
        method: "POST",
        data: searchForm.serialize(),
        success: handleSearchResult,
        error: (jqXHR, textStatus, errorThrown) => {
            jqXHR.status === 404 ?
                alert(`Error: ${errorThrown}. The requested URL was not found on the server.`) :
                alert(`An error occurred: ${textStatus}`);
        }
    });
};

const handleSearchResult = resultDataString => {
    const resultDataJson = JSON.parse(resultDataString);
    let newURL = "input=";
    resultDataJson["sort_title"] && (newURL += `title:${resultDataJson["sort_title"]}:`);
    resultDataJson["sort_year"] && (newURL += `year:${resultDataJson["sort_year"]}:`);
    resultDataJson["sort_director"] && (newURL += `director:${resultDataJson["sort_director"]}:`);
    resultDataJson["sort_name"] && (newURL += `name:${resultDataJson["sort_name"]}:`);

    window.location.replace(`movie-list.html?num=10&page=1&sort=r0t1&${newURL}`);
};

function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated");

    console.log("sending AJAX request to backend Java Servlet");
    jQuery.ajax({
        "method": "GET",
        "url": "api/movie-suggestion?query=" + query,
        "success": function(data) {
            console.log("lookup ajax successful");
            doneCallback(data);
        },
        "error": function(errorData) {
            console.log("lookup ajax error");
            console.log(errorData);
        }
    });
}

function handleSelectSuggestion(suggestion) {
    console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["movieId"]);
    window.location.href = "single-movie.html?id=" + suggestion["data"]["movieId"];
}

$("#movie-autocomplete").autocomplete({
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback);
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion);
    },
    minChars: 3,
    deferRequestBy: 300
});


$.ajax({
    dataType: "json",
    method: "GET",
    url: "api/index",
    success: handleGenreResult,
    error: (jqXHR, textStatus, errorThrown) => {
        jqXHR.status === 404 ?
            alert(`Error: ${errorThrown}. The requested URL was not found on the server.`) :
            alert(`An error occurred: ${textStatus}`);
    }
});

searchForm.submit(handleSearch);