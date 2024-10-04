import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SAXParserExample extends DefaultHandler {
    private List<Movie> movies;
    private List<List<String>> actors;
    private Map<String, List<String>> actorsInMovies;
    private String currentValue;
    private Movie currentMovie;
    private String director;
    private String movieId;
    private String firstName;
    private String lastName;
    private String stageName;

    private MovieCategories movieCategories;

    public SAXParserExample() {
        movies = new ArrayList<>();
        actorsInMovies = new HashMap<>();
        actors = new ArrayList<>();
    }

    public void run() {
        movieCategories = new MovieCategories();
        parseXmlFiles();
        printParsedData();
    }

    private void parseXmlFiles() {
        SAXParserFactory factory = SAXParserFactory.newInstance();

        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse("/home/ubuntu/cs122b-s24-diss/xml/mains243.xml", this);
            parser.parse("/home/ubuntu/cs122b-s24-diss/xml/casts124.xml", this);
            parser.parse("/home/ubuntu/cs122b-s24-diss/xml/actors63.xml", this);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    private void printParsedData() {
        // Print parsed data if needed
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        currentValue = "";
        if (qName.equalsIgnoreCase("film")) {
            currentMovie = new Movie();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        currentValue = new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        switch (qName.toLowerCase()) {
            case "dirname":
                director = currentValue;
                break;
            case "film":
                movies.add(currentMovie);
                currentMovie.setDirectorName(director);
                break;
            case "t":
                currentMovie.setMovieTitle(currentValue);
                break;
            case "year":
                currentMovie.setMovieYear(currentValue);
                break;
            case "cat":
                currentMovie.addCategory(movieCategories.getCategory(currentValue));
                break;
            case "fid":
                currentMovie.setMovieId(currentValue);
                break;
            case "f":
                movieId = currentValue;
                break;
            case "a":
                actorsInMovies.computeIfAbsent(movieId, k -> new ArrayList<>()).add(currentValue);
                break;
            case "stagename":
                stageName = currentValue;
                break;
            case "familyname":
                lastName = currentValue;
                break;
            case "firstname":
                firstName = currentValue;
                break;
            case "dob":
                actors.add(List.of(stageName, firstName + " " + lastName, currentValue));
                break;
        }
    }

    public List<List<String>> getActors() {
        return actors;
    }

    public Map<String, List<String>> getActorsInMoives() {
        return actorsInMovies;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public static void main(String[] args) {
        SAXParserExample parser = new SAXParserExample();
        parser.run();
        Insertion insertion = new Insertion();
        insertion.insertMovies(parser.getMovies());
        insertion.insertStars(parser.getActors());
        insertion.insertStarsInMovies(parser.getActorsInMoives());
        insertion.saveNotFoundDataToFile();
    }
}
