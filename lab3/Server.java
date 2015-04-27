import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
//import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yamin on 14-11-7.
 */

public class Server {

    private ServerSocket ss;
    public static boolean kill=false;
    public static int userCounter = 1;
    public static int tagCounter = 1;
    private ExecutorService threadPool = Executors.newFixedThreadPool(50);
    public static List<Tag> tagList = new ArrayList<Tag>();
    //  public List<ClientObject> list = new ArrayList<ClientObject>();
    //  public Hashtable clientSockets = new Hashtable();

    public Server(int port){
        try {
            // create a serverSocket
            ss = new ServerSocket(port);
            System.out.println("listening on port "+port);

            //use a seperate thread to check the server's status
            Thread checkServerStatus = new Thread(){
                public void run(){
                    if(kill) {
                        try{
                            ss.close();
                        }catch(IOException ie){
                            ie.printStackTrace();
                        }
                        System.exit(1);
                    }
                }
            };
            checkServerStatus.run();

            while (true) {
                //connect with client and get client socket
                Socket client = ss.accept();
                System.out.println("Accepted from "+client.getInetAddress());

                Thread newClient = new ConnectionThread(client);
                threadPool.execute(newClient);
                newClient.run();
            }
        }catch(IOException ie){
            ie.printStackTrace();
        }
    }

    //register users in the server
   /* public void register (ClientObject c){
        list.add(c);
    }*/

    //create tag if it has not been created
   /* public void createTag (Message m){
        Tag tag = new Tag(m.tag);
        ClientObject c= new ClientObject(m.client);
        // traverse the tag list to see if the tag is in the list
        for(int i=0;i<this.tag.size();i++){
            if(this.tag.get(i).tag.equals(m.tag)){
                // traverse the client list of the this specific tag to see if this client is inside this tag
                for(int j=0;j<this.tag.get(i).list.size();j++){
                    if(this.tag.get(i).list.get(j).userName.equals(m.client))
                        break;
                    else
                        this.tag.get(i).join(c);
                }
                break;
            }
            else {
                //check the message if the tag is public
                if(m.symbol=='#')
                    tag.Public = true;
                else if(m.symbol=='@')
                    tag.Public = false;
                // allow the client join the new tag
                tag.join(c);
                // add the new tag to the tag list
                this.tag.add(tag);
            }
        }
    }*/

    //forward message to the destination users
   // public void forwardMessage (Message m){
        //synchronized ()


     /*   // if message is a group message
        if(m.symbol=='#'){
            for(int i=0;i<this.tag.size();i++) {
                //find the tag of this message in the tag list
                if (this.tag.get(i).tag.equals(m.tag)) {
                    // send message to each client in the client list
                    for(int j=0;j<this.tag.get(i).list.size();j++){
                        // the message will be m.client + m.message
                    }
                }
            }
        }
        // if the message is a private one
        else if(m.symbol=='@'){
            //check if the destination client is in the client list of the server
            for(int i=0; i<this.list.size(); i++){
                if(this.list.get(i).userName.equals(m.client)){
                    //send the message directly to the destination user
                    break;
                }
                else{
                    //send the message to the user that there is no such destination user
                }
            }
        }
    }*/

    public static void main(String[] args)throws Exception{
        Server server1;
        if(args.length!=1)
            server1 = new Server(6789);
        else
            server1 = new Server(Integer.parseInt(args[0]));

        Tag TCD = new Tag("TCD_NDS", Server.tagCounter);
        Server.tagCounter++;
        Server.tagList.add(TCD);
    }
}
