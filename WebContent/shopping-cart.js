function handleResult(resultData) {
    const homeElement = $("#home");
    const url = resultData[resultData.length - 1]["movie_page"];
    console.log("Getting movie page URL from result data:", url);

    homeElement.append(`<li><a href="index.html">Home</a></li>`);
    url && homeElement.append(`<li><a href="movie-list.html?${url}">Movie List</a></li>`);
    homeElement.append(`<li><a href="login.html">Log Out</a></li>`);

    const proceedElement = $("#proceed");
    proceedElement.append(`<li><a href="payment.html" onclick="proceedToPayment()">Proceed to Payment --></a></li>`);

    console.log("Shopping Cart");
    refreshCartDisplay();
}

function refreshCartDisplay() {
    const allItems = JSON.parse(sessionStorage.getItem("previousItem") || "[]").sort();
    console.log("All items in the cart:", allItems);

    const cartTableBody = $("#cart_table_body");
    cartTableBody.empty();
    let total = 0;
    let count = 1;
    let i = 0;

    while (i < allItems.length) {
        while (i < allItems.length - 1 && allItems[i] === allItems[i + 1]) {
            count++;
            i++;
        }
        const rowHTML = `
            <tr>
                <td><button type="button" onclick="deleteItem('${allItems[i]}')">x</button></td>
                <td>${decodeURIComponent(allItems[i].split(",")[0])}</td>
                <td>$10</td>
                <td><button type="button" onclick="addItem('${allItems[i]}')">+</button></td>
                <td>${count}</td>
                <td><button type="button" onclick="removeItem('${allItems[i]}')">-</button></td>
                <td>${count * 10}</td>
            </tr>
        `;
        total += count * 10;
        cartTableBody.append(rowHTML);
        i++;
        count = 1;
    }

    $("#total_price").html(`<p>Total is $${total}</p>`);
}

function proceedToPayment() {
    const allItems = JSON.parse(sessionStorage.getItem("previousItem") || "[]");
    if (allItems.length === 0) {
        alert("Your shopping cart is empty. Please add some movies before proceeding to payment.");
        return false;
    }

    // 发送购物车数据到服务器
    $.ajax({
        type: "POST",
        url: "api/shopping-cart",
        data: { cart_data: JSON.stringify(allItems) },
        success: function(response) {
            console.log("Cart data sent to server:", allItems);
            window.location.href = "confirmation.html";
        },
        error: function(error) {
            console.error("Error sending cart data to server:", error);
        }
    });

    return true;
}

$.ajax({
    dataType: "json",
    method: "GET",
    url: "api/shopping-cart",
    success: handleResult
});

function deleteItem(item) {
    const previousItems = JSON.parse(sessionStorage.getItem("previousItem"));
    const updatedItems = previousItems.filter(i => i !== item);
    sessionStorage.setItem("previousItem", JSON.stringify(updatedItems));
    console.log(`Deleted item: ${item}`);
    refreshCartDisplay();
}

function addItem(item) {
    const previousItems = JSON.parse(sessionStorage.getItem("previousItem"));
    previousItems.push(item);
    sessionStorage.setItem("previousItem", JSON.stringify(previousItems));
    console.log(`Increased quantity of item: ${item}`);
    refreshCartDisplay();
}

function removeItem(item) {
    const previousItems = JSON.parse(sessionStorage.getItem("previousItem"));
    const index = previousItems.findIndex(i => i === item);
    if (index !== -1) {
        previousItems.splice(index, 1);
        sessionStorage.setItem("previousItem", JSON.stringify(previousItems));
        console.log(`Decreased quantity of item: ${item}`);
        refreshCartDisplay();
    }
}
