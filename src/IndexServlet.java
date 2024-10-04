import com.google.gson.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;

import javax.naming.*;
import javax.sql.*;
import java.io.*;
import java.sql.*;

@WebServlet(name = "IndexServlet", urlPatterns = "/api/index")
public class IndexServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;
    private DataSource dataSource;

    @Override
    public void init(ServletConfig config) {
        try {
            InitialContext initialContext = new InitialContext();
            dataSource = (DataSource) initialContext.lookup("java:comp/env/jdbc/read");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            String query = "SELECT DISTINCT name FROM genres ORDER BY name ASC";
            statement = conn.prepareStatement(query);
            rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("genre", rs.getString("name"));
                jsonArray.add(jsonObject);
            }

            out.write(jsonArray.toString());
            response.setStatus(200);
        } catch (SQLException e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            request.getServletContext().log("Error:", e);
            response.setStatus(500);
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (statement != null) statement.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
            out.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String sortTitle = request.getParameter("sort_title");
        String sortYear = request.getParameter("sort_year");
        String sortDirector = request.getParameter("sort_director");
        String sortName = request.getParameter("sort_name");

        JsonObject jsonObject = new JsonObject();
        if (sortTitle != null) jsonObject.addProperty("sort_title", sortTitle);
        if (sortYear != null) jsonObject.addProperty("sort_year", sortYear);
        if (sortDirector != null) jsonObject.addProperty("sort_director", sortDirector);
        if (sortName != null) jsonObject.addProperty("sort_name", sortName);

        response.getWriter().write(jsonObject.toString());
    }
}
