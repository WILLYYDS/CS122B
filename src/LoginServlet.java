import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username"); // 修改这里
        String password = request.getParameter("password");

        JsonObject responseJsonObject = new JsonObject();

        // Establish a connection to the database
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            DataSource ds = (DataSource) envCtx.lookup("jdbc/moviedb");

            try (Connection conn = ds.getConnection()) {
                // Create a SQL query
                String query = "SELECT * FROM customers WHERE email = ? AND password = ?";
                PreparedStatement statement = conn.prepareStatement(query);
                statement.setString(1, username);
                statement.setString(2, password);

                // Execute the query
                ResultSet rs = statement.executeQuery();

                // Check if the query returned any result
                if (rs.next()) {
                    // Login success:
                    String firstName = rs.getString("firstName"); // Get firstName from the database
                    String lastName = rs.getString("lastName"); // Get lastName from the database
                    request.getSession().setAttribute("user", new User(firstName, lastName));
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                } else {
                    // Login fail
                    responseJsonObject.addProperty("status", "fail");
                    request.getServletContext().log("Login failed");
                    responseJsonObject.addProperty("message", "Incorrect username or password.");
                }
            } catch (SQLException e) {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "An error occurred while connecting to the database.");
                e.printStackTrace();
            }
        } catch (NamingException e) {
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "An error occurred while looking up the data source.");
            e.printStackTrace();
        }

        response.getWriter().write(responseJsonObject.toString());
    }
}