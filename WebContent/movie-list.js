const cartForm = $("#cart_form");

function getParameterByName(target) {
    let url = window.location.href;
    target = target.replace(/[\[\]]/g, "\\$&");

    const regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)");
    const results = regex.exec(url);

    if (!results) {
        return null;
    }

    if (!results[2]) {
        return '';
    }

    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function updateUrlWithPageNumber(page) {
    const url = window.location.href;
    return url.replace(/(page=)(\d+)/, `$1${page}`);
}

async function handleMovieListResult(resultData) {
    const page = parseInt(getParameterByName('page'));

    if (resultData.length === 0 && page > 1) {
        window.location.href = sessionStorage.getItem("previousURL");
        return;
    }

    console.log("handleResult: prev, next and search page");


    console.log("handleMovieListResult: populating movie list table from resultData");
    const numDropdown = $("#num");
    numDropdown.append(`
        <li><a href="#" id="num_10" onclick="updateNum(10)">10</a></li>
        <li><a href="#" id="num_25" onclick="updateNum(25)">25</a></li>
        <li><a href="#" id="num_50" onclick="updateNum(50)">50</a></li>
        <li><a href="#" id="num_100" onclick="updateNum(100)">100</a></li>
    `);

    const sortNav = $("#sort_nav");
    sortNav.append(`
        <li><a href="#" id="sort_t1r0" onclick="updateSort('t1r0')">Title ↑ Rating ↓</a></li>
        <li><a href="#" id="sort_t0r0" onclick="updateSort('t0r0')">Title ↓ Rating ↑</a></li>
        <li><a href="#" id="sort_t1r1" onclick="updateSort('t1r1')">Title ↑ Rating ↑</a></li>
        <li><a href="#" id="sort_t0r1" onclick="updateSort('t0r1')">Title ↓ Rating ↓</a></li>
        <li><a href="#" id="sort_r1t0" onclick="updateSort('r1t0')">Rating ↑ Title ↓</a></li>
        <li><a href="#" id="sort_r0t1" onclick="updateSort('r0t1')">Rating ↓ Title ↑</a></li>
        <li><a href="#" id="sort_r1t1" onclick="updateSort('r1t1')">Rating ↑ Title ↑</a></li>
        <li><a href="#" id="sort_r0t0" onclick="updateSort('r0t0')">Rating ↓ Title ↓</a></li>
    `);

    const prevNext = $("#prevNext");
    prevNext.append(`
        <li><a href="#" onclick="updatePage(1)">Next</a></li>
        <li><a href="#" onclick="updatePage(-1)">Prev</a></li>
    `);

    const movieListTableBodyElement = $("#movieList_table_body");

    for (const movie of resultData) {
        const { id, title, year, director, genre1, genre2, genre3, starId1, star1, starId2, star2, starId3, star3, rating } = movie;

        let rowHTML = '<tr>';
        rowHTML += `<th><a href="single-movie.html?id=${id}">${title}</a></th>`;
        rowHTML += `<th>${year}</th>`;
        rowHTML += `<th>${director}</th>`;

        rowHTML += `
            <th>
                <a href="movie-list.html?num=10&page=1&sort=r0t1&input=genre:${genre1}">${genre1}</a>
                <p></p>
                <a href="movie-list.html?num=10&page=1&sort=r0t1&input=genre:${genre2}">${genre2}</a>
                <p></p>
                <a href="movie-list.html?num=10&page=1&sort=r0t1&input=genre:${genre3}">${genre3}</a>
            </th>
        `;

        rowHTML += '<th>';
        rowHTML += `<a href="single-star.html?id=${starId1}">${star1}</a>`;

        if (starId2) {
            rowHTML += `<p></p><a href="single-star.html?id=${starId2}">${star2}</a>`;
        }

        if (starId3) {
            rowHTML += `<p></p><a href="single-star.html?id=${starId3}">${star3}</a>`;
        }

        rowHTML += '</th>';

        rowHTML += `<th>${rating || 'N/A'}</th>`;
        rowHTML += `<th><button type="button" onclick="addFunction(this.value)" name="button" value="${[encodeURIComponent(title), id]}">Add</button></th>`;
        rowHTML += '</tr>';

        movieListTableBodyElement.append(rowHTML);
    }

    sessionStorage.setItem("previousURL", window.location.href);
}

async function fetchMovieList(num, page, sort, input) {
    try {
        const response = await $.ajax({
            dataType: "json",
            method: "GET",
            url: `api/movie-list?num=${num}&page=${page}&sort=${sort}&input=${input}`
        });

        await handleMovieListResult(response);
    } catch (error) {
        console.error('Error fetching movie list:', error);
    }
}

const num = getParameterByName('num');
const page = getParameterByName('page');
const sort = getParameterByName('sort');
const input = getParameterByName('input');

function addFunction(ItemAdd) {
    alert(`Successfully added movie ${decodeURIComponent(ItemAdd.split(",")[0])} into your shopping cart!`);

    const previousItems = JSON.parse(sessionStorage.getItem("previousItem")) || [];
    previousItems.push(ItemAdd);
    sessionStorage.setItem("previousItem", JSON.stringify(previousItems));

    console.log(`Added new item ${ItemAdd}`);
}

function updateQueryStringParameter(uri, key, value) {
    const re = new RegExp("([?&])" + key + "=.*?(&|$)", "i");
    const separator = uri.indexOf("?") !== -1 ? "&" : "?";

    if (uri.match(re)) {
        return uri.replace(re, "$1" + key + "=" + value + "$2");
    } else {
        return uri + separator + key + "=" + value;
    }
}

function addQueryStringParameter(uri, key, value) {
    const re = new RegExp("([?&])" + key + "=.*?(&|$)", "i");
    const separator = uri.indexOf("?") !== -1 ? "&" : "?";

    if (uri.match(re)) {
        const currentValue = parseInt(uri.match(re)[0].split("=")[1], 10);
        const newValue = Math.max(currentValue + value, 1);
        return uri.replace(re, "$1" + key + "=" + newValue + "$2");
    } else {
        return uri + separator + key + "=" + value;
    }
}

function updateSort(sortValue) {
    const currentUrl = updateQueryStringParameter(window.location.href, "sort", sortValue);
    window.location.href = currentUrl;
}

function updateNum(numValue) {
    const currentUrl = updateQueryStringParameter(window.location.href, "num", numValue);
    window.location.href = currentUrl;
}

function updatePage(pageValue) {
    const currentUrl = addQueryStringParameter(window.location.href, "page", pageValue);
    window.location.href = currentUrl;
}

fetchMovieList(num, page, sort, input);