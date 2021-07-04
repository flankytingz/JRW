package me.Flanked.JRW;

import java.util.List;

public class Test {
    public static void main(String[] args) {
        RedditClient client = RedditClient.createNewClient(args[0], args[1]);
        Subreddit subreddit = client.getSubRedditByName("aww");
        List<Submission> list = subreddit.getSubmissions(5);
        list.forEach(s -> System.out.println(s.getTitle() + " : " + s.getAuthor()));
    }
}
