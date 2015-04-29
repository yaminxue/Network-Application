import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by yamin on 14/12/7.
 */
public interface PeerChat {
    void init(Socket socket, int uid); //initialise with a TCP server socket and unique id for node
    long joinNetwork(InetSocketAddress bootstrap_node); //returns network_id, a locally generated number to identify peer network
    boolean leaveNetwork(long network_id); //parameter is previously returned peer network identifier
    void chat(String text, String[] tags);
    ChatResult[] getChat(String[] words);
}
