import com.mysql.cj.util.StringUtils;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

public class inser {
    private DataSource dataSource;
    private Set<String> notFoundMovies;
    private Set<String> notFoundStars;
    private Set<String[]> badData;
    private String loginUser = "mytestuser";
    private String loginPasswd = "My6$Password";
    private String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

    public inser() {

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            notFoundMovies = new HashSet<>();
            notFoundStars = new HashSet<>();
            badData = new HashSet<>();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    public void insertMovies(List<Movie> movies) {
        System.out.println("This dataset contains " + movies.size() + " movies.");
        try (Connection conn = DriverManager.getConnection(loginUrl, loginUser, loginPasswd)) {
            conn.setAutoCommit(false);

            String checkMovieSql = "SELECT count(*) FROM movies WHERE id = ? OR title = ? ";
            String insertMovieSql = "INSERT INTO movies (id, title, year, director) VALUES (?, ?, ?, ?)";
            String checkGenreSql = "SELECT id FROM genres WHERE name = ?";
            String insertGenreSql = "INSERT INTO genres (name) VALUES (?)";
            String insertGenreInMovieSql = "INSERT INTO genres_in_movies (genreId, movieId) VALUES (?, ?)";

            PreparedStatement checkMovieStmt = conn.prepareStatement(checkMovieSql);
            PreparedStatement insertMovieStmt = conn.prepareStatement(insertMovieSql);
            PreparedStatement checkGenreStmt = conn.prepareStatement(checkGenreSql);
            PreparedStatement insertGenreStmt = conn.prepareStatement(insertGenreSql, Statement.RETURN_GENERATED_KEYS);
            PreparedStatement insertGenreInMovieStmt = conn.prepareStatement(insertGenreInMovieSql);

            Set<String> movieIdsInBatch = new HashSet<>();
            int count = 0;
            int genre_count = 0;
            int duplicate = 0;
            for (Movie movie : movies) {
                if (StringUtils.isNullOrEmpty(movie.getMovieId())){
                    badData.add(new String[]{" don't have an ID", " title " + movie.getMovieTitle()});
                    continue;
                }
                if (movieIdsInBatch.contains(movie.getMovieId())){
                    badData.add(new String[]{" duplicated", " id " + movie.getMovieId()});
                    duplicate++;
                    continue;
                }
                if (movie.getMovieTitle().equals("")){
                    badData.add(new String[]{" has no title", " id " + movie.getMovieId()});
                    continue;
                }

                checkMovieStmt.setString(1, movie.getMovieId());
                checkMovieStmt.setString(2, movie.getMovieTitle());
                ResultSet movieRs = checkMovieStmt.executeQuery();

                if (movieRs.next() && movieRs.getInt(1) == 0) {
                    insertMovieStmt.setString(1, movie.getMovieId());
                    insertMovieStmt.setString(2, movie.getMovieTitle());

                    try {
                        insertMovieStmt.setInt(3, Integer.parseInt(movie.getMovieYear()));
                    } catch (NumberFormatException e) {
                        badData.add(new String[]{" has invalid year", " id " + movie.getMovieId()});
                        continue;
                    }

                    insertMovieStmt.setString(4, movie.getDirectorName());
                    insertMovieStmt.addBatch();
                    movieIdsInBatch.add(movie.getMovieId());

                    for (String genreName : movie.getCategories()) {
                        if (StringUtils.isNullOrEmpty(genreName)) {
                            continue;
                        }
                        checkGenreStmt.setString(1, genreName);
                        ResultSet genreRs = checkGenreStmt.executeQuery();

                        int genreId = 0;
                        if (genreRs.next()) {
                            genreId = genreRs.getInt(1);
                        } else {
                            insertGenreStmt.setString(1, genreName);
                            insertGenreStmt.executeUpdate();
                            ResultSet generatedKeys = insertGenreStmt.getGeneratedKeys();
                            if (generatedKeys.next()) {
                                genreId = generatedKeys.getInt(1);
                            }
                            genre_count ++;
                        }

                        insertGenreInMovieStmt.setInt(1, genreId);
                        insertGenreInMovieStmt.setString(2, movie.getMovieId());
                        insertGenreInMovieStmt.addBatch();
                        count ++;
                    }
                } else {
//                    System.out.println("Bad data id: " + movie.getMovieId());
                    badData.add(new String[]{" contains inconsistent data", " id " + movie.getMovieId()});
                }
            }
            insertMovieStmt.executeBatch();
            insertGenreInMovieStmt.executeBatch();
            conn.commit();
            System.out.println("Successfully inserted " + genre_count + " genres.");
            System.out.println("Successfully inserted " + count + " in genres_in_movies.");
            System.out.println("Successfully inserted " + movieIdsInBatch.size() + " movies.");
            System.out.println(duplicate + " movies contains duplicate.");
        } catch (Exception e) {
            System.out.println(e);
        }
    }



    public void insertStars(List<List<String>> actors) {
        try (Connection conn = DriverManager.getConnection(loginUrl, loginUser, loginPasswd)) {
            conn.setAutoCommit(false); // Turn off auto-commit for batch processing

            String checkSql = "SELECT count(*) FROM stars WHERE name = ? OR name = ?";
            String insertSql = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            int counter = 0;
            System.out.println("This dataset contains " + actors.size() + " stars.");
            for (List<String> actor : actors) {
                // If both fields are empty, skip this iteration
                if (actor.get(0).equals("") && actor.get(1).equals("")) {
                    System.out.println("Actor has no Name");
                    continue;
                }
                checkStmt.setString(1, actor.get(0));
                checkStmt.setString(2, actor.get(1));
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next() && rs.getInt(1) == 0) {  // If star not found in database
                    insertStmt.setString(1, String.valueOf(counter));
                    if (actor.get(1).equals("")) {
                        insertStmt.setString(2, actor.get(0));
                    } else {
                        insertStmt.setString(2, actor.get(1));
                    }
                    if (actor.get(2).equals("") || actor.get(2).equalsIgnoreCase("n.a.")) {
                        insertStmt.setNull(3, Types.INTEGER);
                    } else {
                        try {
                            insertStmt.setInt(3, Integer.parseInt(actor.get(2)));
                        } catch (NumberFormatException e) {
                            insertStmt.setNull(3, Types.INTEGER);
                        }
                    }
                    insertStmt.addBatch();
                    counter++;
                }
            }

            insertStmt.executeBatch();
            conn.commit(); // Commit all the changes after successful execution of all batches

            System.out.println("Successfully inserted " + counter + " stars.");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void insertStarsInMovies(Map<String, List<String>> starsInMovies) {
        try (Connection conn = DriverManager.getConnection(loginUrl, loginUser, loginPasswd)) {
            int count = 0;
            conn.setAutoCommit(false); // Turn off auto-commit for batch processing

            String checkMovieSql = "SELECT count(*) FROM movies WHERE id = ?";
            String checkStarSql = "SELECT id FROM stars WHERE name = ?";
            String insertStarInMovieSql = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?)";

            PreparedStatement checkMovieStmt = conn.prepareStatement(checkMovieSql);
            PreparedStatement checkStarStmt = conn.prepareStatement(checkStarSql);
            PreparedStatement insertStarInMovieStmt = conn.prepareStatement(insertStarInMovieSql);

            for (Map.Entry<String, List<String>> entry : starsInMovies.entrySet()) {
                String movieId = entry.getKey();
                List<String> actorNames = entry.getValue();
                if (notFoundMovies.contains(movieId)) {
                    continue;
                }
                // Check if movie exists in the database
                checkMovieStmt.setString(1, movieId);
                ResultSet movieRs = checkMovieStmt.executeQuery();

                if (movieRs.next() && movieRs.getInt(1) > 0) { // If movie exists in database
                    for (String actorName : actorNames) {
                        if (notFoundStars.contains(actorName)) {
                            continue;
                        }
                        checkStarStmt.setString(1, actorName);
                        ResultSet starRs = checkStarStmt.executeQuery();
                        if (starRs.next()) { // If star exists in database
                            String starId = starRs.getString(1);

                            // Add to batch for stars_in_movies
                            insertStarInMovieStmt.setString(1, starId);
                            insertStarInMovieStmt.setString(2, movieId);
                            insertStarInMovieStmt.addBatch();
                        } else {
//                            System.out.println("Actor not found: " + actorName);
                            notFoundStars.add(actorName);
                        }
                    }
                    count ++;
                } else {
//                    System.out.println("Movie not found: " + movieId);
                    notFoundMovies.add(movieId);
                }
            }
            insertStarInMovieStmt.executeBatch();
            conn.commit(); // Commit all the changes after successful execution of all batches
            System.out.println("Successfully inserted " + count + " in stars_in_movies.");
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    public void saveNotFoundDataToFile() {
        System.out.println(notFoundMovies.size() + " movies not found.");
        System.out.println(notFoundStars.size() + " stars not found.");
        System.out.println(badData.size() + " inconsistent.");
        try {
            PrintWriter writer = new PrintWriter("/E/cs122b-s24-diss-main - copy/xml/errorReport.txt", "UTF-8");

            if (badData != null) {
//            writer.println("\nBad data: ");
                for (String[] bad : badData) {
                    writer.println("Movie" + bad[1] + bad[0]);
                }}

            if (notFoundMovies != null){
//            writer.println("Movies not found: ");
                for (String movieId : notFoundMovies) {
                    writer.println("Movie id " + movieId + " not found.");
                }}

            if (notFoundStars != null) {
//                writer.println("\nStars not found: ");
                for (String starId : notFoundStars) {
                    writer.println("Star name " + starId + " not found.");
                }
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}


