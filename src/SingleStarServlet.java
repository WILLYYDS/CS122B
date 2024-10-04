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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String id = request.getParameter("id");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        if (id == null) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", "Star ID parameter is missing.");
            out.write(jsonObject.toString());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.close();
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT s.id AS star_id, s.name AS star_name, s.birthYear AS star_dob, " +
                    "m.id AS movie_id, m.title AS movie_title, m.year AS movie_year, m.director AS movie_director " +
                    "FROM stars s " +
                    "INNER JOIN stars_in_movies sim ON s.id = sim.starId " +
                    "INNER JOIN movies m ON sim.movieId = m.id " +
                    "WHERE s.id = ? " +
                    "ORDER BY m.year DESC, m.title";

            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, id);
                ResultSet rs = statement.executeQuery();
                JsonArray jsonArray = new JsonArray();

                while (rs.next()) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("star_id", rs.getString("star_id"));
                    jsonObject.addProperty("star_name", rs.getString("star_name"));
                    jsonObject.addProperty("star_dob", rs.getString("star_dob") != null ? rs.getString("star_dob") : "N/A");
                    jsonObject.addProperty("movie_id", rs.getString("movie_id"));
                    jsonObject.addProperty("movie_title", rs.getString("movie_title"));
                    jsonObject.addProperty("movie_year", rs.getString("movie_year"));
                    jsonObject.addProperty("movie_director", rs.getString("movie_director"));
                    jsonArray.add(jsonObject);
                }

                out.write(jsonArray.toString());
                rs.close();
            }
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.close();
        }
    }
}