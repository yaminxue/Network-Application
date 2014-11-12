import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by yamin on 14-10-13.
 */

public class Client {
    public static void main(String args[])throws Exception {

        String sentence, modifiedSentence;

        Socket clientSocket = new Socket("localhost",6788);

        while(true) {
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            sentence = inFromUser.readLine();
            outToServer.writeBytes(sentence + '\n');

            modifiedSentence = inFromServer.readLine();

            System.out.println("FROM SERVER: " + modifiedSentence);
        }
        //inFromServer.close();
        //outToServer.close();
        //inFromUser.close();
        //clientSocket.close();
    }
}
