
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by yamin on 15/5/6.
 */
public class Test {
    static int portNumber=8767;

    public static void main(String args[])throws Exception{

        InetSocketAddress bootstrap = new InetSocketAddress("localhost", portNumber);
        Node bootstrapNode = new Node();
        bootstrapNode.init(new Socket(), 0);

        bootstrapNode.joinNetwork(bootstrap);
        bootstrapNode.run(portNumber);

//        Node nodeA = new Node();
//        nodeA.init(new Socket(), 1);
//        nodeA.joinNetwork(bootstrap);
//
//        Node nodeB = new Node();
//        nodeB.init(new Socket(), 2);
//        nodeB.joinNetwork(bootstrap);


    }
}
