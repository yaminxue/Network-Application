import java.net.Socket;

/**
 * Created by yamin on 14-11-7.
 */

public class Client {
    private String name = "";
    private Socket socket;
    private int ID = 0;
    //private String message;
    //private boolean connect;
    public Client(Socket s){
        this.socket=s;
    }

    public String getName(){
        return name;
    }

    public void setName(String in){
        name =in;
    }

    public int getID(){
        return ID;
    }

    public void setID(int in){
        ID =in;
    }

   /* public String getMessage(){
        return message;
    }

    public void setMessage(String in){
        message = in;
    }
*/
    /*public boolean getConnect (){
        return connect;
    }

    public void setConnect(boolean in){
        connect = in;
    }*/


    public Socket getSocket(){
        return socket;
    }

}
