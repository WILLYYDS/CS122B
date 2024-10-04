let login_form = $("#login_form");
/**
 * Handle the data returned by LoginServlet
 * @param resultDataJson jsonObject
 */
function handleLoginResult(resultDataJson) {
    console.log("handleLoginResult is called");
    console.log("handle login response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the user to top20index.html
    if (resultDataJson["status"] === "success") {
        window.location.replace("index.html");
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);function submitCheckoutForm(formSubmitEvent) {
            formSubmitEvent.preventDefault();

            var cardNumber = document.getElementById('cardNumber').value;
            var firstName = document.getElementById('firstName').value;
            var lastName = document.getElementById('lastName').value;
            var expirationDate = document.getElementById('expDate').value;

            if (!validateForm(cardNumber, firstName, lastName, expirationDate)) {
                alert('Please fill in all fields.');
                return;
            }

            var params = 'cardNumber=' + encodeURIComponent(cardNumber) +
                '&firstName=' + encodeURIComponent(firstName) +
                '&lastName=' + encodeURIComponent(lastName) +
                '&expirationDate=' + encodeURIComponent(expirationDate);

            $.ajax(
                "/api/checkout", {
                    method: "POST",
                    data: params,
                    dataType: "json",
                    success: handleCheckoutResult,
                    error: function(jqXHR, textStatus, errorThrown) {
                        if(jqXHR.status == 404) {
                            alert("Error: " + errorThrown + ". The requested URL was not found on the server.");
                        } else {
                            alert("An error occurred: " + textStatus);
                        }
                    }
                }
            );
        }

        checkout_form.submit(submitCheckoutForm);function submitCheckoutForm(formSubmitEvent) {
            formSubmitEvent.preventDefault();

            var cardNumber = document.getElementById('cardNumber').value;
            var firstName = document.getElementById('firstName').value;
            var lastName = document.getElementById('lastName').value;
            var expirationDate = document.getElementById('expDate').value;

            if (!validateForm(cardNumber, firstName, lastName, expirationDate)) {
                alert('Please fill in all fields.');
                return;
            }

            var params = 'cardNumber=' + encodeURIComponent(cardNumber) +
                '&firstName=' + encodeURIComponent(firstName) +
                '&lastName=' + encodeURIComponent(lastName) +
                '&expirationDate=' + encodeURIComponent(expirationDate);

            $.ajax(
                "/api/checkout", {
                    method: "POST",
                    data: params,
                    dataType: "json",
                    success: handleCheckoutResult,
                    error: function(jqXHR, textStatus, errorThrown) {
                        if(jqXHR.status == 404) {
                            alert("Error: " + errorThrown + ". The requested URL was not found on the server.");
                        } else {
                            alert("An error occurred: " + textStatus);
                        }
                    }
                }
            );
        }

        checkout_form.submit(submitCheckoutForm);
        $("#login_error_message").text(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/login", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: login_form.serialize(),
            dataType: "json",
            success: handleLoginResult
        }
    );
}

// Bind the submit action of the form to a handler function
login_form.submit(submitLoginForm);