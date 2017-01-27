package thakker.kyle.publicdomainbooks;

import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;


public class BookSearch extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<List<Book>> {

    public static final String LOG_TAG=BookSearch.class.getName();
    private static String GOOGLE_BOOKS_REQUEST_URL= "https://www.googleapis.com/books/v1/volumes?q=";
    private static String apiKey="AIzaSyDvxPUA1v1JBoFNeu6kuyssplLu0JTET-Y";
    private static int BOOK_LOADER_ID = 1;
    private static BookAdapter mAdapter;
    ListView bookListView;
    Button searchButton;
    private String searchParamters;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_book);

        super.onResume();

        //Grabs the listView, button, and editText views
        bookListView = (ListView) findViewById(R.id.list);
        searchButton=(Button)findViewById(R.id.search_button);
        final EditText searchBar=(EditText)findViewById(R.id.search_bar);

        //Sets up the spinner
        Spinner spinner = (Spinner) findViewById(R.id.search_type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.search_type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //Initializes a search when the searchButton is clicked
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Clears the previous search
                if(mAdapter!=null) {
                    mAdapter.clear();
                }
                bookListView.setAdapter(mAdapter);

                //Grabs the entry into the searchBar
                searchParamters=searchBar.getText().toString();

                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected()) {
                    android.app.LoaderManager loaderManager = getLoaderManager();
                    loaderManager.initLoader(BOOK_LOADER_ID++, null, BookSearch.this);
                }

                bookListView.setVisibility(View.VISIBLE);
            }
        });

        mAdapter = new BookAdapter(this, new ArrayList<Book>());
        bookListView.setAdapter(mAdapter);

        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*Launches the detailedBookViewActivity for the current book and sends over the data
                  about the book
                 */
                Book currentBook=mAdapter.getItem(position);
                Intent detailedBookView= new Intent(BookSearch.this, DetailedBookViewActivity.class);
                detailedBookView.putExtra("JSONObject",currentBook.getJSONObject().toString());
                detailedBookView.putExtra("id",currentBook.getId());
                detailedBookView.putExtra("title",currentBook.getTitle());
                detailedBookView.putExtra("authors",currentBook.getAuthors());
                detailedBookView.putExtra("publishedDate",currentBook.getPublishedDate());
                detailedBookView.putExtra("description",currentBook.getDescription());
                detailedBookView.putExtra("pageCount",currentBook.getPageCount());
                detailedBookView.putExtra("thumbnail",currentBook.getThumbnail());
                detailedBookView.putExtra("downloadLink",currentBook.getDownloadLink());
                startActivity(detailedBookView);
            }
        });

    }


    @Override
    public void onRestart(){
       super.onRestart();
        bookListView.setAdapter(null);
    }


    @Override
    public Loader<List<Book>> onCreateLoader(int id, Bundle args) {

        String temp=searchParamters;

        //Splits the search parameters up using whitespace
        String[] searchTerms = temp.split(" ");

        //Grabs the  search type
        Spinner spinner=(Spinner)findViewById(R.id.search_type_spinner);
        String searchType=spinner.getSelectedItem().toString();

        String url="";
        if(searchType.equals("Author")) {
            url=GOOGLE_BOOKS_REQUEST_URL+"inauthor:";
        } else if(searchType.equals("Title")) {
            url = GOOGLE_BOOKS_REQUEST_URL + "intitle:";
        }
        for(int i=0;i<searchTerms.length;i++) {
            //The first term doesn't need the additional plus for the query
            if(i==0){
                url=url+searchTerms[i];
            } else {
                url=url+"+"+searchTerms[i];
            }
        }

        //free-ebooks part of query ensures that the books are in the public domain
        url=url+"&download=epub&filter=free-ebooks&key=" + apiKey;

        return new BookLoader(this, url);

    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> data) {
        mAdapter.addAll(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {
        mAdapter.clear();
    }
}
