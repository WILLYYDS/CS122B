import com.google.gson.Gson;
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
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "MovieSearchServlet", urlPatterns = "/api/full-text-search")
public class MovieSearchServlet extends HttpServlet {
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
        String query = request.getParameter("query");
        boolean fuzzy = Boolean.parseBoolean(request.getParameter("fuzzy"));

        List<Movie> movies = searchMovies(query, fuzzy);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new Gson().toJson(movies));
    }

    private List<Movie> searchMovies(String query, boolean fuzzy) {
        List<Movie> movies = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            String sql;
            if (fuzzy) {
                sql = "SELECT id, title FROM movies WHERE MATCH(title) AGAINST (? IN BOOLEAN MODE) OR title LIKE ? OR edth(title, ?, 2) LIMIT 10";
            } else {
                sql = "SELECT id, title FROM movies WHERE MATCH(title) AGAINST (? IN BOOLEAN MODE) LIMIT 10";
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                String[] keywords = query.trim().split("\\s+");
                StringBuilder searchQuery = new StringBuilder();
                for (String keyword : keywords) {
                    searchQuery.append("+").append(keyword).append("* ");
                }
                ps.setString(1, searchQuery.toString());
                if (fuzzy) {
                    ps.setString(2, "%" + query + "%");
                    ps.setString(3, query);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String movieId = rs.getString("id");
                        String movieTitle = rs.getString("title");
                        movies.add(new Movie(movieId, movieTitle));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return movies;
    }

    static class Movie {
        private String id;
        private String title;

        public Movie(String id, String title) {
            this.id = id;
            this.title = title;
        }


    }
}
