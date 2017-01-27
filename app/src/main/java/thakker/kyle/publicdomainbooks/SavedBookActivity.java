package thakker.kyle.publicdomainbooks;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;

public class SavedBookActivity extends AppCompatActivity {

    private Book currentBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_book);

        Intent intent=getIntent();

        /*Retrieves the information about the book that was clicked on and uses this information to
          construct a new Book object
         */
        try {
            currentBook = new Book(new JSONObject(intent.getStringExtra("JSONObject")), intent.getStringExtra("id"), intent.getStringExtra("title"), intent.getStringArrayExtra("authors"), intent.getStringExtra("publishedDate"), intent.getStringExtra("description"), intent.getIntExtra("pageCount", -1), intent.getStringExtra("thumbnail"), intent.getStringExtra("webReaderLink"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Sets the label of the view to the current book title
        setTitle(currentBook.getTitle());

        //Updates the text view for the title
        String currentBookTitle=currentBook.getTitle();
        TextView title=(TextView)findViewById(R.id.title_saved);
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
        TextView author=(TextView)findViewById(R.id.author_saved);
        if(currentBooksAuthors==null||currentBooksAuthors.isEmpty()) {
            author.setText("Author Unknown");
        } else {
            author.setText(currentBooksAuthors);
        }

        //Updates the text view for the page count
        int currentBookPageCount=currentBook.getPageCount();
        TextView pageCount=(TextView)findViewById(R.id.pageCount_saved);
        if(currentBookPageCount==-1) {
            pageCount.setText("Page Count Unavailable");
        } else {
            pageCount.setText(currentBookPageCount + " Pages");
        }

        //Updates the text view for the date the book was published
        String currentBookPublishedDate=currentBook.getPublishedDate();
        TextView publishedDate=(TextView)findViewById(R.id.publishedDate_saved);
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
        TextView description=(TextView)findViewById(R.id.description_saved);
        if(currentBookDescription==null||currentBookDescription.isEmpty()) {
            description.setText("Description Unavailable");
        } else {
            description.setText(currentBookDescription);
        }


        //Uploads the thumbnail if it exists and displays it
        final ImageView thumbnail=(ImageView) findViewById(R.id.thumbnail_saved);
        try {
            File thumbnailFile = new File(getExternalFilesDir("/Books"), currentBook.getTitle() + ": " + currentBook.getId() + " Thumbnail.png");
            thumbnail.setImageURI(Uri.fromFile(thumbnailFile));
        } catch (Exception e) {
        }


        final Button readButton=(Button) findViewById(R.id.read_button);
        File pdf = new File(getExternalFilesDir("/Books"), currentBook.getTitle() + ": " + currentBook.getId() + ".pdf");

        //If for some reason the pdf file is missing "File Not Downloaded" is displayed
        if(!pdf.exists()) {
            readButton.setText("File Not Downloaded");
        } else {

            /* A onClickListener is set up for the read button. When clicked on the pdf is uploaded
                and a pdf intent is sent out. A pdf reader must be installed to read the book. If no
                app capable of reading a pdf is installed, a toast will be displayed telling the
                user to download a pdf reader.
             */
        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    File file = new File(getExternalFilesDir("/Books"), currentBook.getTitle() + ": " + currentBook.getId() + ".pdf");
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), "application/pdf");
                    startActivity(intent);
                } catch (Exception e) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please download a pdf reader!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}

