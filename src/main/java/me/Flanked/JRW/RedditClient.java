package me.Flanked.JRW;

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

    // Object variables
    private final String UUID = java.util.UUID.randomUUID().toString();
    private final Networker networker = new Networker();
    private String access_Token;

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
        this.access_Token = networker.getAccessToken(client_ID, client_Secret, UUID);
        // Updates access token one minute before it expires
        ses.scheduleWithFixedDelay(() -> this.access_Token = networker.getAccessToken(client_ID, client_Secret, UUID), 3540L, 3540L, TimeUnit.SECONDS);
        logger.info("Created reddit client with UUID {}", this.UUID);
    }

    /**
     * Get a subreddit by it's name.
     * @param Subreddit_Name Name of the subreddit.
     * @return Returns a subreddit object.
     */
    @CheckReturnValue
    public Subreddit getSubReddit (@NotNull String Subreddit_Name) {
        return new Subreddit(Subreddit_Name.toLowerCase(Locale.ROOT), this.access_Token, this.networker);
    }

    public String getUUID() {
        return UUID;
    }
}
