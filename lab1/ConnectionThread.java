
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by yamin on 14-10-12.
 */

public class ConnectionThread extends Thread {
    private Socket client;
    private ServerSocket server;
    private BufferedReader inFromClient;
    private DataOutputStream outToClient;
    private PrintWriter pw;

    public ConnectionThread(Socket client,ServerSocket server) {
        this.client=client;
        this.server=server;
    }
    public void run() {
        String clientSentence;
        while(true) {
        try{
           
                inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
                clientSentence = inFromClient.readLine();

                outToClient = new DataOutputStream(client.getOutputStream());
                if (clientSentence.equals("KILL_SERVICE")) {
                    outToClient.writeBytes("Service is terminated.\n");
                   
                    server.close(); //client.close();
                    System.exit(1);
                } else if (clientSentence.equals("HELO BASE_TEST")) {
                    pw = new PrintWriter(outToClient,true);
                    String ipadd = client.getLocalAddress().getHostAddress();
                    pw.println("HELO BASE_TEST\n" + "IP:" + ipadd+'\n' + "Port:" + client.getLocalPort() +'\n'+ "StudentID:14302951" );
                } else {
                    //capitalisedSentence = clientSentence.toUpperCase() + '\n';
                    //outToClient.writeBytes(capitalisedSentence);
                }
            }
       
        catch(Exception e){
            System.out.println(e);
        }finally{
        	try{
        		client.close();
        		inFromClient.close();
        		outToClient.close();
        		pw.close();
        	}
            catch(Exception e){
                System.out.println(e);
                }
        }
        }
    }
  
}
