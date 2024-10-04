function handleResult(resultData) {
    console.log("handleResult: check out page");

    let homeElement = jQuery("#home");
    homeElement.append('<li><a href="shopping-cart.html">Check Out</a></li>');
    homeElement.append('<li><a href="login.html">Log Out</a></li>');

    console.log("Result Data: ", resultData); // 调试信息

    let cartTableBodyElement = jQuery("#cart_table_body");
    resultData.forEach(sale => {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + sale.saleId + "</th>";
        rowHTML += "<th>" + sale.movieTitle + "</th>";
        rowHTML += "<th>$" + sale.price + "</th>";
        rowHTML += "<th>" + sale.quantity + "</th>";
        rowHTML += "</tr>";
        cartTableBodyElement.append(rowHTML);
    });

    let total = resultData.reduce((sum, sale) => sum + (sale.price * sale.quantity), 0);
    let element = jQuery("#Element");
    element.append('<p>' + "Total is $" + total + '</p>');
    sessionStorage.clear();
}

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/confirmation",
    success: (resultData) => handleResult(resultData)
});
