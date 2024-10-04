import java.util.HashMap;
import java.util.Map;

public class MovieCategories {
    private static Map<String, String> movieCategories;

    public MovieCategories() {
        movieCategories = new HashMap<>();
        movieCategories.put("Susp", "Thriller");
        movieCategories.put("CnR", "Cops and Robbers");
        movieCategories.put("CnRb", "Cops and Robbers");
        movieCategories.put("Dram", "Drama");
        movieCategories.put("West", "Western");
        movieCategories.put("Myst", "Mystery");
        movieCategories.put("S.F.", "Sci-Fi");
        movieCategories.put("ScFi", "Sci-Fi");
        movieCategories.put("SciF", "Sci-Fi");
        movieCategories.put("Advt", "Adventure");
        movieCategories.put("Horr", "Horror");
        movieCategories.put("Romt", "Romantic");
        movieCategories.put("Comd", "Comedy");
        movieCategories.put("Musc", "Musical");
        movieCategories.put("Docu", "Documentary");
        movieCategories.put("Porn", "Pornography");
        movieCategories.put("Noir", "Black");
        movieCategories.put("BioP", "Biographical Picture");
        movieCategories.put("TV", "TV show");
        movieCategories.put("TVs", "TV series");
        movieCategories.put("TVm", "TV miniseries");
        movieCategories.put("Actn", "Action");
    }
    public String getCategory(String cat) {
        return movieCategories.get(cat);
    }
}