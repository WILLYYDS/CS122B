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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
//import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/read");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Set response content type
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

        // try {
        //     // Verify reCAPTCHA
        //     RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        // } catch (Exception e) {
        //     JsonObject responseJsonObject = new JsonObject();
        //     responseJsonObject.addProperty("status", "fail");
        //     responseJsonObject.addProperty("message", "Please finish reCAPTCHA verification.");
        //     response.getWriter().write(responseJsonObject.toString());
        //     return;
        // }

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String id = "";
        String correctPassword = "";
        boolean success = false;

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT * FROM customers WHERE email = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                id = rs.getString("id");
                correctPassword = rs.getString("password");
                // success = new StrongPasswordEncryptor().checkPassword(password, correctPassword);
                success = password.equals(correctPassword); // For plain text password check
            }
            rs.close();
            statement.close();
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("status", "fail");
            jsonObject.addProperty("message", e.getMessage());
            response.getWriter().write(jsonObject.toString());
            return;
        }

        JsonObject responseJsonObject = new JsonObject();

        if (success && !password.isEmpty()) {
            request.getSession().setAttribute("user", new User(username, id));
            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", "Login successful.");
        } else {
            responseJsonObject.addProperty("status", "fail");
            if (id.isEmpty()) {
                responseJsonObject.addProperty("message", "User " + username + " doesn't exist.");
            } else {
                responseJsonObject.addProperty("message", "Wrong password.");
            }
        }
        response.getWriter().write(responseJsonObject.toString());
    }
}
