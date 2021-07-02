package JRW;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import me.Flanked.JRW.Exceptions.InvalidResponse;
import me.Flanked.JRW.Exceptions.InvalidSubredditName;
import me.Flanked.JRW.Exceptions.InvalidType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Subreddit {

    private static final Logger logger = LoggerFactory.getLogger(Subreddit.class);
    private static final String URL = "https://oauth.reddit.com/r/{subredditname}/about";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "bearer ";
    private static final String SUBREDDITNAME = "subredditname";

    private final String access_token;

    // Subreddit data
    private final String subreddit_Name;
    private String title;
    private int active_user_count;
    private String icon_img;
    private int subscribers;
    private boolean emojis_enabled;
    private String public_description;
    private String community_icon;
    private String banner_background_image;
    private boolean original_content_tag_enabled;
    private boolean community_reviewed;
    private String submit_text;
    private boolean spoilers_enabled;
    private String header_title;
    private boolean all_original_content;
    private Long created_utc;
    private boolean allow_videogifs;
    private boolean allow_polls;
    private boolean allow_videos;
    private boolean is_crosspostable_subreddit;
    private boolean allow_discovery;
    private boolean subreddit_type;
    private boolean over18;
    private String description;
    private boolean allow_images;
    private String lang;



    protected Subreddit (String SubredditName, String access_token) {
        this.subreddit_Name = SubredditName;
        this.access_token = access_token;
        getData();
    }

    private void getData () throws InvalidType, InvalidResponse, InvalidSubredditName {
        HttpResponse <JsonNode> response = Unirest.get(URL)
                .header("Authorization", BEARER + this.access_token)
                .routeParam(SUBREDDITNAME, this.subreddit_Name)
                .asJson();

        logger.debug("Attempted to retrieve subreddit data, status {} {}", response.getStatus(), response.getStatusText());

        // If API didn't respond as expected throws an error
        if (!(response.getStatus() == 200)) {
            logger.error("Failed to get subreddit.");
            if (response.getStatus() == 404) {
                throw new InvalidSubredditName("Subreddit %s does not exist".formatted(this.subreddit_Name));
            }
            throw new InvalidResponse("Received an invalid response from API");
        }

        // If the returned data is not for a subreddit throws an error
        if (!response.getBody().getObject().getString("kind").equals("t5")) {
            String type;
            type = switch (response.getBody().getObject().getString("kind")) {
                case "t1" -> "comment";
                case "t2" -> "account";
                case "t3" -> "link";
                case "t4" -> "message";
                case "t5" -> "subreddit";
                case "t6" -> "award";
                default -> "unknown";
            };
            logger.error("API return invalid type of object. Requested for subreddit, returned {}",type);
            throw new InvalidType("API return invalid type of object. Requested for subreddit, returned %s".formatted(type));
        }
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
