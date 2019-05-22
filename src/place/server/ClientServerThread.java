package place.server;

import place.PlaceTile;
import place.network.PlaceRequest;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;
import java.sql.Timestamp;

/**
 * Class that is spawned for ever new client that logs in
 *
 * This class is a Thread responsible for receiving all messages from the client
 */
public class ClientServerThread extends Thread{
    private ClientConnection clientConnection;
    private Socket client;
    private NetworkServer networkServer;
    private boolean isActive;
    private final int SLEEP_TIME = 500;

    /**
     * Constructor for the ClientServerThread
     * @param client Socket
     * @param networkServer NetworkServer
     */
    public ClientServerThread(Socket client, NetworkServer networkServer){
        this.client = client;
        this.networkServer = networkServer;
        isActive = true;
    }

    /**
     * Method called when thread is started. It calls handleLogin, the enters the mainloop
     */
    public void run(){
        try(
                ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(client.getInputStream());
                ){

            handleLogin(client, out, in);
            if(isActive) {
                sendBoardToClient(clientConnection);
            }
            PlaceRequest<?> req;
            while(isActive && !client.isClosed()){
                //while the client is active, recieve tile_changed, update board, sleep 15

                //read in the most recent object!!!
                req = (PlaceRequest<?>) in.readUnshared();

                switch (req.getType()) {
                    case CHANGE_TILE:
                        // send tile change to board -> send tile change to all clients
                        networkServer.updateBoard((PlaceTile) req.getData());
                        sleep(SLEEP_TIME);
                        break;
                    case ERROR:
                        // terminate client, remove client from NetworkServer
                        terminateClient();
                        break;
                }
                sleep(SLEEP_TIME);

            }
            //terminateClient();
        }catch(EOFException e){
            System.out.println("Client  disconnected! : " + clientConnection.getUsername());
            terminateClient();
        }catch(IOException e){
            System.out.println("Client  disconnected! : " + clientConnection.getUsername());
            terminateClient();
        }catch(ClassNotFoundException e){
            System.out.println("Client  disconnected! : " + clientConnection.getUsername());
            terminateClient();
        }catch (InterruptedException e){
            System.out.println("Client  disconnected! : " + clientConnection.getUsername());
            terminateClient();
        }
    }

    /**
     * Handles the login process for a client
     * @param client Socket
     * @param out ObjectOutputStream
     * @param in ObjectInputStream
     * @throws IOException
     */
    public void handleLogin(Socket client, ObjectOutputStream out, ObjectInputStream in) throws IOException{
        try {
            Date date = new Date();
            long time = date.getTime();
            Timestamp ts = new Timestamp(time);
            System.out.println(ts);
            // recieve login request:
            PlaceRequest<?> req = (PlaceRequest<?>) in.readUnshared();

            // PlaceRequest must be type LOGIN, and username doesnt exist
            if(req.getType() == PlaceRequest.RequestType.LOGIN &&
                    !usernameExists((String) req.getData())){

                //create the object that stores all the clients stuff
                clientConnection = new ClientConnection(client, (String) req.getData(), in, out);


                //write to the client that login was successful
                networkServer.writeLoginSuccessful(clientConnection);
                networkServer.addNewClient(clientConnection);

            }else{ //if not stop thread
                networkServer.writeLoginUnsuccessful(out);
                isActive = false;
                //System.exit(0);
                System.out.println("username exists");
            }

        }catch(ClassNotFoundException e){
            //System.err.println(e.getMessage() + " @ handleLogin");
        }
    }

    /**
     * Responsible for closing all parts of the client's connection
     */
    public void terminateClient(){
        //close the socket, and its streams here, tell network server to remove them from the collections
        try {
            networkServer.terminateClient(clientConnection);
            clientConnection.getIn().close();
            clientConnection.getOut().close();
            clientConnection.getSocket().close();
        }catch(IOException e){
            //System.err.println(e + " @ terminateClient");
        }
    }

    /**
     * This method handles the sending of a board to a client
     * @param clientConnection ClientConnection
     * @throws IOException
     */
    public void sendBoardToClient(ClientConnection clientConnection) throws IOException{
        networkServer.sendBoardToClient(clientConnection);
    }

    /**
     * This method asks networkServer is that username is taken
     * @param s username
     * @return Whether the username exists or not
     */
    public boolean usernameExists(String s){
        return networkServer.usernameExists(s);
    }
}
