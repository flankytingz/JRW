package me.flanked.JRW;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RedditClient {
    // Static variables for better performance
    private static final Logger logger = LoggerFactory.getLogger(RedditClient.class);
    private static final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

    // Object variables
    private final String UUID = java.util.UUID.randomUUID().toString();
    private String access_Token;

    /**
     * Creates a new reddit client which updates it's access token after it expires automatically.
     * @param ID The client ID of the application.
     * @param Secret The client secret of the application.
     * @return Returns a RedditClient which can be used to perform requests.
     */
    @CheckReturnValue
    public static RedditClient createNewClient (@NotNull String ID,@NotNull String Secret) {
        return new RedditClient(ID, Secret);
    }

    /**
     * Creates a reddit client object.
     * @param client_ID The client ID of the application.
     * @param client_Secret The client secret of the application.
     */
    private RedditClient (String client_ID, String client_Secret) {
        this.access_Token = Networker.getAccessToken(client_ID, client_Secret, UUID);
        // Updates access token one minute before it expires
        ses.scheduleWithFixedDelay(() -> this.access_Token = Networker.getAccessToken(client_ID, client_Secret, UUID), 3540L, 3540L, TimeUnit.SECONDS);
        logger.info("Created reddit client with UUID {}", this.UUID);
    }

    /**
     * Get a subreddit by it's name.
     * @param name Name of the subreddit.
     * @return Returns a subreddit object.
     */
    @CheckReturnValue
    public Subreddit getSubredditByName (@Nonnull String name) {
        return Subreddit.getSubredditByName(name, this.access_Token);
    }

    @CheckReturnValue
    public Submission getSubmissionByID (@Nonnull String ID) {
        return Submission.getSubmissionByData(Networker.getSubmission(ID, this.access_Token));
    }

    public String getUUID() {
        return UUID;
    }
}
