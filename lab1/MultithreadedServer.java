

/**
 * Created by yamin on 14-10-10.
 */


import java.net.*;
//import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//import java.util.concurrent.*;

public class MultithreadedServer {
    public static void main(String[] args) throws Exception {


        //ArrayList<Thread> ThreadPool = new ArrayList<Thread>();// create a pool to store threads
        //int Port = Integer.parseInt(args[0]);
    	
        ServerSocket server = new ServerSocket(6789);
        ExecutorService threadPool = Executors.newFixedThreadPool(3);// create a fixed size thread pool
        while(true) {

             // create a serverSocket
            Socket client = server.accept(); //connect with client and get client socket

            Thread newClient = new ConnectionThread(client,server);
            //ThreadPool.add(newClient);
            //newClient.start();
            threadPool.submit(newClient);
        }
        
    
        //System.exit(1); //exit if there are more than 10 threads
        //threadPool.execute(new ConnectionThread(client));// put new thread into the pool
    }
}








