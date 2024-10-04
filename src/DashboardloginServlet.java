import com.google.gson.JsonObject;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.naming.Context;
import javax.naming.InitialContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


@WebServlet(name = "DashboardloginServlet", urlPatterns = "/api/dashboard_login")
public class DashboardloginServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

    public String getServletInfo() {
        return "Servlet connects to MySQL database and displays result of a SELECT";
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");

        // Verify reCAPTCHA
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        } catch (Exception e) {

            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            out.close();
            // set response status to 500 (Internal Server Error)
            response.setStatus(200);
            return;
        }
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        JsonObject responseJsonObject = new JsonObject();

        try {
            // Obtain our environment naming context
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource dataSource = (DataSource) envContext.lookup("jdbc/read");

            // Get a connection from dataSource
            Connection connection = dataSource.getConnection();

            String query = "SELECT * FROM employees AS e WHERE e.email = ?";
            String employee_email = "";
            String employee_password = "";
            // Declare our statement
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);

            System.out.println("Query" + statement);
            // Perform the query
            ResultSet resultSet = statement.executeQuery();

            boolean success = false;
            if (resultSet.next()) {
                employee_email = resultSet.getString("email");
                employee_password = resultSet.getString("password");
                // use the same encryptor to compare the user input password with encrypted password stored in DB
                success = new StrongPasswordEncryptor().checkPassword(password, employee_password);
            }
            System.out.println("Success" + success);
            if(success == false){
                // Login fail
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "incorrect password");
                response.setStatus(200);
                out.write(responseJsonObject.toString());
                out.close();
                connection.close();
                statement.close();
                return;
            }
            if (email.equals(employee_email)) {
                // Login success:
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");

                request.getSession().setAttribute("employee", email);

            } else {
                // Login fail
                responseJsonObject.addProperty("status", "fail");

                // sample error messages. in practice, it is not a good idea to tell user which one is incorrect/not exist.
                if (!email.equals(employee_email)) {
                    responseJsonObject.addProperty("message", "email " + email + " doesn't exist");
                }
            }

            out.write(responseJsonObject.toString());
            out.close();
            connection.close();
            statement.close();

        } catch (Exception e) {

            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
            out.close();

        }
    }
}
