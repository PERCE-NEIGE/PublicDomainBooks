package thakker.kyle.publicdomainbooks;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static thakker.kyle.publicdomainbooks.BookSearch.LOG_TAG;

/**
 * Created by Kyle on 1/16/2017.
 */

public final class QueryUtils {

    //Don't want an object of this class to be created
    private QueryUtils(){};

    //Parses the JSoN file, creating books from the data and adding them to the list that is returned
    private static List<Book> extractFeatureFromJson(String bookJSON) {
        if (TextUtils.isEmpty(bookJSON)) {
            return null;
        }

        List<Book> books = new ArrayList<>();
        try {

            //Tries to get the array of JSON objects representing books
            JSONObject baseJsonResponse = new JSONObject(bookJSON);
            JSONArray booksArray;
            try {
                booksArray = baseJsonResponse.getJSONArray("items");
            } catch (JSONException e) {
                return null;
            }


            for (int i = 0; i < booksArray.length(); i++) {

                JSONObject currentBook = booksArray.getJSONObject(i);

                //Gets the id of the book
                String id = currentBook.getString("id");

                JSONObject volumeInfo=currentBook.getJSONObject("volumeInfo");

                //Gets the title of the book
                String title=volumeInfo.getString("title");

                //Tries to get the authors of the book
                JSONArray authorsJSON;
                String[] authors;
                try {
                    authorsJSON = volumeInfo.getJSONArray("authors");
                    authors = new String[authorsJSON.length()];
                    for (int j = 0; j < authorsJSON.length(); j++) {
                        authors[j] = authorsJSON.getString(j);
                    }
                } catch (JSONException e) {
                    authors=null; //no authors provided
                }

                //Tries to get the date the book was published
                String publishedDate;
                try {
                    publishedDate=volumeInfo.getString("publishedDate");
                } catch (JSONException e) {
                    publishedDate=null; //no published date provided
                }

                //Tries to get the description of the book
                String description;
                try {
                    description=volumeInfo.getString("description");
                }catch (JSONException e) {
                    description=null;//no description provided
                }

                //Tries to get the page count
                int pageCount;
                try{
                    pageCount=volumeInfo.getInt("pageCount");
                } catch (JSONException e) {
                    pageCount=-1; //no page count provided
                }

                //Tries to get the thumbnail link
                JSONObject imageLinks;
                String thumbnail;
                try {
                    imageLinks=volumeInfo.getJSONObject("imageLinks");
                    thumbnail=imageLinks.getString("thumbnail");
                } catch (JSONException e) {
                    thumbnail=null; //no thumbnail null
                }

                //Tries to get the pdf download link
                String downloadLink;
                try {
                    JSONObject accessInfo=currentBook.getJSONObject("accessInfo");
                    JSONObject pdf=accessInfo.getJSONObject("pdf");
                    downloadLink=pdf.getString("downloadLink");
                } catch (Exception e) {
                    downloadLink=null;
                }

                //Creates a book from all this information
                Book book = new Book(currentBook, id,title,authors,publishedDate,description,pageCount,thumbnail,downloadLink);
                books.add(book);
            }

        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the  JSON results", e);
        }


        return books;
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    //Tries to make a http connection and download the JSON file for the search
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();


            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the book JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    //fetches data from all the books in the json file
    public static List<Book> fetchBookData(String requestUrl) {

        URL url = createUrl(requestUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        List<Book> books = extractFeatureFromJson(jsonResponse);

        return books;
    }
}
