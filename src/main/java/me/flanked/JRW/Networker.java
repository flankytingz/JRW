package me.flanked.JRW;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import me.flanked.JRW.Enums.Sort;
import me.flanked.JRW.Exceptions.*;
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
    private static final String SUBMISSION_URL = "https://oauth.reddit.com/comments/{id}";

    private static int rateLimitRemaining = -1;
    private static int rateLimitReset = -1;

    private static void rateLimitTest() throws RateLimited {
        if (rateLimitRemaining == 0) {
            logger.error("Client has being rate limited, please wait %s sec before making a request.".formatted(rateLimitReset));
            throw new RateLimited("Client has being rate limited, please wait %s sec before making a request.".formatted(rateLimitReset));
        }
    }

    @CheckReturnValue
    protected static String getAccessToken (@Nonnull String client_ID,@Nonnull String client_Secret,@Nonnull String UUID) throws InvalidResponse {
        HttpResponse<JsonNode> response = Unirest.post(OAUTH_URL)
                .basicAuth(client_ID, client_Secret)
                .field(GRANT_TYPE, GRANT_TYPE_URL)
                .field(DEVICE_ID, UUID)
                .asJson();

        logger.debug("Attempted to retrieve access token, status {} {}", response.getStatus(), response.getStatusText());

        if (!(response.getStatus() == 200)) {
            throw new InvalidResponse("Failed to get an access token.");
        }

        rateLimitRemaining = Integer.parseInt(response.getHeaders().getFirst("x-ratelimit-remaining"));
        rateLimitReset = Integer.parseInt(response.getHeaders().getFirst("x-ratelimit-reset"));
        rateLimitTest();
        return response.getBody().getObject().getString(ACCESS_TOKEN);
    }

    @CheckReturnValue
    protected static JSONObject getSubredditData (@Nonnull String name, @Nonnull String access_Token) throws InvalidResponse, InvalidSubredditName, InvalidType {
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
        testForType(response, kind, "t5");

        rateLimitRemaining = Integer.parseInt(response.getHeaders().getFirst("x-ratelimit-remaining"));
        rateLimitReset = Integer.parseInt(response.getHeaders().getFirst("x-ratelimit-reset"));
        rateLimitTest();
        return response.getBody().getObject().getJSONObject("data");
    }

    @CheckReturnValue
    protected static JSONArray getSubmissions (@Nonnull Subreddit subreddit, int limit, Sort sort, @Nonnull String access_Token) {
        HttpResponse<JsonNode> response;
//        if (sort.getTopOfValue() == null) {
//             response = Unirest.get(SUBMISSIONS_URL)
//                    .header(AUTHORIZATION, BEARER + access_Token)
//                    .routeParam(SUBREDDITNAME, subreddit.getDisplayName().toLowerCase(Locale.ROOT))
//                    .routeParam("sort", sort.getValue())
//                    .queryString("limit", limit)
//                    .asJson();
//        } else {
            response = Unirest.get(SUBMISSIONS_URL)
                    .header(AUTHORIZATION, BEARER + access_Token)
                    .routeParam(SUBREDDITNAME, subreddit.getDisplayName().toLowerCase(Locale.ROOT))
                    .routeParam("sort", sort.getValue())
                    .queryString("limit", limit)
                    .queryString("t",sort.getTopOfValue())
                    .asJson();
//        }

        logger.debug("Attempted to retrieve subreddit \"{}\" submissions with limit {}, status {} {}", subreddit.getDisplayName(), limit, response.getStatus(), response.getStatusText());

        if (!(response.getStatus() == 200)) {
            logger.error("Failed to get subreddit.");
            throw new InvalidResponse("Received an invalid response from API");
        }
        String kind = response.getBody().getObject().getString("kind");
        testForType(response, kind, "Listing");
        return response.getBody().getObject().getJSONObject("data").getJSONArray("children");
    }

    @CheckReturnValue
    protected static JSONObject getSubmission (@Nonnull String ID, @Nonnull String access_Token) {
        HttpResponse<JsonNode> response = Unirest.get(SUBMISSION_URL)
                .header(AUTHORIZATION, BEARER + access_Token)
                .routeParam("id", ID)
                .asJson();

        logger.debug("Attempted to retrieve submission \"{}\" , status {} {}", ID, response.getStatus(), response.getStatusText());

        if (!(response.getStatus() == 200)) {
            logger.error("Failed to get submission.");
            if (response.getStatus() == 400) {
                throw new InvalidSubmissionID("Submission does not exist");
            }
            throw new InvalidResponse("Received an invalid response from API");
        }

        String kind = response.getBody().getArray().getJSONObject(1).getString("kind");
        testForType(response, kind, "Listing");
        return response.getBody().getArray().getJSONObject(1).getJSONObject("data").getJSONArray("children").getJSONObject(1);
    }

    private static void testForType(HttpResponse<JsonNode> response, String kind, String request) {
        if (!kind.equalsIgnoreCase(request)) {
            String type = switch (kind) {
                case "t1" -> "comment";
                case "t2" -> "account";
                case "t3" -> "link";
                case "t4" -> "message";
                case "t5" -> "subreddit";
                case "t6" -> "award";
                default -> "unknown";
            };
            logger.error("API return invalid type of object. Requested for {}, returned {}", request,type);
            throw new InvalidType("API return invalid type of object. Requested for %s, returned %s".formatted(request, type));
        }


        rateLimitRemaining = Integer.parseInt(response.getHeaders().getFirst("x-ratelimit-remaining"));
        rateLimitReset = Integer.parseInt(response.getHeaders().getFirst("x-ratelimit-reset"));
        rateLimitTest();
    }
}
