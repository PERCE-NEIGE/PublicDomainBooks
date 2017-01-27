package thakker.kyle.publicdomainbooks;

import org.json.JSONObject;

/**
 * Created by Kyle on 1/16/2017.
 */

public class Book {
    private String id;
    private String title;
    private String[] authors;
    private String publishedDate;
    private String description;
    private int pageCount;
    private String thumbnail;
    private String downloadLink;
    private JSONObject jSONObject;

    public Book(JSONObject jSONObject, String id, String title, String[] authors, String publishedDate, String description, int pageCount, String thumbnail, String downloadLink) {
        this.jSONObject=jSONObject;
        this.id=id;
        this.title=title;
        this.authors=authors;
        this.publishedDate=publishedDate;
        this.description=description;
        this.pageCount=pageCount;
        this.thumbnail=thumbnail;
        this.downloadLink=downloadLink;
    }

    public JSONObject getJSONObject() {return jSONObject;}
    public String getId(){return id;}
    public String getTitle() {return title;}
    public String getPublishedDate() {return publishedDate;}
    public String [] getAuthors() {return authors;}
    public String getDescription(){return description;}
    public int getPageCount() {return pageCount;}
    public String getThumbnail() {return thumbnail;}
    public String getDownloadLink() {return downloadLink;}
}
