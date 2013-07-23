import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * This class uses the 'http://goo.gl/' service to shorten the provided URL.
 * Stolen from: http://www.glodde.com/blog/default.aspx?id=51&t=Java-Use-googl-URL-shorten-from-Java
 */
public class GoogleShortener implements URLShortener {

    /*
	 * Google URL Shortener API Key must be declared here.
	 * Create API Key here: https://code.google.com/apis/console
	 * and get the key here: https://code.google.com/apis/console#access
	 */
    final static String apiURL = "https://www.googleapis.com/urlshortener/v1/url?shortUrl=http://goo.gl/fbsS&key=AIzaSyDXmdjeMFOzIjz8n8AS0d031N6LQ86o2Ts";

    @Override
    public String shorten(String longURL) {
        String shortUrl = "";

        try {
            URLConnection conn = new URL(apiURL).openConnection();
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write("{\"longUrl\":\"" + longURL + "\"}");
            wr.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;

            while ((line = rd.readLine()) != null) {
                if (line.contains("id")) {
                    // I'm sure there's a more elegant way of parsing
                    // the JSON response, but this is quick/dirty =)
                    shortUrl = line.substring(8, line.length() - 2);
                    break;
                }
            }

            wr.close();
            rd.close();
        } catch (MalformedURLException ex) {
            ex.getMessage();
        } catch (IOException ex) {
            ex.getMessage();
        }

        System.out.println("Short URL: " + shortUrl);

        return shortUrl;
    }
}
