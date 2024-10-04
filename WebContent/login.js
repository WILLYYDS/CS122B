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

    // If login succeeds, it will redirect the user to index.html
    if (resultDataJson["status"] === "success") {
        window.location.replace("index.html");
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#login_error_message").text(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log("submit login form");

    // Prevent the default form submission
    formSubmitEvent.preventDefault();

    // Get reCAPTCHA response
    // let recaptchaResponse = grecaptcha.getResponse();

    // if (recaptchaResponse.length === 0) {
    //     // If reCAPTCHA is not completed, show error message
    //     $("#login_error_message").text("PLZ finish reCAPTCHA！");
    // } else {
    // If reCAPTCHA is completed, proceed with form submission via AJAX
    $.ajax(
        "api/login", {
            method: "POST",
            data: login_form.serialize(), // + "&g-recaptcha-response=" + recaptchaResponse,
            dataType: "json",
            success: handleLoginResult
        }
    );
    // }
}

// Bind the submit event of the form to the submitLoginForm handler function
login_form.submit(submitLoginForm);
