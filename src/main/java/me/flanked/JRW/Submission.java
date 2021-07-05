package me.flanked.JRW;

import kong.unirest.json.JSONObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Submission {
    private final String subreddit;
    private final String author;
    private final String title;
    private final String id;
    private final int upvotes;
    private final double upvote_ratio;
    private final int total_awards_received;
    private final String thumbnail;
    private final long created;
    private final boolean over_18;
    private final boolean spoiler;
    private final String url;
    private final String link;
    private final int num_comments;
    private final boolean pinned;

    @CheckReturnValue
    protected static Submission getSubmissionByData (@Nonnull JSONObject data) {
        return new Submission(data);
    }

    private Submission (JSONObject data) {
        subreddit = data.getString("subreddit");
        author = data.getString("author");
        title = data.getString("title");
        id = data.getString("id");
        upvotes = data.getInt("ups");
        upvote_ratio = data.getInt("upvote_ratio");
        total_awards_received = data.getInt("total_awards_received");
        thumbnail = data.getString("thumbnail");
        created = data.getLong("created");
        over_18 = data.getBoolean("over_18");
        spoiler = data.getBoolean("spoiler");
        url = data.getString("url");
        link = data.getString("permalink");
        num_comments = data.getInt("num_comments");
        pinned = data.getBoolean("stickied");
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getSubredditName() {
        return subreddit;
    }

    public String getId() {
        return id;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public double getUpvoteRatio() {
        return upvote_ratio;
    }

    public int getTotalAwardsReceived() {
        return total_awards_received;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public LocalDateTime getTimeCreated() {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(created), ZoneId.systemDefault());
    }

    public boolean isOver18() {
        return over_18;
    }

    public boolean isSpoiler() {
        return spoiler;
    }

    public String getAttachmentUrl() {
        return url;
    }

    public String getLink() {
        return "https://www.reddit.com" + link;
    }

    public int getNumComments() {
        return num_comments;
    }

    public boolean isPinned() {
        return pinned;
    }

    @Override
    public String toString() {
        return "Submission{" +
                "subreddit='" + subreddit + '\'' +
                ", author='" + author + '\'' +
                ", title='" + title + '\'' +
                ", id='" + id + '\'' +
                ", upvotes=" + upvotes +
                ", upvote_ratio=" + upvote_ratio +
                ", total_awards_received=" + total_awards_received +
                ", thumbnail='" + thumbnail + '\'' +
                ", created=" + created +
                ", over_18=" + over_18 +
                ", spoiler=" + spoiler +
                ", url='" + url + '\'' +
                ", link='" + link + '\'' +
                ", num_comments=" + num_comments +
                ", pinned=" + pinned +
                '}';
    }
}
