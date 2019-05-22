package place.server;

import place.PlaceBoard;
import place.PlaceTile;
import place.network.PlaceRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * AUTHOR: BENJAMIN GLOGER
 *
 * This is the Network server class. It is a thread, that is responsible for sending messages to the clients
 */
public class NetworkServer {
    private HashMap<String, ClientConnection> users; //HashMap of all users
    private PlaceBoard board;

    /**
     * Constructor for NetworkServer
     * @param DIM
     */
    public NetworkServer(int DIM){
        users = new HashMap<>();
        board = new PlaceBoard(DIM);
    }

    /**
     * This method writes to the client that their login was successful, and is called by ClientServerThread
     * @param clientConnection
     */
    public void writeLoginSuccessful(ClientConnection clientConnection){
        try {
            clientConnection.getOut().writeUnshared(new PlaceRequest<>(PlaceRequest.RequestType.LOGIN_SUCCESS, "LOGIN_SUCCESS"));
            System.out.println("New Client: " + clientConnection.getUsername() +" "+clientConnection.getSocket().getInetAddress()+ "!");
        }catch(IOException e) {
            //System.err.println(e + " @ writeLoginSuccessful");
        }
    }

    /**
     * This method writes to the client that their login was unsuccessful, and is called by ClientServerThread
     * @param out
     */
    public void writeLoginUnsuccessful(ObjectOutputStream out){
        try {
            out.writeUnshared(new PlaceRequest<>(PlaceRequest.RequestType.ERROR, "LOGIN_FAILURE"));
        }catch(IOException e) {
            //System.err.println(e + " @ writeLoginUnsuccessful");
        }
    }

    /**
     * This method writes the Server's current PlaceBoard to the client
     * @param clientConnection ClientConnection
     * @throws IOException
     */
    public void sendBoardToClient(ClientConnection clientConnection) throws IOException{
        clientConnection.getOut().writeUnshared(new PlaceRequest<>(PlaceRequest.RequestType.BOARD, board));
    }

    /**
     * This method updates the board, after having been called by ClientServerThread after receiving a TILE_CHANGE
     * @param t
     */
    public synchronized void updateBoard(PlaceTile t){
        if(board.isValid(t)){
            t.setTime(System.currentTimeMillis());
            board.setTile(t);
            System.out.println("A tile was changed!");
            sendTileChangeToAllClients(t);
        }
    }

    /**
     * This method sends a TILE_CHANGED request to all clients after the board has been updated
     * @param t
     */
    public synchronized void sendTileChangeToAllClients(PlaceTile t){
        users.forEach((String username, ClientConnection cc) -> {
                try {
                    cc.getOut().writeUnshared(new PlaceRequest<>(PlaceRequest.RequestType.TILE_CHANGED, t));
                    cc.getOut().flush();
                }catch(IOException e){
                    //System.err.println(e + " @ sendTileChangeToAllClients "+ username);
                }
        });
    }

    /**
     * This method removes the user from the collection of connected users, after they disconnect
     * @param clientConnection
     */
    public synchronized void terminateClient(ClientConnection clientConnection){
        users.remove(clientConnection.getUsername());
    }

    /**
     * This method adds a new user to the collection of connected users, after they login
     * @param clientConnection
     */
    public synchronized void addNewClient(ClientConnection clientConnection){
        users.put(clientConnection.getUsername(), clientConnection);
    }

    /**
     * This method checks to see if a given username exists
     * @param s username
     * @return
     */
    public synchronized boolean usernameExists(String s){
        return users.containsKey(s);
    }
}
