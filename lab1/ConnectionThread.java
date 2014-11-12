package IndividualProject.MultiThreadServer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by yamin on 14-10-12.
 */

public class ConnectionThread extends Thread {
    public Socket client;
    public ConnectionThread(Socket client) {
        this.client=client;
    }
    public void run() {
        String clientSentence,capitalisedSentence;
        try{
            while(true) {
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
                clientSentence = inFromClient.readLine();

                DataOutputStream outToClient = new DataOutputStream(client.getOutputStream());
                if (clientSentence.equals("KILL_SERVICE")) {
                    outToClient.writeBytes("Service is terminated.\n");
                    client.close();
                    System.exit(1);
                } else if (clientSentence.equals("HELO text")) {
                    outToClient.writeBytes("HELLO text" + " IP: " + client.getLocalAddress() + " port: " + client.getLocalPort()  + " Student ID: 14302951" + '\n');
                } else {
                    capitalisedSentence = clientSentence.toUpperCase() + '\n';
                    outToClient.writeBytes(capitalisedSentence);
                }
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
}
