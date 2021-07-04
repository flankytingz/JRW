package me.Flanked.JRW;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import me.Flanked.JRW.Exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Locale;

public class Networker {
    private static final Logger logger = LoggerFactory.getLogger(Networker.class);

    private static final String OAUTH_URL = "https://www.reddit.com/api/v1/access_token";
    private static final String GRANT_TYPE = "grant_type";
    private static final String GRANT_TYPE_URL = "https://oauth.reddit.com/grants/installed_client";
    private static final String DEVICE_ID = "device_id";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String SUBREDDIT_URL = "https://oauth.reddit.com/r/{subredditname}/about";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "bearer ";
    private static final String SUBREDDITNAME = "subredditname";
    private static final String SUBMISSIONS_URL = "https://oauth.reddit.com/r/{subredditname}/{sort}";

    private static int ratelimitRemaining = -1;
    private static int ratelimitReset = -1;

    private static void ratelimitTest () throws RateLimited {
        if (ratelimitRemaining == 0) {
            logger.error("Client has being rate limited, please wait %s sec before making a request.".formatted(ratelimitReset));
            throw new RateLimited("Client has being rate limited, please wait %s sec before making a request.".formatted(ratelimitReset));
        }
    }

    @CheckReturnValue
    protected static String getAccessToken (@Nonnull String client_ID,@Nonnull String client_Secret,@Nonnull String UUID) throws InvalidResponse {
        ratelimitTest();
        HttpResponse<JsonNode> response = Unirest.post(OAUTH_URL)
                .basicAuth(client_ID, client_Secret)
                .field(GRANT_TYPE, GRANT_TYPE_URL)
                .field(DEVICE_ID, UUID)
                .asJson();

        logger.debug("Attempted to retrieve access token, status {} {}", response.getStatus(), response.getStatusText());

        if (!(response.getStatus() == 200)) {
            throw new InvalidResponse("Failed to get an access token.");
        }

        ratelimitRemaining = Integer.parseInt(response.getHeaders().getFirst("x-ratelimit-remaining"));
        ratelimitReset = Integer.parseInt(response.getHeaders().getFirst("x-ratelimit-reset"));
        return response.getBody().getObject().getString(ACCESS_TOKEN);
    }

    @CheckReturnValue
    protected static JSONObject getSubredditData (@Nonnull String name, @Nonnull String access_Token) throws InvalidResponse, InvalidSubredditName, InvalidType {
        ratelimitTest();
        HttpResponse <JsonNode> response = Unirest.get(SUBREDDIT_URL)
                .header(AUTHORIZATION, BEARER + access_Token)
                .routeParam(SUBREDDITNAME, name)
                .asJson();

        logger.debug("Attempted to retrieve subreddit \"{}\" data, status {} {}", name, response.getStatus(), response.getStatusText());

        // If API didn't respond as expected throws an error
        if (!(response.getStatus() == 200)) {
            logger.error("Failed to get subreddit.");
            if (response.getStatus() == 404) {
                throw new InvalidSubredditName("Subreddit %s does not exist".formatted(name));
            }
            throw new InvalidResponse("Received an invalid response from API");
        }

        // If the returned data is not for a subreddit throws an error
        String kind = response.getBody().getObject().getString("kind");
        if (!kind.equals("t5")) {
            String type = getType(kind);
            logger.error("API return invalid type of object. Requested for subreddit, returned {}",type);
            throw new InvalidType("API return invalid type of object. Requested for subreddit, returned %s".formatted(type));
        }

        ratelimitRemaining = Integer.parseInt(response.getHeaders().getFirst("x-ratelimit-remaining"));
        ratelimitReset = Integer.parseInt(response.getHeaders().getFirst("x-ratelimit-reset"));
        return response.getBody().getObject().getJSONObject("data");
    }

    @CheckReturnValue
    protected static JSONArray getSubmissions (@Nonnull Subreddit subreddit, int limit, @Nonnull String access_Token) {
        ratelimitTest();
        HttpResponse<JsonNode> response = Unirest.get(SUBMISSIONS_URL)
                .header(AUTHORIZATION, BEARER + access_Token)
                .routeParam(SUBREDDITNAME, subreddit.getDisplayName().toLowerCase(Locale.ROOT))
                .routeParam("sort", "hot")
                .queryString("limit", limit)
                .asJson();

        logger.debug("Attempted to retrieve subreddit \"{}\" submissions with limit {}, status {} {}", subreddit.getDisplayName(), limit, response.getStatus(), response.getStatusText());

        if (!(response.getStatus() == 200)) {
            logger.error("Failed to get subreddit.");
            throw new InvalidResponse("Received an invalid response from API");
        }
        String kind = response.getBody().getObject().getString("kind");
        if (!kind.equals("Listing")) {
            String type = getType(kind);
            logger.error("API return invalid type of object. Requested for listing, returned {}",type);
            throw new InvalidType("API return invalid type of object. Requested for listing, returned %s".formatted(type));
        }


        ratelimitRemaining = Integer.parseInt(response.getHeaders().getFirst("x-ratelimit-remaining"));
        ratelimitReset = Integer.parseInt(response.getHeaders().getFirst("x-ratelimit-reset"));
        return response.getBody().getObject().getJSONObject("data").getJSONArray("children");
    }

    @CheckReturnValue
    private static String getType (String kind) {
        return switch (kind) {
            case "t1" -> "comment";
            case "t2" -> "account";
            case "t3" -> "link";
            case "t4" -> "message";
            case "t5" -> "subreddit";
            case "t6" -> "award";
            default -> "unknown";
        };
    }
}
