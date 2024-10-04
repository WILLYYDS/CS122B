import java.util.ArrayList;
import java.util.List;

public class Movie {

    private String directorName;
    private String movieTitle;
    private String movieYear;
    private List<String> categories;
    private String movieId;

    public Movie() {
        categories = new ArrayList<>();
    }

    public String getDirectorName() {
        return directorName;
    }
    public String getMovieId()
    {
        return movieId;
    }
    public void setDirectorName(String directorName) {
        this.directorName = directorName;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String  getMovieYear() {
        return movieYear;
    }

    public void setMovieYear(String movieYear) {
        this.movieYear = movieYear;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void addCategory(String category) {
        this.categories.add(category);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Movie Details - ");
        sb.append("Director:" + getDirectorName());
        sb.append(", ");
        sb.append("Title:" + getMovieTitle());
        sb.append(", ");
        sb.append("Year:" + getMovieYear());
        sb.append(", ");
        sb.append("Categories:" + String.join(", ", getCategories()));
        sb.append(", ");
        sb.append("ID: " + getMovieId());
        return sb.toString();
    }
}