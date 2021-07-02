package JRW;

public class Test {
    public static void main(String[] args) {
        RedditClient client = RedditClient.newClient(args[0], args[1]);
        Subreddit subreddit = client.getSubReddit("sfsd");
    }
}
