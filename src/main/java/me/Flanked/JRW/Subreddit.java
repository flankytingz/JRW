package me.Flanked.JRW;

import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Subreddit {

    private static final Logger logger = LoggerFactory.getLogger(Subreddit.class);

    private final String access_token;

    // Subreddit data
    private final String subredditName;

    protected Subreddit (String subredditName, String access_token, Networker networker) {
        this.subredditName = subredditName;
        this.access_token = access_token;
        JSONObject data = networker.getSubredditData(this.subredditName, this.access_token);
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
