/**
 * Created by yamin on 14/12/7.
 */
public class ChatResult {
    String tags; //strings matched for this url
    String[] posts; //url matching chat query

    public ChatResult(String tags, String[] posts){
        this.tags = tags;
        this.posts = posts;
    }
}
