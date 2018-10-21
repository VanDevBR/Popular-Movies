package br.com.vanilson.popularmovies.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * These utilities will be used to communicate with the weather servers.
 */
public final class NetworkUtils {

    public static final String MOVIES_URL = "http://api.themoviedb.org/3/movie/";
    public static final String API_KEY = "";
    public static final String IMG_URL = "http://image.tmdb.org/t/p/w500/";
    public static final String IMG_POSTER_URL = "http://image.tmdb.org/t/p/w1280/";


    public static String requestHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}