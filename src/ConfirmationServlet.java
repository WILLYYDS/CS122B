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
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@WebServlet(name = "ConfirmationServlet", urlPatterns = "/api/confirmation")
public class ConfirmationServlet extends HttpServlet {

    private static final long serialVersionUID = 2L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            InitialContext ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        HttpSession session = request.getSession(false);

        if (session != null) {
            List<Integer> salesIds = (List<Integer>) session.getAttribute("salesIds");

            if (salesIds != null && !salesIds.isEmpty()) {
                try (Connection conn = dataSource.getConnection()) {
                    JsonArray jsonArray = new JsonArray();

                    for (Integer saleId : salesIds) {
                        String query = "SELECT s.id as saleId, m.title as movieTitle, s.price as price, s.quantity as quantity " +
                                "FROM sales s JOIN movies m ON s.movieId = m.id WHERE s.id = ?";
                        try (PreparedStatement ps = conn.prepareStatement(query)) {
                            ps.setInt(1, saleId);
                            try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    JsonObject jsonObject = new JsonObject();
                                    jsonObject.addProperty("saleId", rs.getInt("saleId"));
                                    jsonObject.addProperty("movieTitle", rs.getString("movieTitle"));
                                    jsonObject.addProperty("price", rs.getDouble("price"));
                                    jsonObject.addProperty("quantity", rs.getInt("quantity"));
                                    jsonArray.add(jsonObject);
                                }
                            }
                        }
                    }

                    System.out.println("Response JSON: " + jsonArray.toString()); // 调试信息
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write(jsonArray.toString());
                } catch (SQLException e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
