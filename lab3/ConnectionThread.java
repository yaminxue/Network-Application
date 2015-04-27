import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by yamin on 14/11/9.
 */

public class ConnectionThread extends Thread {
    private Socket client;
    private BufferedReader inFromClient;
    private DataOutputStream outToClient;
    private PrintWriter pw;
    private Client co;

    public ConnectionThread(Socket client) {

        co = new Client(client);
        this.client=client;

        try {

            inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
            outToClient = new DataOutputStream(client.getOutputStream());
            pw = new PrintWriter(outToClient, true);
        }catch(IOException ie){
            ie.printStackTrace();
        }
    }

    public void run() {
        while(true) {
            try{
                //System.out.println(1);
                String[] clientSentence = inFromClient.readLine().split("\\s+");

                if (clientSentence[0].equals("HELO")) {

                    HELO();

                } else if (clientSentence[0].equals("KILL_SERVICE")) {
                    KILL_SERVICE();

                } else if (clientSentence[0].equals("DISCONNECT")) {
                    disconnect();

                } else if (clientSentence[0].equals("JOIN_CHATROOM")) {
                   // System.out.println("ykj");
                    joinChatroom(clientSentence);

                } else if (clientSentence[0].equals("LEAVE_CHATROOM")) {
                    leaveChatroom(clientSentence);

                } else if (clientSentence[0].equals("CHAT")) {
                    messaging(clientSentence);

                } else {
                    Error(1, "Illegal message!");
                }
            }

            catch(Exception e){
                System.out.println(e);
            }/*finally{
                try{
                    client.close();
                    inFromClient.close();
                    outToClient.close();
                    pw.close();
                }
                catch(Exception e){
                    System.out.println(e);
                }
            }*/
       }
    }

    private void HELO(){

        int studentID = 14302951;
        String ipadd = client.getLocalAddress().getHostAddress();
        int port = client.getLocalPort();
        pw.println("HELO BASE_TEST\n" + "IP:" + ipadd+'\n' + "Port:" + port +'\n'+ "StudentID:"+studentID );
    }

    private void KILL_SERVICE(){
            pw.println("Service is terminated.\n");
            Server.kill=true;
            System.exit(1);
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

    private synchronized void joinChatroom(String[] in) throws Exception {
       // System.out.println("ykj");
        co.setName(in[7]);

        //check if this client has already got an ID
        if(co.getID()==0) {
            co.setID(Server.userCounter);
            Server.userCounter++;
        }

        for(Tag t : Server.tagList){
            if(t.getName().equals(in[1])){
                if(!t.getClientList().contains(co)){
                    t.join(co);
                    pw.println("JOINED_CHATROOM:" + t.getName());
                    pw.println("SERVER_IP:" + InetAddress.getLocalHost().getHostAddress());
                    pw.println("PORT:" + client.getLocalPort());
                    pw.println("ROOM_REF:" + t.getID());
                    pw.println("JOIN_ID:" + co.getID());
                    //post a join message to the relevant tag
                    for (Client c : t.getClientList()) {
                        if(c!=co) {
                            DataOutputStream dos = new DataOutputStream(c.getSocket().getOutputStream());
                            PrintWriter p = new PrintWriter(dos, true);
                            p.println("CHAT:" + t.getID());
                            p.println("CLIENT_NAME:" + co.getName());
                            p.println("MESSAGE:" + "CLIENT JOINED" + '\n');
                        }
                    }
                }
                else{
                    Error(3, "You have already joined this chatroom!");
                }
            }
            else{
                Error(2, "No such Tag exists!");
            }

        }
    }

    private synchronized void leaveChatroom(String[] in)throws Exception{
        for(Tag t : Server.tagList){
            if(Integer.toString(t.getID()).equals(in[1])){
                pw.println("LEAVE_CHATROOM:" + t.getID());
                pw.println("JOIN_ID:" + co.getID());
                if(t.getClientList().contains(co)) {
                    t.leave(co);
                }
                    //post a leave message to relevant tag
                    for (Client c : t.getClientList()) {
                        if(c!=co) {
                            DataOutputStream dos = new DataOutputStream(c.getSocket().getOutputStream());
                            PrintWriter p = new PrintWriter(dos, true);
                            p.println("CHAT:" + t.getID());
                            p.println("CLIENT_NAME:" + co.getName());
                            p.println("MESSAGE:" + "CLIENT LEFT" + '\n');
                        }
                    }
            }
                /*else{
                    Error(4, "You didn't join this chatroom or you have already leaved this chatroom!");
                }*/
            else{
                Error(2, "No such Tag exists!");
            }
        }
    }

    private void disconnect(){
        try{
            client.close();
            inFromClient.close();
            outToClient.close();
            pw.close();
            this.interrupt();
        }catch(IOException ie){
            ie.printStackTrace();
        }
    }

    private synchronized void messaging(String[] in)throws Exception{
        for(Tag t: Server.tagList) {
            if (Integer.toString(t.getID()).equals(in[1])) {
                if (t.getClientList().contains(co)) {
                    for (Client c : t.getClientList()) {
                        DataOutputStream dos = new DataOutputStream(c.getSocket().getOutputStream());
                        PrintWriter p = new PrintWriter(dos,true);
                        p.println("CHAT:"+t.getID());
                        p.println("CLIENT_NAME:"+co.getName());
                        p.println("MESSAGE:"+in[7]+'\n');
                    }
                }
                else{
                    Error(4, "You didn't join this chatroom or you have already leaved this chatroom!");
                }
            }

            else{
                Error(2, "No such Tag exists!");
            }
        }
    }

    private void Error(int i, String in)throws Exception{
        pw.println("ERROR_CODDE:"+i);
        pw.println("ERROR_DESCRIPTION:"+in);
        //terminate the client
        //client.close();
        //inFromClient.close();
        //outToClient.close();
        //pw.close();
        //this.interrupt();
    }

}
