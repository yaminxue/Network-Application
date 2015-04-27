import java.util.ArrayList;

/**
 * Created by yamin on 14-11-7.
 */

public class Tag {
    private String name;
    private int ID;
    public ArrayList<Client> clientList = new ArrayList<Client>();


    /*public Boolean Public;
    public Queue<String> MessageQueue;
    public Hashtable outputStreams = new Hashtable();*/

    public Tag (String name, int ID){
        this.name=name;
        this.ID=ID;

    }

    //allow users join a tag
    public synchronized void join (Client c){
        synchronized (clientList) {
            clientList.add(c);
        }
    }

    //allow users leave a tag
    public synchronized void leave (Client c){
        synchronized (clientList) {
            clientList.remove(c);
        }
    }

    public String getName(){
        return name;
    }

    public void setName(String in){
        name=in;
    }

    public int getID(){
        return ID;
    }

    public void setID(int in){
        ID =in;
    }

    public ArrayList<Client> getClientList() {
        return clientList;
    }

    public void setClientList(ArrayList<Client> in) {
        clientList = in;
    }
}
