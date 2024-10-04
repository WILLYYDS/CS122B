import com.google.gson.JsonArray;
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
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Objects;

@WebServlet(name = "DashboardServlet", urlPatterns = "/api/dashboard")
public class DashboardServlet extends HttpServlet {
    private DataSource dataSource;

    @Override
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            String allTableQuery = "SELECT DISTINCT TABLE_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = \"moviedb\"";
            try (PreparedStatement queryStatement = conn.prepareStatement(allTableQuery);
                 ResultSet allTableQueryRs = queryStatement.executeQuery()) {

                JsonArray tableJsonArray = new JsonArray();

                while (allTableQueryRs.next()) {
                    String tableName = allTableQueryRs.getString("TABLE_NAME");
                    String query = "SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND TABLE_SCHEMA = \"moviedb\"";

                    JsonArray jsonArray;
                    try (PreparedStatement statement = conn.prepareStatement(query)) {
                        statement.setString(1, tableName);

                        try (ResultSet rs = statement.executeQuery()) {
                            jsonArray = new JsonArray();

                            while (rs.next()) {
                                JsonObject jsonObjectData = new JsonObject();
                                jsonObjectData.addProperty("TABLE_NAME", tableName);
                                jsonObjectData.addProperty("COLUMN_NAME", rs.getString("COLUMN_NAME"));
                                jsonObjectData.addProperty("DATA_TYPE", rs.getString("DATA_TYPE"));
                                jsonArray.add(jsonObjectData);
                            }
                        }
                    }

                    tableJsonArray.add(jsonArray);
                }

                out.write(tableJsonArray.toString());
                response.setStatus(200);
            }
        } catch (Exception e) {
            handleError(request, response, out, e);
        } finally {
            out.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject responseJsonObject = new JsonObject();
        String starName = request.getParameter("star_name");
        String starYear = request.getParameter("star_year");
        String movieTitle = request.getParameter("movie_title");
        String movieYear = request.getParameter("movie_year");
        String movieDirector = request.getParameter("movie_director");
        String movieStar = request.getParameter("movie_star");
        String movieGenre = request.getParameter("movie_genre");

        try (Connection conn = dataSource.getConnection()) {
            if (starName != null) {
                addStar(conn, responseJsonObject, starName, starYear);
            } else if (movieTitle != null) {
                addMovie(conn, responseJsonObject, movieTitle, movieYear, movieDirector, movieStar, movieGenre);
            }

            response.setStatus(200);
        } catch (Exception e) {
            handleError(request, response, responseJsonObject, e);
        }

        response.getWriter().write(responseJsonObject.toString());
    }

    private void addStar(Connection conn, JsonObject responseJsonObject, String starName, String starYear) throws SQLException {
        String maxIdQuery = "SELECT MAX(id) AS maxID FROM stars";

        try (PreparedStatement statement = conn.prepareStatement(maxIdQuery);
             ResultSet rs = statement.executeQuery()) {
            rs.next();
            String maxId = rs.getString("maxID");
            int maxIdNum = Integer.parseInt(maxId.replace("nm", ""));
            String newId = "nm" + (maxIdNum + 1);

            String starQuery = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";

            try (PreparedStatement insertStatement = conn.prepareStatement(starQuery)) {
                insertStatement.setString(1, newId);
                insertStatement.setString(2, starName);
                insertStatement.setString(3, starYear.isEmpty() ? null : starYear);
                insertStatement.executeUpdate();
            }

            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", "Successfully added " + starName + ", star id: " + newId);
        }
    }

    private void addMovie(Connection conn, JsonObject responseJsonObject, String movieTitle, String movieYear, String movieDirector, String movieStar, String movieGenre) throws SQLException {
        try (CallableStatement callableStatement = conn.prepareCall("{CALL add_movie(?, ?, ?, ?, ?, ?)}")) {
            callableStatement.setString(1, movieTitle);
            callableStatement.setString(2, movieYear);
            callableStatement.setString(3, movieDirector);
            callableStatement.setString(4, movieStar);
            callableStatement.setString(5, movieGenre);
            callableStatement.registerOutParameter(6, Types.VARCHAR);
            callableStatement.execute();

            String message = callableStatement.getString(6);
            responseJsonObject.addProperty("message", message);
        }
    }

    private void handleError(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Exception e) throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("errorMessage", e.getMessage());
        out.write(jsonObject.toString());

        request.getServletContext().log("Error:", e);
        response.setStatus(500);
    }

    private void handleError(HttpServletRequest request, HttpServletResponse response, JsonObject responseJsonObject, Exception e) {
        responseJsonObject.addProperty("errorMessage", e.getMessage());
        request.getServletContext().log("Error:", e);
        response.setStatus(500);
    }
}