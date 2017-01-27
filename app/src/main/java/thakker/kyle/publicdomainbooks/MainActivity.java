package thakker.kyle.publicdomainbooks;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import static android.R.attr.button;


public class MainActivity extends AppCompatActivity {
    private BookAdapter mAdapter;
    private boolean deleteState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Sets the delete mode to not be on
        deleteState=false;


        /*Searches through all the files in the this apps storage directory and stores all the text
          files, which contain JSON information in an arraylist
        */
        File folder = new File(getExternalFilesDir("/Books").toString());
        File[] allFiles = folder.listFiles();
        ArrayList<File> allJSONFiles=new ArrayList<File>();
        for (int i = 0; i < allFiles.length; i++) {
            int indexPeriod = allFiles[i].toString().lastIndexOf('.');
            String ext=allFiles[i].toString().substring(indexPeriod+1);
            if(ext.equals("txt")) {
                allJSONFiles.add(allFiles[i]);
            }

        }

        ArrayList<Book> books=new ArrayList<Book>();
        for (int i = 0; i < allJSONFiles.size(); i++) {

            //Tries to read in the JSON information from the txt file.
            String JSONtxt="";
            try {
                BufferedReader reader = new BufferedReader(new FileReader(allJSONFiles.get(i)));
                String line;
                while ((line = reader.readLine()) != null)
                {
                    JSONtxt+=line;
                }
            } catch (Exception e) {
                continue;
            }

            //Tries to create a JSONObject from the information from the txt file
            JSONObject currentBook;
            try {
                currentBook=new JSONObject(JSONtxt);
            } catch (Exception e) {
                continue;
            }


            //Tries to get the id of the book
            String id=null;
            try{
                id = currentBook.getString("id");
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Tries to create a JSONObject for the volume information section
            JSONObject volumeInfo=null;
            try {
                volumeInfo=currentBook.getJSONObject("volumeInfo");
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Tries to  the title
            String title=null;
            try {
                title=volumeInfo.getString("title");
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Declarations for variables that depend on volumeInfo  existing
            JSONArray authorsJSON;
            String[] authors=null;
            String publishedDate=null;
            String description=null;
            int pageCount=-1;
            JSONObject imageLinks;
            String thumbnail=null;


            if(volumeInfo!=null) {

                //Tries to get all the  authors
                try {
                    authorsJSON = volumeInfo.getJSONArray("authors");
                    authors = new String[authorsJSON.length()];
                    for (int j = 0; j < authorsJSON.length(); j++) {
                        authors[j] = authorsJSON.getString(j);
                    }
                } catch (JSONException e) {

                }

                //Tries date the book was published
                try {
                    publishedDate = volumeInfo.getString("publishedDate");
                } catch (JSONException e) {

                }

                //Tries to get the description of the book
                try {
                    description = volumeInfo.getString("description");
                } catch (JSONException e) {

                }

                //Tries to get the the page count
                try {
                    pageCount = volumeInfo.getInt("pageCount");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Tries to get the thumbnail for the book
                try {
                    imageLinks = volumeInfo.getJSONObject("imageLinks");
                    thumbnail = imageLinks.getString("thumbnail");
                } catch (JSONException e) {
                }
            }

            //Tries to get the JSONObject for the access information
            JSONObject accessInfo=null;
            try {
                accessInfo = currentBook.getJSONObject("accessInfo");
            } catch (Exception e) {

            }

            //Tries to get the download link of the pdf
            String downloadLink=null;
            try {
                downloadLink=accessInfo.getString("downloadLink");
            } catch (Exception e) {
            }

            //Creates a new book with all the information gathered and adds it to the array list
            Book book = new Book(currentBook, id,title,authors,publishedDate,description,pageCount,thumbnail,downloadLink);
            books.add(book);
        }

        //Updates the listView
        mAdapter=new BookAdapter(this, new ArrayList<Book>());
        mAdapter.addAll(books);
        ListView savedBooks=(ListView)findViewById(R.id.saved_books);
        savedBooks.setAdapter(mAdapter);
        savedBooks.setVisibility(View.VISIBLE);

        /*Launches the SavedBookActivity and sends over the information about the book clicked on
        */
        savedBooks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Book currentBook=mAdapter.getItem(position);
                Intent savedBookView= new Intent(MainActivity.this, SavedBookActivity.class);
                savedBookView.putExtra("JSONObject",currentBook.getJSONObject().toString());
                savedBookView.putExtra("id",currentBook.getId());
                savedBookView.putExtra("title",currentBook.getTitle());
                savedBookView.putExtra("authors",currentBook.getAuthors());
                savedBookView.putExtra("publishedDate",currentBook.getPublishedDate());
                savedBookView.putExtra("description",currentBook.getDescription());
                savedBookView.putExtra("pageCount",currentBook.getPageCount());
                savedBookView.putExtra("thumbnail",currentBook.getThumbnail());
                savedBookView.putExtra("webReaderLink",currentBook.getDownloadLink());
                startActivity(savedBookView);
            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_activity_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {

            /*A click on this button brings the user to the ativity from which they can search for
              more books
            */
            case R.id.add_books_icon:
                Intent intent=new Intent(MainActivity.this,BookSearch.class);
                startActivity(intent);
                return true;

            /*Clicking once on the delete icon allows the user to delete books by clicking on each
              book in the listView. The background should turn red to indicate that the delete mode
              is on. Clicking on the delete icon again should return the activity to its normal
              state.
             */
            case R.id.delete_icon:
                ListView savedBooks=(ListView)findViewById(R.id.saved_books);
                View root =findViewById(R.id.activity_main);

                //Delete mode was not previously on
                if(deleteState==false) {

                    //Background color set to red to indicate delete mode is now on
                    root.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

                    /*Changes the list view onItemClickListener so that when books are clicked on
                      they are deleted.
                    */
                    savedBooks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Book currentBook = mAdapter.getItem(position);
                            File pdf = new File(getExternalFilesDir("/Books"), currentBook.getTitle() + ": " + currentBook.getId() + ".pdf");
                            File thumbnail = new File(getExternalFilesDir("/Books"), currentBook.getTitle() + ": " + currentBook.getId() + " Thumbnail.png");
                            File txt = new File(getExternalFilesDir("/Books"), currentBook.getTitle() + ": " + currentBook.getId() + ".txt");

                            if (pdf.exists()) {
                                pdf.delete();
                            }

                            if (thumbnail.exists()) {
                                thumbnail.delete();
                            }

                            if (txt.exists()) {
                                txt.delete();
                            }

                            mAdapter.remove(currentBook);
                        }
                    });

                    //Delete mode set to true
                    deleteState = true;
                } else { //Delete mode was previously on

                    //Background color is set to white to indicate that delete mode if off
                    root.setBackgroundColor(getResources().getColor(android.R.color.white));

                    /*The listView onItemClickListener is reverted back to its original state, in
                    which clicking on a item brings you to the saved book activity.
                     */
                    savedBooks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        /*Launches the SavedBookActivity and sends over the information about the book clicked on
                         */
                            Book currentBook = mAdapter.getItem(position);
                            Intent savedBookView = new Intent(MainActivity.this, SavedBookActivity.class);
                            savedBookView.putExtra("JSONObject", currentBook.getJSONObject().toString());
                            savedBookView.putExtra("id", currentBook.getId());
                            savedBookView.putExtra("title", currentBook.getTitle());
                            savedBookView.putExtra("authors", currentBook.getAuthors());
                            savedBookView.putExtra("publishedDate", currentBook.getPublishedDate());
                            savedBookView.putExtra("description", currentBook.getDescription());
                            savedBookView.putExtra("pageCount", currentBook.getPageCount());
                            savedBookView.putExtra("thumbnail", currentBook.getThumbnail());
                            savedBookView.putExtra("webReaderLink", currentBook.getDownloadLink());
                            startActivity(savedBookView);
                        }
                    });

                    //Delete mode set to off
                    deleteState = false;
                }
            default:
                return false;
        }
    }
}
