import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "MoviePaymentServlet", value = "/api/movie-payment")
public class PaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String creditCardNum = request.getParameter("creditCardNum");
        String expirationDate = request.getParameter("expirationDate");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        if (firstName.isEmpty() || lastName.isEmpty() || creditCardNum.isEmpty() || expirationDate.isEmpty()) {
            response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
            JsonObject jsonError = new JsonObject();
            jsonError.addProperty("errorMessage", "Missing required parameter(s).");
            jsonError.addProperty("status", "failure");
            out.write(jsonError.toString());
            out.close();
            return;
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(getConfirmCardQuery())) {

            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, creditCardNum);
            statement.setString(4, expirationDate);

            ResultSet rs = statement.executeQuery();
            JsonObject jsonResponse = new JsonObject();

            if (rs.next()) {
                boolean isValidCard = rs.getBoolean("is_valid_card");

                if (isValidCard) {
                    jsonResponse.addProperty("status", "success");
                } else {
                    jsonResponse.addProperty("status", "fail");
                    String errorMessage = getErrorMessage(rs);
                    jsonResponse.addProperty("message", errorMessage);
                }
            } else {
                jsonResponse.addProperty("status", "fail");
                jsonResponse.addProperty("message", "No records found.");
            }

            out.write(jsonResponse.toString());
            response.setStatus(HttpURLConnection.HTTP_OK);
        } catch (SQLException e) {
            JsonObject jsonError = new JsonObject();
            jsonError.addProperty("errorMessage", e.getMessage());
            jsonError.addProperty("status", "failure");
            out.write(jsonError.toString());

            request.getServletContext().log("SQL Error:", e);
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        } catch (Exception e) {
            JsonObject jsonError = new JsonObject();
            jsonError.addProperty("errorMessage", e.toString());
            jsonError.addProperty("status", "failure");
            out.write(jsonError.toString());

            request.getServletContext().log("General Error:", e);
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        } finally {
            out.close();
        }
    }

    private String getConfirmCardQuery() {
        return "SELECT " +
                "CASE WHEN c.firstName IS NULL THEN 0 ELSE 1 END AS first_name_exists, " +
                "CASE WHEN c.lastName IS NULL THEN 0 ELSE 1 END AS last_name_exists, " +
                "CASE WHEN c.id IS NULL THEN 0 ELSE 1 END AS credit_card_exists, " +
                "CASE WHEN c.expiration IS NULL THEN 0 ELSE 1 END AS expiration_exists, " +
                "CASE WHEN c.firstName IS NOT NULL AND c.lastName IS NOT NULL AND c.id IS NOT NULL AND c.expiration IS NOT NULL " +
                "THEN 1 ELSE 0 END AS is_valid_card " +
                "FROM creditcards c " +
                "WHERE c.firstName = ? AND c.lastName = ? AND c.id = ? AND c.expiration = ?";
    }

    private String getErrorMessage(ResultSet rs) throws SQLException {
        if (rs.getInt("first_name_exists") == 0) {
            return "First Name does not exist";
        } else if (rs.getInt("last_name_exists") == 0) {
            return "Last Name does not exist";
        } else if (rs.getInt("credit_card_exists") == 0) {
            return "Incorrect Credit Card Number";
        } else {
            return "Invalid Card Expiration Date";
        }
    }
}