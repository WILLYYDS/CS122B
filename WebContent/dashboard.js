let star = $("#star");
let movie = $("#movie");




function handleResult(resultData) {
    // console.log("handleMovieListResult: link movie-list.html to home");
    // let homeElement = jQuery("#home");
    // homeElement.append('<a href="movie-list.html">' + "Home" + '</a>');

    console.log("handleResult: check out page");
    let homeElement = jQuery("#home");
    homeElement.append('<li><a href="index.html">' + "Home" + '</a></li>');
    homeElement.append('<li><a href="movie-list.html">' + "Movie List" + '</a></li>');
    homeElement.append('<li><a href="login.html">' + "Log Out" + '</a></li>');

    console.log("handleResult: dashboard page");
    console.log(resultData);
    let metadataElement = jQuery("#metadata");
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        rowHTML += "<p>" + resultData[i][0]["TABLE_NAME"] + "</p>";
        rowHTML += "<table border >";
        rowHTML += "<tr><th>attribute</th><th>type</th></tr>";
        for (let x = 0; x < resultData[i].length; x++) {
            rowHTML += "<tr>";
            rowHTML += "<td>" + resultData[i][x]["COLUMN_NAME"] + "</td><td>" + resultData[i][x]["DATA_TYPE"] + "</td>";
            rowHTML += "</tr>";
        }
        rowHTML += "</table>";
        metadataElement.append(rowHTML);
    }
}


function showForm(formType) {
    var starForm = document.getElementById('star_body');
    var movieForm = document.getElementById('alpha_body');
    if (formType === 'star') {
        starForm.style.display = 'block';
        movieForm.style.display = 'none';
    } else if (formType === 'movie') {
        movieForm.style.display = 'block';
        starForm.style.display = 'none';
    }
}


jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/dashboard", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the MovieListServlet
});

function handleAddStar(searchEvent) {
    console.log("submit add star form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    searchEvent.preventDefault();

    $.ajax(
        "api/dashboard", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: star.serialize(),
            success: handleAddResult
        }
    );
}


function handleAddMovie(searchEvent) {
    console.log("submit add movie form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    searchEvent.preventDefault();

    $.ajax(
        "api/dashboard", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: movie.serialize(),
            success: handleAddResult
        }
    );
}


function handleAddResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle add response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);
    if (resultDataJson["status"] === "success") {
        alert(resultDataJson["message"]);
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        alert(resultDataJson["message"]);
        // $("#login_error_message").text(resultDataJson["message"]);
    }
}

star.submit(handleAddStar);
movie.submit(handleAddMovie);
