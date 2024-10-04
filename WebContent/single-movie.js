/**
 * Retrieve a parameter value from the URL query string.
 * @param {string} target The parameter name.
 * @returns {string|null} The parameter value, or null if not found.
 */
function getParameterByName(target) {
    let url = window.location.href;
    target = target.replace(/[\[\]]/g, "\\$&");

    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handle the data returned by the SingleMovieServlet.
 * @param {Object} resultData The JSON data returned by the servlet.
 */
function handleResult(resultData) {
    console.log("handleResult: link movie-list.html to home");
    let homeElement = $("#home");
    let count = resultData.length - 1;
    let url = resultData[count]["movie_page"];
    console.log("getting movie page url from result data");
    console.log(url);
    homeElement.append('<li><a href="movie-list.html?num=10&page=1&sort=r0t1&input=genre:Action">' + "Movie List" + '</a></li>');
    homeElement.append('<li><a href="shopping-cart.html">' + "Check Out" + '</a></li>');
    homeElement.append('<li><a href="login.html">' + "Log Out" + '</a></li>');

    let button = $("#button");
    button.append("<li><button type='button' onclick=\"addFunction(this.value)\" name='button' value=" + [encodeURIComponent(resultData[0]["movie_title"]), resultData[0]["movie_id"]] + ">Add</button></li>");

    console.log("handleResult: populating movie title from resultData");
    $("#movie_title").append("<p>" + resultData[0]["movie_title"] + "</p>");

    console.log("handleResult: populating movie info from resultData");
    let movieInfoElement = $("#movie_info");
    movieInfoElement.append(
        "<p>" + resultData[0]["movie_year"] + " / " + resultData[0]["movie_director"] + " / " + resultData[0]["rating"] + "</p>" +
        "<p>" + resultData[0]["genre"] + "</p>"
    );

    console.log("handleResult: populating star table from resultData");
    let movieTableBodyElement = $("#star_table_body");
    for (let i = 0; i < Math.min(10, count); i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            '<a href="single-star.html?id=' + resultData[i]['star_id'] + '">' +
            resultData[i]["star_name"] +
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["star_dob"] + "</th>";
        rowHTML += "</tr>";

        movieTableBodyElement.append(rowHTML);
    }
}

/**
 * Submit the GET request to the SingleMovieServlet with the movie ID.
 */
function getMovieDetails() {
    let movieId = getParameterByName('id');

    if (movieId) {
        $.ajax({
            dataType: "json",
            method: "GET",
            url: "api/single-movie?id=" + movieId,
            success: handleResult,
            error: function (errorData) {
                console.error("An error occurred during AJAX request:", errorData);
                alert("Error loading movie details. Please try again.");
            }
        });
    } else {
        console.error("No movie ID in URL query string");
        alert("No movie ID specified.");
    }
}

/**
 * Add a movie to the shopping cart.
 * @param {string} itemToAdd The movie title and ID to add.
 */
function addFunction(itemToAdd) {
    let [movieTitle, movieId] = itemToAdd.split(",");
    movieTitle = decodeURIComponent(movieTitle);

    alert('Successfully added movie ' + movieTitle + ' into your shopping cart!');

    let array = JSON.parse(sessionStorage.getItem("previousItem"));
    if (!array) {
        array = [];
    }
    array.push(itemToAdd);
    sessionStorage.setItem("previousItem", JSON.stringify(array));
    console.log('Added new item ' + itemToAdd);
}

// When the document is fully loaded, fetch the movie details.
$(document).ready(function () {
    getMovieDetails();
});