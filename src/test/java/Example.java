import me.flanked.JRW.Enums.Sort;
import me.flanked.JRW.RedditClient;
import me.flanked.JRW.Submission;
import me.flanked.JRW.Subreddit;

import java.util.List;

public class Example {
    public static void main(String[] args) {
        RedditClient client = RedditClient.createNewClient(args[0], args[1]);
        Subreddit subreddit = client.getSubredditByName("aww");
        List<Submission> submissions = subreddit.getSubmissions(5, Sort.HOT);
        submissions.forEach(
                submission -> {
                    if (submission.isPinned()) {
                        return;
                    }
                    System.out.println(submission);
                }
        );
    }
}
