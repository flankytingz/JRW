package me.Flanked.JRW;

import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Subreddit {

    private static final Logger logger = LoggerFactory.getLogger(Subreddit.class);

    private final String access_token;

    // Subreddit data
    private final String subredditName;
    private int accounts_active;
    private String description;
    private String display_name;
    private String header_img;
    private String header_title;
    private boolean over18;
    private String public_description;
    private boolean public_traffic;
    private long subscribers;
    private String submission_type;
    private String subreddit_type;
    private String title;
    private String url;

    protected static Subreddit getSubredditByName (String name, String access_token) {
        return new Subreddit(name, access_token);
    }

    private Subreddit (String subredditName, String access_token) {
        this.subredditName = subredditName;
        this.access_token = access_token;
        JSONObject data = Networker.getSubredditData(this.subredditName, this.access_token);
        setData(data);
    }

    private void setData (JSONObject data) {
        this.accounts_active = data.getInt("accounts_active");
        this.description = data.getString("description");
        this.display_name = data.getString("display_name");
        this.header_img = data.getString("header_img");
        this.header_title = data.getString("header_title");
        this.over18 = data.getBoolean("over18");
        this.public_description = data.getString("public_description");
        this.public_traffic = data.getBoolean("public_traffic");
        this.subscribers = data.getLong("subscribers");
        this.submission_type = data.getString("submission_type");
        this.subreddit_type = data.getString("subreddit_type");
        this.title = data.getString("title");
        this.url = "https://www.reddit.com" + data.getString("url");
    }

    public List<Submission> getSubmissions (int limit) {
        JSONArray array = Networker.getSubmissions(this, limit, this.access_token);
        List<Submission> list = new ArrayList<>();
        array.forEach(object -> {
            final JSONObject data = (JSONObject) object;
            list.add(Submission.getSubmissionByData(data));
        });
        return list;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getSubredditType() {
        return subreddit_type;
    }

    public String getSubmissionType() {
        return submission_type;
    }

    public long getSubscribers() {
        return subscribers;
    }

    public boolean isPublicTraffic() {
        return public_traffic;
    }

    public String getPublicDescription() {
        return public_description;
    }

    public boolean isOver18() {
        return over18;
    }

    public String getHeaderTitle() {
        return header_title;
    }

    public String getHeaderIMG() {
        return header_img;
    }

    public String getDisplayName() {
        return display_name;
    }

    public String getDescription() {
        return description;
    }

    public int getAccountsActive() {
        return accounts_active;
    }

    enum Listing {
        HOT ("hot"),
        NEW ("new"),
        TOP ("top");

        private final String value;
        Listing(String sort) {
            this.value = sort;
        }
    }
}
