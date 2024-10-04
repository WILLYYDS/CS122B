
function handleResult(resultData) {
    console.log("handleResult: check out page");
    let homeElement = jQuery("#home");
    homeElement.append('<li><a href="shopping-cart.html">' + "Check Out" + '</a></li>');
    homeElement.append('<li><a href="login.html">' + "Log Out" + '</a></li>');

    console.log(resultData);
    let allItems = JSON.parse(sessionStorage.getItem("previousItem"));
    allItems.sort();
    console.log(allItems);
    console.log(allItems.length);

    console.log("handleResult: populating star table from resultData");
    let cartTableBodyElement = jQuery("#cart_table_body");
    let total = 0;
    let count = 1;
    let i = 0;
    while (i < allItems.length - 1){
        while (allItems[i] === allItems[i+1]){
            count ++;
            i ++;
        }
        let rowHTML = "";
        console.log(resultData[0][allItems[i].split(",")[1]])
        rowHTML += "<tr>";
        rowHTML += "<th>" + resultData[0][allItems[i].split(",")[1]] + "</th>";
        rowHTML +=
            "<th>" + decodeURIComponent(allItems[i].split(",")[0]) + "</th>"; //this should be getting the id and title from session.
        rowHTML += "<th>" + "$10" + "</th>";
        rowHTML += "<th>" + count + "</th>";
        total += count * 10;
        rowHTML += "</tr>";
        // Append the row created to the table body, which will refresh the page
        cartTableBodyElement.append(rowHTML);
        i++;
        count = 1;
    }

    if (allItems[allItems.length-1] !== allItems[allItems.length-2]){
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + resultData[0][allItems[allItems.length-1].split(",")[1]] + "</th>";
        rowHTML +=
            "<th>" + decodeURIComponent(allItems[allItems.length-1].split(",")[0]) + "</th>"; //this should be getting the id and title from session.
        rowHTML += "<th>" + "$10" + "</th>";
        rowHTML += "<th>" + count + "</th>";
        total += count * 10;
        rowHTML += "</tr>";
        // Append the row created to the table body, which will refresh the page
        cartTableBodyElement.append(rowHTML);
    }
    let Element = jQuery("#Element");
    Element.append('<p>'+ "You are logged out, please refresh the page and sign in again." + '</p>');
    Element.append('<p>'+ "Total is $" + total + '</p>');
    sessionStorage.clear();
}

// Makes the HTTP GET request and registers on success callback function handleResult

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/confirmation", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});

