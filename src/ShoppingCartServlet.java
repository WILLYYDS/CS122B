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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/shopping-cart")
public class ShoppingCartServlet extends HttpServlet {

    private static final long serialVersionUID = 2L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type
        HttpSession session = request.getSession();
        PrintWriter out = response.getWriter();

        JsonArray jsonArray = new JsonArray();

        JsonObject UrljsonObject = new JsonObject();
        String url = (String) session.getAttribute("movie_page");
        UrljsonObject.addProperty("movie_page", url);
        System.out.println("Movie page URL from session: " + url);
        jsonArray.add(UrljsonObject);

        // 添加购物车内容到响应中
        JsonObject cartJsonObject = new JsonObject();
        String cartData = (String) session.getAttribute("cart_data");
        cartJsonObject.addProperty("cart_data", cartData);
        System.out.println("Cart data from session: " + cartData);
        jsonArray.add(cartJsonObject);

        out.write(jsonArray.toString());
        response.setStatus(200);
    }
}
