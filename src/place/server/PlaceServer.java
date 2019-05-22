package place.server;

import place.PlaceException;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


/**
 * This is the PlaceServer class. It is responsible for accepting all new connections by clients.
 *
 * The class starts up a NetworkServer Thread on creation, and then listens for new connections
 * After a new client connects, the PlaceServer generates a new ClientServerThread for that user, and
 * passes the users information to both the ClientServerThread and the NetworkServer.
 */
public class PlaceServer implements Closeable {
    private ServerSocket server;
    private boolean isActive;
    private NetworkServer networkServer;
    private ArrayList<ClientServerThread> clientServerThreads;


    /**
     * Constructor for the PlaceServer. This is where the NetworkServer is created.
     * @param port int
     * @param DIM int, dimension of board
     * @throws PlaceException
     */
    public PlaceServer(int port, int DIM) throws PlaceException{
        //create new PlaceServer, make ServerSocket on port, and set isActive to true
        try{
            server = new ServerSocket(port);
        }catch(IOException e){
            throw new PlaceException(e + " @ PlaceServer constructor");
        }
        isActive = true;
        networkServer = new NetworkServer(DIM);
        clientServerThreads = new ArrayList<>();
    }

    /**
     * This method contains the main loop. It calls .accept() and waits for new client connections.
     * For each new connection, it will start a ClientServerThread, and pass the clients info to the NetworkServer.
     */
    public void run(){
        //begin accepting new clients, and creating a new ClientServerThread for each
        try {
            ClientServerThread temp;
            while (isActive) {
                //while sercer isActive, listen for new connections
                //for every new connection, create a new ClientServerThread and run it
                Socket client = server.accept();
                temp = new ClientServerThread(client, networkServer);
                clientServerThreads.add(temp);
                temp.start();
            }

            //terminate all the clients
            for (ClientServerThread clientServerThread: clientServerThreads) {
                clientServerThread.terminateClient();
            }
            //join all threads
            for (ClientServerThread clientServerThread: clientServerThreads) {
                try {
                    clientServerThread.join();
                }catch (InterruptedException e){
                    //System.err.println(e + " @ run");
                }
            }


        }catch(IOException e){
            //System.err.println(e + " @ run");
        }
    }

    /**
     * Main method for the PlaceServer
     * @param args
     */
    public static void main(String[] args) {
        //args- port DIM
        if(args.length != 2){
            System.err.println("Usage: java PlaceServer port DIM");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        int DIM = Integer.parseInt(args[1]);

        // create new PlaceServer adn run it
        try(
                PlaceServer placeServer = new PlaceServer(port, DIM);
                ){
            placeServer.run();

        }catch (PlaceException e){
            System.err.println(e + " @ main");
        }


    }

    /**
     * Closes the client {@link Socket}.
     */
    @Override
    public void close() {
        try {
            this.server.close();
        } catch (IOException e) {
            //System.err.println(e + " @ close");
        }
    }
}
