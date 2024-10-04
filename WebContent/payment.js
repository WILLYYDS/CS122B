let payment_form = jQuery("#payment_form");

function checkCreditCardInformation(resultData) {
    console.log("Checking credit card response");
    console.log(resultData);
    console.log(resultData["status"]);

    if (resultData["status"] === "success") {
        // If payment is successful, redirect to the confirmation page with necessary parameters
        window.location.href = "confirmation.html?saleId=" + resultData["saleId"] + "&totalPrice=" + resultData["totalPrice"];
    } else {
        console.log("show error message");
        console.log(resultData["message"]);
        jQuery("#payment_error_message").text(resultData["message"]);
    }
}

function submitCreditCardInformation(formSubmitEvent) {
    console.log("submit payment form");

    formSubmitEvent.preventDefault();

    // Retrieve customerId and movieId from session storage
    let customerId = sessionStorage.getItem("customerId");
    let movieId = sessionStorage.getItem("movieId");

    // Include customerId and movieId in the form data
    let formData = payment_form.serialize() + "&customerId=" + customerId + "&movieId=" + movieId;

    jQuery.ajax({
        url: "api/movie-payment",
        method: "POST",
        data: formData,
        success: checkCreditCardInformation,
        error: function(resultData) {
            console.log(resultData);
            jQuery("#payment_error_message").text("An error occurred while processing your payment. Please try again.");
        }
    });
}

payment_form.submit(submitCreditCardInformation);
