import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@WebServlet(name = "ConfirmationServlet", urlPatterns = "/api/confirmation")
public class ConfirmationServlet extends HttpServlet {

    private static final long serialVersionUID = 2L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        HttpSession session = request.getSession(false);

        if (session != null) {
            Map<String, Integer> salesData = (Map<String, Integer>) session.getAttribute("salesId");

            if (salesData != null && !salesData.isEmpty()) {
                JsonArray jsonArray = new JsonArray();

                for (Map.Entry<String, Integer> entry : salesData.entrySet()) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty(entry.getKey(), entry.getValue());
                    jsonArray.add(jsonObject);

                    System.out.println("Key: " + entry.getKey());
                    System.out.println("Value: " + entry.getValue());
                }

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(jsonArray.toString());
            } else {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}