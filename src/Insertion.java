import com.mysql.cj.util.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

public class Insertion {
    private final Set<String> notFoundMovies = new HashSet<>();
    private final Set<String> notFoundStars = new HashSet<>();
    private final Set<String[]> badData = new HashSet<>();

    private static final String LOGIN_USER = "mytestuser";
    private static final String LOGIN_PASSWORD = "My6$Password";
    private static final String LOGIN_URL = "jdbc:mysql://localhost:3306/moviedb";

    public Insertion() {
        initializeDriver();
    }

    private void initializeDriver() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertMovies(List<Movie> movies) {
        int movieCount = movies.size();
        System.out.println("This dataset contains " + movieCount + " movies.");

        String[] sqls = {
                "SELECT count(*) FROM movies WHERE id = ? OR title = ?",
                "INSERT INTO movies (id, title, year, director) VALUES (?, ?, ?, ?)",
                "SELECT id FROM genres WHERE name = ?",
                "INSERT INTO genres (name) VALUES (?)",
                "INSERT INTO genres_in_movies (genreId, movieId) VALUES (?, ?)"
        };

        try (Connection conn = DriverManager.getConnection(LOGIN_URL, LOGIN_USER, LOGIN_PASSWORD)) {
            conn.setAutoCommit(false);

            try (PreparedStatement checkMovieStmt = conn.prepareStatement(sqls[0]);
                 PreparedStatement insertMovieStmt = conn.prepareStatement(sqls[1]);
                 PreparedStatement checkGenreStmt = conn.prepareStatement(sqls[2]);
                 PreparedStatement insertGenreStmt = conn.prepareStatement(sqls[3], Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement insertGenreInMovieStmt = conn.prepareStatement(sqls[4])) {

                Set<String> movieIdsInBatch = new HashSet<>();
                int[] counts = {0, 0, 0};

                for (Movie movie : movies) {
                    if (isInvalidMovie(movie, movieIdsInBatch)) {
                        continue;
                    }

                    checkMovieStmt.setString(1, movie.getMovieId());
                    checkMovieStmt.setString(2, movie.getMovieTitle());
                    ResultSet movieRs = checkMovieStmt.executeQuery();

                    if (movieRs.next() && movieRs.getInt(1) == 0) {
                        insertMovieData(movie, insertMovieStmt, movieIdsInBatch);
                        insertGenreData(movie, checkGenreStmt, insertGenreStmt, insertGenreInMovieStmt, checkMovieStmt);
                        counts[0]++;
                        counts[1] += movie.getCategories().size();
                    } else {
                        badData.add(new String[]{" contains inconsistent data", " id " + movie.getMovieId()});
                    }
                }

                insertMovieStmt.executeBatch();
                conn.commit(); // 先提交电影数据的插入
                insertGenreInMovieStmt.executeBatch();
                conn.commit();

                printInsertionResults(counts[1], counts[0], movieIdsInBatch.size(), counts[2]);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private boolean isInvalidMovie(Movie movie, Set<String> movieIdsInBatch) {
        String movieId = movie.getMovieId();
        String movieTitle = movie.getMovieTitle();

        if (StringUtils.isNullOrEmpty(movieId)) {
            badData.add(new String[]{" don't have an ID", " title " + movieTitle});
            return true;
        }
        if (movieIdsInBatch.contains(movieId)) {
            badData.add(new String[]{" duplicated", " id " + movieId});
            return true;
        }
        if (movieTitle.equals("")) {
            badData.add(new String[]{" has no title", " id " + movieId});
            return true;
        }
        return false;
    }

    private void insertMovieData(Movie movie, PreparedStatement insertMovieStmt, Set<String> movieIdsInBatch) throws SQLException {
        insertMovieStmt.setString(1, movie.getMovieId());
        insertMovieStmt.setString(2, movie.getMovieTitle());

        try {
            insertMovieStmt.setInt(3, Integer.parseInt(movie.getMovieYear()));
        } catch (NumberFormatException e) {
            badData.add(new String[]{" has invalid year", " id " + movie.getMovieId()});
            return;
        }

        insertMovieStmt.setString(4, movie.getDirectorName());
        insertMovieStmt.addBatch();
        movieIdsInBatch.add(movie.getMovieId());
    }

    private void insertGenreData(Movie movie, PreparedStatement checkGenreStmt, PreparedStatement insertGenreStmt,
                                 PreparedStatement insertGenreInMovieStmt, PreparedStatement checkMovieStmt) throws SQLException {
        for (String genreName : movie.getCategories()) {
            if (StringUtils.isNullOrEmpty(genreName)) {
                continue;
            }

            int genreId = getOrInsertGenre(genreName, checkGenreStmt, insertGenreStmt);

            // 检查电影 ID 是否存在于 movies 表中
            checkMovieStmt.setString(1, movie.getMovieId());
            ResultSet movieRs = checkMovieStmt.executeQuery();

            if (movieRs.next() && movieRs.getInt(1) > 0) {
                insertGenreInMovieStmt.setInt(1, genreId);
                insertGenreInMovieStmt.setString(2, movie.getMovieId());
                insertGenreInMovieStmt.addBatch();
            }
        }
    }

    private int getOrInsertGenre(String genreName, PreparedStatement checkGenreStmt, PreparedStatement insertGenreStmt) throws SQLException {
        checkGenreStmt.setString(1, genreName);
        ResultSet genreRs = checkGenreStmt.executeQuery();

        if (genreRs.next()) {
            return genreRs.getInt(1);
        } else {
            insertGenreStmt.setString(1, genreName);
            insertGenreStmt.executeUpdate();
            ResultSet generatedKeys = insertGenreStmt.getGeneratedKeys();
            return generatedKeys.next() ? generatedKeys.getInt(1) : 0;
        }
    }

    private void printInsertionResults(int genreCount, int genreMovieCount, int movieCount, int duplicateCount) {
        System.out.println("Successfully inserted " + genreCount + " genres.");
        System.out.println("Successfully inserted " + genreMovieCount + " in genres_in_movies.");
        System.out.println("Successfully inserted " + movieCount + " movies.");
        System.out.println(duplicateCount + " movies contains duplicate.");
    }

    public void insertStars(List<List<String>> actors) {
        String checkSql = "SELECT count(*) FROM stars WHERE name = ? OR name = ?";
        String insertSql = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(LOGIN_URL, LOGIN_USER, LOGIN_PASSWORD);
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            conn.setAutoCommit(false);
            int counter = 0;
            int actorCount = actors.size();
            System.out.println("This dataset contains " + actorCount + " stars.");

            for (List<String> actor : actors) {
                if (isEmptyActor(actor)) {
                    System.out.println("Actor has no Name");
                    continue;
                }

                if (isNewActor(actor, checkStmt)) {
                    insertActorData(actor, insertStmt, counter);
                    counter++;
                }
            }

            insertStmt.executeBatch();
            conn.commit();
            System.out.println("Successfully inserted " + counter + " stars.");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private boolean isEmptyActor(List<String> actor) {
        return actor.get(0).equals("") && actor.get(1).equals("");
    }

    private boolean isNewActor(List<String> actor, PreparedStatement checkStmt) throws SQLException {
        checkStmt.setString(1, actor.get(0));
        checkStmt.setString(2, actor.get(1));
        ResultSet rs = checkStmt.executeQuery();
        return rs.next() && rs.getInt(1) == 0;
    }

    private void insertActorData(List<String> actor, PreparedStatement insertStmt, int counter) throws SQLException {
        insertStmt.setString(1, String.valueOf(counter));
        insertStmt.setString(2, actor.get(1).equals("") ? actor.get(0) : actor.get(1));

        if (actor.get(2).equals("") || actor.get(2).equalsIgnoreCase("n.a.")) {
            insertStmt.setNull(3, java.sql.Types.INTEGER);
        } else {
            try {
                insertStmt.setInt(3, Integer.parseInt(actor.get(2)));
            } catch (NumberFormatException e) {
                insertStmt.setNull(3, java.sql.Types.INTEGER);
            }
        }

        insertStmt.addBatch();
    }

    public void insertStarsInMovies(Map<String, List<String>> starsInMovies) {
        String checkMovieSql = "SELECT count(*) FROM movies WHERE id = ?";
        String checkStarSql = "SELECT id FROM stars WHERE name = ?";
        String insertStarInMovieSql = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(LOGIN_URL, LOGIN_USER, LOGIN_PASSWORD);
             PreparedStatement checkMovieStmt = conn.prepareStatement(checkMovieSql);
             PreparedStatement checkStarStmt = conn.prepareStatement(checkStarSql);
             PreparedStatement insertStarInMovieStmt = conn.prepareStatement(insertStarInMovieSql)) {

            int count = 0;
            conn.setAutoCommit(false);

            for (Map.Entry<String, List<String>> entry : starsInMovies.entrySet()) {
                String movieId = entry.getKey();
                List<String> actorNames = entry.getValue();

                if (notFoundMovies.contains(movieId)) {
                    continue;
                }

                if (isValidMovie(movieId, checkMovieStmt)) {
                    for (String actorName : actorNames) {
                        if (notFoundStars.contains(actorName)) {
                            continue;
                        }

                        String starId = getStarId(actorName, checkStarStmt);
                        if (starId != null) {
                            insertStarInMovie(starId, movieId, insertStarInMovieStmt);
                        } else {
                            notFoundStars.add(actorName);
                        }
                    }
                    count++;
                } else {
                    notFoundMovies.add(movieId);
                }
            }

            insertStarInMovieStmt.executeBatch();
            conn.commit();
            System.out.println("Successfully inserted " + count + " in stars_in_movies.");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private boolean isValidMovie(String movieId, PreparedStatement checkMovieStmt) throws SQLException {
        checkMovieStmt.setString(1, movieId);
        ResultSet movieRs = checkMovieStmt.executeQuery();
        return movieRs.next() && movieRs.getInt(1) > 0;
    }

    private String getStarId(String actorName, PreparedStatement checkStarStmt) throws SQLException {
        checkStarStmt.setString(1, actorName);
        ResultSet starRs = checkStarStmt.executeQuery();
        return starRs.next() ? starRs.getString(1) : null;
    }

    private void insertStarInMovie(String starId, String movieId, PreparedStatement insertStarInMovieStmt) throws SQLException {
        insertStarInMovieStmt.setString(1, starId);
        insertStarInMovieStmt.setString(2, movieId);
        insertStarInMovieStmt.addBatch();
    }

    public void saveNotFoundDataToFile() {
        int notFoundMovieCount = notFoundMovies.size();
        int notFoundStarCount = notFoundStars.size();
        int badDataCount = badData.size();

        System.out.println(notFoundMovieCount + " movies not found.");
        System.out.println(notFoundStarCount + " stars not found.");
        System.out.println(badDataCount + " inconsistent.");

        String filePath = "/home/ubuntu/cs122b-s24-diss/xml/errorReport.txt";

        try (PrintWriter writer = new PrintWriter(filePath, "UTF-8")) {
            writeErrorData(writer, badData, notFoundMovies, notFoundStars);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeErrorData(PrintWriter writer, Set<String[]> badData, Set<String> notFoundMovies, Set<String> notFoundStars) {
        if (badData != null) {
            for (String[] bad : badData) {
                writer.println("Movie" + bad[1] + bad[0]);
            }
        }

        if (notFoundMovies != null) {
            for (String movieId : notFoundMovies) {
                writer.println("Movie id " + movieId + " not found.");
            }
        }

        if (notFoundStars != null) {
            for (String starId : notFoundStars) {
                writer.println("Star name " + starId + " not found.");
            }
        }
    }
}
