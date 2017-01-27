package thakker.kyle.publicdomainbooks;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.List;


/**
 * Created by Kyle on 1/16/2017.
 */

public class BookAdapter extends ArrayAdapter<Book> {
    private String thumbnailUrl;
    public BookAdapter(Context context, List<Book> books) {
        super(context,0,books);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView=convertView;
        if(listItemView==null) {
            listItemView= LayoutInflater.from(getContext()).inflate(R.layout.book_list_item,parent,false);
        }
        Book currentBook=getItem(position);

        TextView title=(TextView) listItemView.findViewById(R.id.title);
        title.setText(currentBook.getTitle());

        TextView author=(TextView) listItemView.findViewById(R.id.author);
        if(currentBook.getAuthors()==null||currentBook.getAuthors()[0]==null) {
            author.setText("Author Unknown");
        } else {
            String authors=currentBook.getAuthors()[0];
            for(int i=0;i<currentBook.getAuthors().length;i++) {
                if(!authors.contains(currentBook.getAuthors()[i])) {
                    authors += ", " + currentBook.getAuthors()[i];
                }
            }
            author.setText(authors);
        }

        ImageView thumbnailView=(ImageView)listItemView.findViewById(R.id.thumbnail);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext()).build();
        ImageLoader.getInstance().init(config);
        ImageLoader imageLoader = ImageLoader.getInstance();


        if(currentBook.getThumbnail()!=null&&!currentBook.getThumbnail().isEmpty()) {
            imageLoader.displayImage(currentBook.getThumbnail(), thumbnailView);
        }

        return listItemView;

    }
}
