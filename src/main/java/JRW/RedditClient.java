package JRW;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import me.Flanked.JRW.Exceptions.InvalidResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RedditClient {
    // Static variables for better performance
    private static final Logger logger = LoggerFactory.getLogger(RedditClient.class);
    private static final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
    private static final String URL = "https://www.reddit.com/api/v1/access_token";
    private static final String GRANT_TYPE = "grant_type";
    private static final String GRANT_TYPE_URL = "https://oauth.reddit.com/grants/installed_client";
    private static final String DEVICE_ID = "device_id";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String EXPIRES_IN = "expires_in";

    // Object variables
    private final String UUID = java.util.UUID.randomUUID().toString();
    private final String client_ID;
    private final String client_Secret;
    private String access_Token;
    private long expires_In;

    /**
     * Creates a new reddit client which updates it's access token after it expires automatically.
     * @param client_ID The client ID of the application.
     * @param client_Secret The client secret of the application.
     * @return Returns a RedditClient which can be used to perform requests.
     */
    @CheckReturnValue
    public static RedditClient newClient (@NotNull String client_ID,@NotNull String client_Secret) {
        return new RedditClient(client_ID, client_Secret);
    }

    /**
     * Creates a reddit client object.
     * @param client_ID The client ID of the application.
     * @param client_Secret The client secret of the application.
     */
    private RedditClient (String client_ID, String client_Secret) {
        logger.debug("New reddit client created with UUID {}",UUID);
        this.client_ID = client_ID;
        this.client_Secret = client_Secret;
        getCredentials();

        // Updates access token as it about to expire.
        ses.scheduleWithFixedDelay(this::getCredentials, this.expires_In, this.expires_In, TimeUnit.SECONDS);

        logger.info("Created reddit client with UUID {}", this.UUID);
    }


    /**
     * Gets access token and expires in.
     * @throws InvalidResponse Throws if it is unable to get an access token
     */
    private void getCredentials () throws InvalidResponse {
        HttpResponse<JsonNode> response = Unirest.post(URL)
                .basicAuth(this.client_ID, this.client_Secret)
                .field(GRANT_TYPE, GRANT_TYPE_URL)
                .field(DEVICE_ID, this.UUID)
                .asJson();

        logger.debug("Attempted to retrieve credentials, status {} {}", response.getStatus(), response.getStatusText());

        if (!(response.getStatus() == 200)) {
            throw new InvalidResponse("Failed to get an access token.");
        }

        this.access_Token = response.getBody().getObject().getString(ACCESS_TOKEN);
        this.expires_In = response.getBody().getObject().getLong(EXPIRES_IN);

        logger.debug("Got credentials with access token {} and expires in {} seconds", this.access_Token, this.expires_In);
    }

    /**
     * Get a subreddit by it's name.
     * @param Subreddit_Name Name of the subreddit.
     * @return Returns a subreddit object.
     */
    @CheckReturnValue
    public Subreddit getSubReddit (@NotNull String Subreddit_Name) {
        return new Subreddit(Subreddit_Name.toLowerCase(Locale.ROOT), this.access_Token);
    }

}
