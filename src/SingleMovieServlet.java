import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
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

@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;
    private DataSource dataSource;

    @Override
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/read");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String id = request.getParameter("id");
        request.getServletContext().log("getting id: " + id);

        try (Connection conn = dataSource.getConnection();
             PrintWriter out = response.getWriter()) {

            String query = "SELECT m.id as movieId, m.title, m.year, m.director, " +
                    "GROUP_CONCAT(g.name ORDER BY g.name ASC SEPARATOR ', ') AS genre, r.rating " +
                    "FROM movies m " +
                    "JOIN genres_in_movies gim ON m.id = gim.movieId " +
                    "JOIN genres g ON gim.genreId = g.id " +
                    "LEFT JOIN ratings r ON r.movieId = m.id " +
                    "WHERE m.id = ? " +
                    "GROUP BY m.id, m.title, m.year, m.director, r.rating;";

            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, id);

                try (ResultSet rs = statement.executeQuery()) {
                    JsonArray jsonArray = new JsonArray();

                    if (rs.next()) {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("movie_id", rs.getString("movieId"));
                        jsonObject.addProperty("movie_title", rs.getString("title"));
                        jsonObject.addProperty("movie_year", rs.getString("year"));
                        jsonObject.addProperty("movie_director", rs.getString("director"));
                        jsonObject.addProperty("genre", rs.getString("genre"));
                        jsonObject.addProperty("rating", rs.getString("rating") != null ? rs.getString("rating") : "N/A");
                        jsonArray.add(jsonObject);
                    }

                    query = "WITH temp(id, name, dob) AS (" +
                            "  SELECT stars.id, stars.name, stars.birthYear " +
                            "  FROM stars " +
                            "  JOIN stars_in_movies ON stars.id = stars_in_movies.starId " +
                            "  WHERE stars_in_movies.movieId = ? " +
                            ") " +
                            "SELECT temp.name, temp.id, temp.dob, COUNT(stars_in_movies.movieId) as count " +
                            "FROM temp JOIN stars_in_movies ON temp.id = stars_in_movies.starId " +
                            "GROUP BY temp.name, temp.id, temp.dob " +
                            "ORDER BY COUNT(stars_in_movies.movieId) DESC, temp.name;";

                    try (PreparedStatement statement2 = conn.prepareStatement(query)) {
                        statement2.setString(1, id);

                        try (ResultSet rs2 = statement2.executeQuery()) {
                            while (rs2.next()) {
                                JsonObject jsonObject = new JsonObject();
                                jsonObject.addProperty("star_id", rs2.getString("id"));
                                jsonObject.addProperty("star_name", rs2.getString("name") + "(" + rs2.getString("count") + ")");
                                jsonObject.addProperty("star_dob", rs2.getString("dob") != null ? rs2.getString("dob") : "N/A");
                                jsonArray.add(jsonObject);
                            }
                        }
                    }

                    out.write(jsonArray.toString());
                    response.setStatus(200);
                }
            }
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            response.getWriter().write(jsonObject.toString());
            request.getServletContext().log("Error:", e);
            response.setStatus(500);
        }
    }
}
