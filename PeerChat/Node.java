import java.io.DataOutputStream;
import java.util.*;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.concurrent.*;

/**
 * Created by yamin on 14/12/7.
 */

public class Node implements PeerChat {
    private int port;
    private long nodeID;
    private long network_id;
    private Socket socket;
    private ServerSocket ss;
    //public static boolean kill=false;
    private ExecutorService threadPool = Executors.newFixedThreadPool(100);
    private Vector<String> chatACK;
    private TreeMap<Long, InetSocketAddress> routingTable = new TreeMap<Long, InetSocketAddress>();
    private HashMap<String, Boolean> pingACK = new HashMap<String, Boolean>();
    HashMap<String, HashMap<String, Integer>> chatTable = new HashMap<String, HashMap<String, Integer>>();
    private Vector<ChatResult> chatResult;

    public Node(){
        super();
    }

    // used to run the server via the port
    public void run(int NodePort) throws Exception {
        ss = new ServerSocket(NodePort);
        while(true){
            Socket s = ss.accept();//connect with client and get client socket
            System.out.println("Accepted from "+s.getInetAddress());

            Thread newNode = new Handler(s);
            threadPool.execute(newNode);
            newNode.run();
        }
    }

    @Override
    // initialise with a TCP server socket and unique id for node
    public void init(Socket socket, int id) {
        this.socket = socket;
        this.nodeID = id;
        this.port = socket.getPort();
    }

    @Override
    //return network_id, a locally generated number to identify peer network
    public long joinNetwork(InetSocketAddress bootstrap_node){
        try{
            JSONObject toSend = new JSONObject();
            toSend.put("type", "JOINGING_NETWORK");
            toSend.put("node_id", String.valueOf(nodeID));
            toSend.put("ip_address", InetAddress.getLocalHost().toString());

            send(toSend, bootstrap_node);
        }catch(JSONException je){
            je.printStackTrace();
        }catch(UnknownHostException ue){
            ue.printStackTrace();
        }
        return nodeID;
    }

    @Override
    // parameter is previously returned peer network identifier
    public boolean leaveNetwork(long network_id) {
        try{
            JSONObject toSend = new JSONObject();

            toSend.put("type", "LEAVING_NETWORK");
            toSend.put("node_id", nodeID);

            for(Entry<Long, InetSocketAddress> entry : routingTable.entrySet()){
                send(toSend, entry.getValue());
            }
        }catch(JSONException je){
            je.printStackTrace();
        }
        return true;
    }

    @Override
    // used to send chat message via message content and the tags
    public void chat(String text, String[] tags) {
        chatACK = new Vector<String>();
        try{
            for(int i=0; i<tags.length; i++){
                JSONObject toSend = new JSONObject();
                LinkedList<String> l1 = new LinkedList<String>();
                toSend.put("type","CHAT");
                toSend.put("target_id", hashCode(tags[i]));
                toSend.put("sender_id",Long.toString(nodeID));
                toSend.put("tag",tags[i]);
                toSend.put("text", l1);
                l1.add(text);
                send(toSend, hashCode(tags[i]));
            }

            ExecutorService service = Executors.newSingleThreadExecutor();
            final String[] t = tags;
            try{
                Runnable r = new Runnable(){
                    public void run(){
                        while (chatACK.size()<t.length)
                            ;
                    }
                };

                // check the result in 30 seconds
                Future<?> f = service.submit(r);
                f.get(30, TimeUnit.SECONDS);
            }catch (final InterruptedException e){
                e.printStackTrace();
            }catch (final TimeoutException e){
                // if there is no responses, send PING message
                for(int i = 0; i< tags.length; i++){
                    if(!chatACK.contains(tags[i])){
                        try{
                            JSONObject toSend = new JSONObject();
                            toSend.put("type", "PING");
                            toSend.put("target_id", hashCode(tags[i]));
                            toSend.put("sender_id", Long.toString(nodeID));

                            toSend.put("ip_address", InetAddress.getLocalHost().getHostAddress().toString());

                            send(toSend, hashCode(tags[i]));
                            Thread.sleep(10000);

                            synchronized (pingACK){
                                if(!pingACK.get((String) toSend.get("ip_address"))){
                                    routingTable.remove(findNearest(hashCode(tags[i])));
                                }
                            }
                            chat(text,tags);
                        }catch(InterruptedException e1){
                            e1.printStackTrace();
                        }
                        catch(UnknownHostException e2){
                            e2.printStackTrace();
                        }
                    }
                }
            }catch(final ExecutionException e){
                e.printStackTrace();
            }finally{
                service.shutdown();
            }
        }catch(JSONException je){
            je.printStackTrace();
        }
    }

    @Override
    // used to return chat results via the tags
    public ChatResult[] getChat(String[] words) {
        final String[] tmpWords = words;
        chatResult = new Vector<ChatResult>();
        try {
            for (int i = 0; i < words.length; i++) {
                JSONObject toSend = new JSONObject();
                toSend.put("type", "CHAT_RETRIEVE");
                toSend.put("tag", words[i]);
                toSend.put("node_id", hashCode(words[i]));
                toSend.put("sender_id", nodeID);
                send(toSend, hashCode(words[i]));
            }

            ExecutorService service = Executors.newSingleThreadExecutor();

            try {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        while (chatResult.size() < tmpWords.length)
                            ;
                    }
                };

                Future<?> f = service.submit(r);
                f.get(30, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            } catch (final TimeoutException e) {
                // if there is no result in 30 seconds, send PING message
                for (int i = 0; i < words.length; i++) {
                    if (!chatResult.contains(words[i])) {
                        try {
                            JSONObject toSend = new JSONObject();
                            toSend.put("type", "PING");
                            toSend.put("target_id", hashCode(words[i]));
                            toSend.put("sender_id", Long.toString(nodeID));
                            toSend.put("ip_address", InetAddress.getLocalHost()
                                    .getHostAddress().toString());

                            send(toSend, hashCode(words[i]));
                            // wait fro 10 seconds
                            Thread.sleep(10000);
                            // if there is no PING-ACK, remove the target node from the routing table
                            synchronized (pingACK) {
                                if (!pingACK.get((String) toSend
                                        .get("ip_address"))) {
                                    routingTable
                                            .remove(findNearest(hashCode(words[i])));
                                }
                            }
                            return getChat(words);
                        } catch ( InterruptedException e1) {
                            e1.printStackTrace();
                        }catch ( UnknownHostException e2){
                            e2.printStackTrace();
                        }
                    }
                }
            } catch (final ExecutionException e) {
                e.printStackTrace();
            } finally {
                service.shutdown();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return chatResult.toArray(new ChatResult[chatResult.size()]);
    }

    // used to find target node
    public int hashCode(String str){
        int hash = 0;
        for (int i=0; i<str.length(); i++){
            hash = hash*31 + str.charAt(i);
        }
        return Math.abs(hash);
    }

    // used to find nearest node
    private Long findNearest(long key) {
        Entry<Long, InetSocketAddress> low = routingTable.floorEntry(key);
        Entry<Long, InetSocketAddress> high = routingTable.ceilingEntry(key);
        Long res = (long) 0;
        if (low != null && high != null) {
            res = Math.abs(key - low.getKey()) < Math.abs(key - high.getKey()) ? low
                    .getKey() : high.getKey();
        } else if (low != null || high != null) {
            res = low != null ? low.getKey() : high.getKey();
        }
        return res;
    }

    // used to send messages by target nodeID
    private void send(JSONObject toSend, long targetNode) {
        String targetAdd = routingTable.get(findNearest(targetNode)).toString();

        try {
            Socket socket = new Socket(targetAdd, 8767);
            DataOutputStream outToTarget = new DataOutputStream(socket.getOutputStream());
            String contents = toSend.toString();
            outToTarget.writeBytes(contents);
            socket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // used to send messages to target node by address
    private void send(JSONObject toSend, InetSocketAddress targetNode) {
        String targetAdd = targetNode.toString();

        try {
            Socket socket = new Socket(targetAdd, 8767);
            DataOutputStream outToTarget = new DataOutputStream(socket.getOutputStream());
            String contents = toSend.toString();
            outToTarget.writeBytes(contents);
            socket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // used to create new thread for new node
    private class Handler extends Thread{
        private Socket socket;
        private BufferedReader inFromUser;
        public String inSentence;
        private JSONObject data;
        private String ipAddress;

        public Handler(Socket soc){

            this.socket=soc;

            try{
                inFromUser = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                inSentence = inFromUser.readLine();
                data = new JSONObject(inSentence);
            }catch(IOException ie){
                ie.printStackTrace();
            }catch(JSONException je){
                je.printStackTrace();
            }

        }

        public void run() {
            JSONObject toSend;
            try {
                String s = data.getString("type");
                if (s.equals("JOINING_NETWORK")) {
                    toSend = new JSONObject();
                    toSend.put("type", "JOINING_NETWORK");
                    toSend.put("node_id", data.getString("node_id"));
                    toSend.put("ip_address", data.getString("ip_address"));

                    send(toSend, Long.getLong(data.getString("node_id")));
                    // all communication will be via TCP to port 8767
                    routingTable.put(Long.getLong(data.getString("node_id")), new InetSocketAddress(data.getString("ip_address"), 8767));

                } else if (s.equals("JOINING_NETWORK_RELAY")) {
                    // if the node is not the target node, forward the JOINING_NETWORK_RELAY to the gateway
                    if (findNearest(Long.getLong(data.getString("node_id"))) != nodeID) {
                        toSend = new JSONObject();
                        toSend.put("type", "JOINING_NETWORK_RELAY");
                        toSend.put("node_id", data.getString("node_id"));
                        toSend.put("gateway_id", data.getString("gateway_id"));

                        send(toSend, Long.getLong(data.getString("node_id")));
                    }
                    toSend = new JSONObject();

                    // forward the ROUTING_INFO to the gateway node, let the gateway node send the message to the joining node
                    LinkedList<LinkedHashMap<String, String>> l1 = new LinkedList<LinkedHashMap<String, String>>();
                    toSend.put("type", "ROUTING_INFO");
                    toSend.put("node_id", data.getString("node_id"));
                    toSend.put("gateway_id", data.getString("gateway_id"));
                    toSend.put("ip_address", InetAddress.getLocalHost()
                            .toString());

                    for (Entry<Long, InetSocketAddress> entry : routingTable
                            .entrySet()) {
                        LinkedHashMap<String, String> m1 = new LinkedHashMap<String, String>();
                        m1.put("node_id", Long.toString(entry.getKey()));
                        m1.put("ip_address", entry.getValue().getAddress()
                                .toString());
                        l1.add(m1);
                    }
                    toSend.put("route_table", l1);

                    send(toSend, Long.getLong(data.getString("gateway_id")));

                } else if (s.equals("ROUTING_INFO")) {
                    if (Long.getLong(data.getString("gateway_id")) == nodeID) {
                        send(data, Long.getLong(data.getString("node_id")));
                    } else if (Long.getLong(data.getString("node_id")) == nodeID) {

                    } else {
                        send(data, Long.getLong(data.getString("gateway_id")));
                    }

                    // all nodes add routing info to their routing table
                    JSONArray table = data.getJSONArray("route_table");
                    for (int i = 0; i < table.length(); i++) {
                        JSONObject entry = table.getJSONObject(i);
                        routingTable.put(Long.valueOf(entry
                                .getString("node_id")), new InetSocketAddress(
                                entry.getString("ip_address"), 8767));
                    }

                } else if (s.equals("LEAVING_NETWORK")) {
                    routingTable
                            .remove(Long.valueOf(data.getString("node_id")));

                } else if (s.equals("CHAT")) {
                    if (findNearest(Long.getLong(data.getString("target_id"))) == nodeID) {
                        JSONArray chat = data.getJSONArray("text");
                        if (!chatTable.containsKey(data.getString("tag"))) {
                            chatTable.put(data.getString("tag"),
                                    new HashMap<String, Integer>());
                        }
                        for (int i = 0; i < chat.length(); i++) {
                            if (chatTable.get(data.getString("tag"))
                                    .containsKey(chat.getString(i))) {
                                int tmp = chatTable.get(
                                        data.getString("tag")).get(
                                        chat.getString(i));
                                chatTable.get(data.getString("tag")).put(
                                        chat.getString(i), tmp++);
                            }
                            chatTable.get(data.getString("tag")).put(
                                    chat.getString(i), 1);
                        }

                        toSend = new JSONObject();
                        toSend.put("type", "ACK_CHAT");
                        toSend.put("node_id", (String) data.get("target_id"));
                        toSend.put("tag", data.getString("tag"));
                        send(toSend, Long.valueOf(data.getString("sender_id")));

                    } else {
                        send(data, Long.getLong(data.getString("target_id")));
                    }

                } else if (s.equals("CHAT_RETRIEVE")) {
                    if (findNearest(Long.getLong(data.getString("node_id"))) == nodeID) {
                        toSend = new JSONObject();
                        LinkedList<LinkedHashMap<String, String>> l2 = new LinkedList<LinkedHashMap<String, String>>();
                        toSend.put("type", "CHAT_RESPONSE");
                        toSend.put("tag", data.getString("tag"));
                        toSend.put("node_id", data.getString("sender_id"));
                        toSend.put("sender_id", Long.toString(nodeID));
                        for (Entry<String, Integer> entry : chatTable.get(
                                data.getString("tag")).entrySet()) {
                            LinkedHashMap<String, String> m1 = new LinkedHashMap<String, String>();
                            m1.put("url", entry.getKey());
                            m1.put("rank", Integer.toString(entry.getValue()));
                            l2.add(m1);
                        }
                        toSend.put("route_table", l2);
                    } else {
                        send(data, Long.getLong(data.getString("node_id")));
                    }

                } else if (s.equals("CHAT_RESPONSE")) {
                    if (findNearest(Long.getLong(data.getString("node_id"))) == nodeID) {
                        toSend = new JSONObject();
                        toSend.put("type", "ACK_CHAT");
                        toSend.put("node_id", (String) data.get("target_id"));
                        toSend.put("tag", data.getString("tag"));
                        send(toSend, Long.valueOf(data.getString("sender_id")));

                        JSONArray result = data.getJSONArray("response");
                        String[] resultArray = new String[result.length()];
                        for (int i = 0; i < result.length(); i++) {
                            resultArray[i] = result.getString(i);

                        }
                        chatResult.add(new ChatResult(data.getString("tag"), resultArray));

                    } else {
                        send(data, Long.getLong(data.getString("node_id")));
                    }

                } else if (s.equals("PING")) {
                    InetSocketAddress toAddress;

                    toSend = new JSONObject();
                    toSend.put("type", "ACK");
                    toSend.put("node_id", (String) data.get("target_id"));
                    toSend.put("ip_address", InetAddress.getLocalHost().getHostAddress().toString());
                    toAddress = new InetSocketAddress(
                            InetAddress.getByName((String) data
                                    .get("ip_address")), 8767);

                    send(toSend, toAddress);
                    System.out.println("Sending: " + toSend.toString());

                    long to = findNearest(Long.parseLong((String) data
                            .get("target_id")));

                    if (nodeID != to) {
                        toSend = new JSONObject();
                        toSend.put("type", "PING");
                        toSend.put("target_id", (String) data.get("target_id"));
                        toSend.put("sender_id", (String) data.get("sender_id"));
                        toSend.put("ip_address", InetAddress.getLocalHost().getHostAddress().toString());

                        toAddress = new InetSocketAddress(
                                InetAddress.getByName((String) data
                                        .get("ip_address")), 8767);

                        send(toSend, toAddress);
                        System.out.println("Sending: " + toSend.toString());

                        // Wait for ACK
                        synchronized (pingACK) {
                            pingACK.put((String) toSend.get("ip_address"),
                                    false);
                        }
                        System.out.println("ping" + pingACK.toString());
                        Thread.sleep(10000);
                        // If no ACK - assume target node is dead - remove from
                        // routing table.
                        synchronized (pingACK) {

                            if (!pingACK.get((String) toSend.get("ip_address"))) {
                                routingTable.remove(to);
                                System.out.println("Node removed");
                            } else {
                                System.out.println("Ping ACKed");
                            }
                        }
                    }

                } else if (s.equals("ACK_CHAT")) {
                } else if (s.equals("ACK")) {
                    synchronized (pingACK) {
                        if (pingACK
                                .containsKey((String) data.get("ip_address")))
                            pingACK.put((String) data.get("ip_address"), true);
                        System.out.println("ACK Recieved: " + data.toString());
                    }

                } else {
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }

        }
    }
}


