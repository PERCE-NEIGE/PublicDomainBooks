package thakker.kyle.publicdomainbooks;

import android.app.DownloadManager;
import android.app.DownloadManager.*;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;

public class DetailedBookViewActivity extends AppCompatActivity {
    private Book currentBook;
    private DownloadManager downloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_book_view);

        Intent intent=getIntent();

        /*Retrieves the information about the book that was clicked on and uses this information to
          construct a new Book object
         */
        try {
            currentBook = new Book(new JSONObject(intent.getStringExtra("JSONObject")), intent.getStringExtra("id"), intent.getStringExtra("title"), intent.getStringArrayExtra("authors"), intent.getStringExtra("publishedDate"), intent.getStringExtra("description"), intent.getIntExtra("pageCount", -1), intent.getStringExtra("thumbnail"), intent.getStringExtra("downloadLink"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Sets the label of the view to the current book title
        setTitle(currentBook.getTitle());

        //Updates the text view for the title
        String currentBookTitle=currentBook.getTitle();
        TextView title=(TextView)findViewById(R.id.title_detailed);
        if (currentBookTitle!=null&&currentBookTitle.length()>50) {
            currentBookTitle=currentBookTitle.substring(0,50);
            currentBookTitle+="...";
        }
        if(currentBookTitle==null||currentBookTitle.isEmpty()) {
            title.setText("Title Unavailable");
        } else {
            title.setText(currentBookTitle);
        }

        //Updates the text view for the authors
        String[] currentBooksAuthorsArray=currentBook.getAuthors();
        String currentBooksAuthors=null;
        if(currentBooksAuthorsArray!=null) {
            currentBooksAuthors = currentBooksAuthorsArray[0];
            for(int i=1;i<currentBooksAuthorsArray.length;i++) {
                currentBooksAuthors += ", " + currentBooksAuthorsArray[i];
            }
        }
        TextView author=(TextView)findViewById(R.id.author_detailed);
        if(currentBooksAuthors==null||currentBooksAuthors.isEmpty()) {
            author.setText("Author Unknown");
        } else {
            author.setText(currentBooksAuthors);
        }

        //Updates the text view for the page count
        int currentBookPageCount=currentBook.getPageCount();
        TextView pageCount=(TextView)findViewById(R.id.pageCount_detailed);
        if(currentBookPageCount==-1) {
            pageCount.setText("Page Count Unavailable");
        } else {
            pageCount.setText(currentBookPageCount + " Pages");
        }

        //Updates the text view for the date the book was published
        String currentBookPublishedDate=currentBook.getPublishedDate();
        TextView publishedDate=(TextView)findViewById(R.id.publishedDate_detailed);
        if(currentBookPublishedDate==null||currentBookPublishedDate.isEmpty()) {
            publishedDate.setText("Date Published Unavailable Unknown");
        } else {
            publishedDate.setText("Published in " + currentBookPublishedDate);
        }

        //Updates the text view for the book description
        String currentBookDescription=currentBook.getDescription();
        if (currentBookDescription!=null&&currentBookDescription.length()>350) {
            currentBookDescription=currentBookDescription.substring(0,350);
            currentBookDescription+="...";
        }
        TextView description=(TextView)findViewById(R.id.description_detailed);
        if(currentBookDescription==null||currentBookDescription.isEmpty()) {
            description.setText("Description Unavailable");
        } else {
            description.setText(currentBookDescription);
        }


        //Downloads the thumbnail and displays it
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
        ImageLoader imageLoader = ImageLoader.getInstance();
        final ImageView thumbnail=(ImageView) findViewById(R.id.thumbnail_detailed);
        if(currentBook.getThumbnail()!=null&&!currentBook.getThumbnail().isEmpty()) {
            imageLoader.displayImage(currentBook.getThumbnail(), thumbnail);
        }

        final Button saveButton=(Button) findViewById(R.id.save_button);

        /*Check to see if the pdf file of the book exists, indicating the
          book was already saved. If the book was not saved, then an on click listener is setup for
          the save button. When pressed, this button downloads the thumbnail and pdf files ands writes the JSON
          information to a text file. The file name for the text file should be "<book title>: <book
          id>.txt". The file name for the pdf file should be the same but with a .pdf extension.
          The file name for the thumbnail should be "<book title>: <book id> Thumbnail.png"
         */
        File fileToCheck = new File(getExternalFilesDir("/Books") + "/" +currentBook.getTitle() + ": " + currentBook.getId() + ".pdf");
        if(currentBook.getDownloadLink()==null) {
            saveButton.setText("Unavailable");
        } else if(fileToCheck.exists()) {
            saveButton.setText("SAVED");
        } else {
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloadManager=(DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);

                    //If the thumbnail exists, it is downloaded
                    if(currentBook.getThumbnail()!=null) {
                        Uri uri = Uri.parse(currentBook.getThumbnail());
                        DownloadManager.Request request = new DownloadManager.Request(uri);
                        try {
                            request.setDescription(currentBook.getAuthors()[0]);
                        } catch (Exception e) {
                            request.setDescription("Author Unavailable");
                        }
                        request.setTitle(currentBook.getTitle());
                        request.setDestinationInExternalFilesDir(DetailedBookViewActivity.this, "/Books", currentBook.getTitle() + ": " + currentBook.getId() + " Thumbnail.png");
                        downloadManager.enqueue(request);
                    }

                    //JSON information written
                    File jSONFile=new File(getExternalFilesDir("/Books"), currentBook.getTitle() + ": " + currentBook.getId() + ".txt");
                    try {
                        FileWriter fileWriter = new FileWriter(jSONFile);
                        fileWriter.append(currentBook.getJSONObject().toString());
                        fileWriter.flush();
                        fileWriter.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    /*WebView is created for the downoad link. User will probably have to fill in a
                    captcha*/

                    WebView webView=new WebView(DetailedBookViewActivity.this);
                    setContentView(webView);
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                    webView.setWebViewClient(new WebViewClient());
                    webView.setDownloadListener(new DownloadListener() {
                        @Override
                        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                            Request request=new Request(Uri.parse(url));
                            request.allowScanningByMediaScanner();
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            String namePDF=currentBook.getTitle() + ": " + currentBook.getId() + ".pdf";
                            request.setTitle(currentBook.getTitle());
                            request.setDescription(currentBook.getDescription());
                            request.setDestinationInExternalFilesDir(DetailedBookViewActivity.this, "/Books", namePDF);
                            downloadManager.enqueue(request);
                        }
                    });
                    webView.loadUrl(currentBook.getDownloadLink());

                    saveButton.setText("SAVED");
                    saveButton.setOnClickListener(null);

                }
            });
        }

    }



}
