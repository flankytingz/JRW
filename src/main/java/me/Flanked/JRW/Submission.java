package me.Flanked.JRW;

import kong.unirest.json.JSONObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Submission {
    private final String subreddit;
    private final String author;
    private final String title;
    private final String id;
    private final int ups;
    private final double upvote_ratio;
    private final int downs;
    private final int total_awards_received;
    private final String thumbnail;
    private final long created;
    private final boolean over_18;
    private final boolean spoiler;
    private final String url;
    private final int num_comments;

    @CheckReturnValue
    protected static Submission getSubmissionByData (@Nonnull JSONObject data) {
        return new Submission(data);
    }

    private Submission (JSONObject object) {
        JSONObject data = object.getJSONObject("data");
        this.subreddit = data.getString("subreddit");
        this.author = data.getString("author");
        this.title = data.getString("title");
        this.id = data.getString("id");
        this.ups = data.getInt("ups");
        this.upvote_ratio = data.getInt("upvote_ratio");
        this.downs = (int) -((this.upvote_ratio * this.ups) - this.ups);
        this.total_awards_received = data.getInt("total_awards_received");
        this.thumbnail = data.getString("thumbnail");
        this.created = data.getLong("created");
        this.over_18 = data.getBoolean("over_18");
        this.spoiler = data.getBoolean("spoiler");
        this.url = data.getString("url");
        this.num_comments = data.getInt("num_comments");
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }
}
