package me.Flanked.JRW;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import me.Flanked.JRW.Exceptions.InvalidResponse;
import me.Flanked.JRW.Exceptions.InvalidSubredditName;
import me.Flanked.JRW.Exceptions.InvalidType;
import me.Flanked.JRW.Exceptions.RateLimited;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public class Networker {
    private static final Logger logger = LoggerFactory.getLogger(Networker.class);
    private static final String URL = "https://www.reddit.com/api/v1/access_token";
    private static final String GRANT_TYPE = "grant_type";
    private static final String GRANT_TYPE_URL = "https://oauth.reddit.com/grants/installed_client";
    private static final String DEVICE_ID = "device_id";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String SUBREDDIT_URL = "https://oauth.reddit.com/r/{subredditname}/about";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "bearer ";
    private static final String SUBREDDITNAME = "subredditname";

    private int ratelimitRemaining = -1;
    private int ratelimitReset = -1;

    protected Networker() {

    }

    private void ratelimitTest () throws RateLimited {
        if (ratelimitRemaining < 1) {
            logger.error("Client has being rate limited, please wait %s sec before making a request.".formatted(this.ratelimitReset));
            throw new RateLimited("Client has being rate limited, please wait %s sec before making a request.".formatted(this.ratelimitReset));
        }
    }

    @CheckReturnValue
    protected String getAccessToken (@Nonnull String client_ID,@Nonnull String client_Secret,@Nonnull String UUID) throws InvalidResponse {
        ratelimitTest();
        HttpResponse<JsonNode> response = Unirest.post(URL)
                .basicAuth(client_ID, client_Secret)
                .field(GRANT_TYPE, GRANT_TYPE_URL)
                .field(DEVICE_ID, UUID)
                .asJson();

        logger.debug("Attempted to retrieve access token, status {} {}", response.getStatus(), response.getStatusText());

        if (!(response.getStatus() == 200)) {
            throw new InvalidResponse("Failed to get an access token.");
        }

        this.ratelimitRemaining = Integer.parseInt(response.getHeaders().getFirst("x-ratelimit-remaining"));
        this.ratelimitReset = Integer.parseInt(response.getHeaders().getFirst("x-ratelimit-reset"));
        return response.getBody().getObject().getString(ACCESS_TOKEN);
    }

    @CheckReturnValue
    protected JSONObject getSubredditData (@Nonnull String subredditName, @Nonnull String access_Token) throws InvalidResponse, InvalidSubredditName, InvalidType {
        ratelimitTest();
        HttpResponse <JsonNode> response = Unirest.get(SUBREDDIT_URL)
                .header(AUTHORIZATION, BEARER + access_Token)
                .routeParam(SUBREDDITNAME, subredditName)
                .asJson();

        logger.debug("Attempted to retrieve subreddit \"{}\" data, status {} {}", subredditName, response.getStatus(), response.getStatusText());

        // If API didn't respond as expected throws an error
        if (!(response.getStatus() == 200)) {
            logger.error("Failed to get subreddit.");
            if (response.getStatus() == 404) {
                throw new InvalidSubredditName("Subreddit %s does not exist".formatted(subredditName));
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

        this.ratelimitRemaining = Integer.parseInt(response.getHeaders().getFirst("x-ratelimit-remaining"));
        this.ratelimitReset = Integer.parseInt(response.getHeaders().getFirst("x-ratelimit-reset"));
        return response.getBody().getObject().getJSONObject("data");
    }
}
