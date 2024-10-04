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
import java.util.Objects;

@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movie-list")
public class MovieListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
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
        HttpSession session = request.getSession();
        long servletStartTime = System.currentTimeMillis();

        String url = request.getQueryString();
        if (!Objects.equals(url, "num=null&page=null&sort=null&input=null")) {
            session.setAttribute("movie_page", url);
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String pageNumber = request.getParameter("page");
        String numberPerPage = request.getParameter("num");
        String sort = request.getParameter("sort");
        String input = request.getParameter("input");

        try (Connection conn = dataSource.getConnection()) {
            long jdbcStartTime = System.currentTimeMillis();

            String sortOrder = getSortOrder(sort);
            String inputQuery = getInputQuery(input);

            String query = buildQuery(numberPerPage, pageNumber, sortOrder, inputQuery);
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            List<JsonObject> movieList = new ArrayList<>();
            while (rs.next()) {
                JsonObject movieObject = new JsonObject();
                String movieId = rs.getString("movieId");
                movieObject.addProperty("id", movieId);
                movieObject.addProperty("title", rs.getString("title"));
                movieObject.addProperty("year", rs.getString("year"));
                movieObject.addProperty("director", rs.getString("director"));
                movieObject.addProperty("rating", rs.getString("rating"));
                addGenresToMovieObject(movieObject, rs.getString("genres"));
                addStarsToMovieObject(movieObject, movieId, conn);
                movieList.add(movieObject);
            }

            JsonArray jsonArray = new JsonArray();
            movieList.forEach(jsonArray::add);

            out.write(jsonArray.toString());
            response.setStatus(200);

            long jdbcEndTime = System.currentTimeMillis();
            long jdbcDuration = jdbcEndTime - jdbcStartTime;
            long servletEndTime = System.currentTimeMillis();
            long servletDuration = servletEndTime - servletStartTime;

          //  logTimingInfo(input, servletDuration, jdbcDuration, request);

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        } finally {
            out.close();
        }
    }

    private String getSortOrder(String sort) {
        String sortOrder = "";
        if (sort.substring(0, 2).equals("t0")) {
            sortOrder += "m.title DESC";
        } else if (sort.substring(0, 2).equals("t1")) {
            sortOrder += "m.title";
        } else if (sort.substring(0, 2).equals("r0")) {
            sortOrder += "r.rating DESC";
        } else if (sort.substring(0, 2).equals("r1")) {
            sortOrder += "r.rating";
        }
        sortOrder += ", ";
        if (sort.substring(2).equals("t0")) {
            sortOrder += "m.title DESC";
        } else if (sort.substring(2).equals("t1")) {
            sortOrder += "m.title";
        } else if (sort.substring(2).equals("r0")) {
            sortOrder += "r.rating DESC";
        } else if (sort.substring(2).equals("r1")) {
            sortOrder += "r.rating";
        }
        return sortOrder;
    }

    private String getInputQuery(String input) {
        String inputQuery = "";
        if (input != null) {
            String[] inputArr = input.trim().split(":");
            if (inputArr[0].equals("genre")) {
                inputQuery += "g.name = '" + inputArr[1] + "'";
            } else if (inputArr[0].equals("alpha")) {
                if (inputArr[1].equals("*")) {
                    inputQuery += "m.title REGEXP '^[^a-zA-Z0-9]'";
                } else {
                    inputQuery += "m.title LIKE '" + inputArr[1] + "%' OR m.title LIKE '" + inputArr[1].toLowerCase() + "%' ";
                }
            } else {
                for (int i = 0; i < inputArr.length; i += 2) {
                    if (i != 0 && !inputArr[i].equals(" ")) {
                        inputQuery += " AND ";
                    }
                    if (inputArr[i].equals("title")) {
                        inputQuery += "m.title LIKE '%" + inputArr[i + 1] + "%' COLLATE utf8mb4_general_ci ";
                    } else if (inputArr[i].equals("year")) {
                        inputQuery += "m.year ='" + inputArr[i + 1] + "' ";
                    } else if (inputArr[i].equals("director")) {
                        inputQuery += "m.director LIKE '%" + inputArr[i + 1] + "%' COLLATE utf8mb4_general_ci ";
                    } else if (inputArr[i].equals("name")) {
                        inputQuery += "s.name LIKE '%" + inputArr[i + 1] + "%' COLLATE utf8mb4_general_ci ";
                    }
                }
            }
        }
        return inputQuery;
    }

    private String buildQuery(String numberPerPage, String pageNumber, String sortOrder, String inputQuery) {
        return "WITH temp(id) AS (\n" +
                "SELECT m.id\n" +
                "FROM movies m LEFT JOIN ratings r ON m.id = r.movieId " +
                "JOIN genres_in_movies gim ON m.id = gim.movieId " +
                "JOIN genres g ON gim.genreId = g.id " +
                "JOIN stars_in_movies sim ON m.id = sim.movieId\n" +
                "JOIN stars s ON sim.starId = s.id\n" +
                "WHERE " + inputQuery +
                "ORDER BY " + sortOrder + ")\n" +
                "SELECT m.id as movieId, m.title, m.year, m.director, GROUP_CONCAT(distinct g.name SEPARATOR ',') AS genres, r.rating\n" +
                "FROM movies m\n" +
                "JOIN genres_in_movies gim ON m.id = gim.movieId\n" +
                "JOIN genres g ON gim.genreId = g.id\n" +
                "JOIN temp ON m.id = temp.id\n" +
                "LEFT JOIN ratings r ON r.movieId = m.id\n" +
                "GROUP BY m.id, m.title, m.year, m.director,  r.rating\n" +
                "ORDER BY " + sortOrder +
                " LIMIT " + numberPerPage + " OFFSET " + Integer.toString((Integer.parseInt(pageNumber) - 1) * Integer.parseInt(numberPerPage));
    }

    private void addGenresToMovieObject(JsonObject movieObject, String genresString) {
        String[] genres = genresString.split(",");
        for (int i = 0; i < 3; i++) {
            if (i < genres.length) {
                movieObject.addProperty("genre" + (i + 1), genres[i]);
            } else {
                movieObject.addProperty("genre" + (i + 1), "");
            }
        }
    }

    private void addStarsToMovieObject(JsonObject movieObject, String movieId, Connection conn) throws SQLException {
        String query = "WITH temp(id,name, dob) AS (\n" +
                "SELECT stars.id, stars.name, stars.birthYear\n" +
                "FROM stars\n" +
                "JOIN stars_in_movies ON stars.id = stars_in_movies.starId\n" +
                "WHERE stars_in_movies.movieId = ?)\n" +
                "\n" +
                "SELECT temp.name, temp.id, temp.dob, COUNT(stars_in_movies.movieId) as count \n" +
                "FROM temp JOIN stars_in_movies ON temp.id = stars_in_movies.starId\n" +
                "GROUP BY temp.name, temp.id, temp.dob\n" +
                "ORDER by COUNT(stars_in_movies.movieId) DESC, temp.name;";

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, movieId);
            ResultSet rs = statement.executeQuery();
            int counter = 1;
            while (rs.next() && counter <= 3) {
                String starId = rs.getString("id");
                String starName = rs.getString("name");
                String starDob = rs.getString("dob");
                String count = rs.getString("count");

                movieObject.addProperty("star" + counter, starName + "(" + count + ")");
                movieObject.addProperty("starId" + counter, starId);
                if (starDob == null) {
                    starDob = "N/A";
                }
                counter++;
            }
        }
    }


}
