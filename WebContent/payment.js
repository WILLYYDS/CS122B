let payment_form = jQuery("#payment_form");
function checkCreditCardInformation(resultData) {
    console.log("Checking credit card response");
    console.log(resultData);
    console.log(resultData["status"]);

    if (resultData["status"] === "success") {
        window.location.replace("confirmation.html");
    }
    else {
        console.log("show error message");
        console.log(resultData["message"]);
        jQuery("#payment_error_message").text(resultData["message"]);
    }
}

function submitCreditCardInformation(formSubmitEvent) {
    console.log("submit login form");

    formSubmitEvent.preventDefault();

    jQuery.ajax(
        "api/movie-payment", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: payment_form.serialize(),
            success: checkCreditCardInformation,
            error: (resultData) => console.log(resultData)
        }
    );
}

payment_form.submit(submitCreditCardInformation)