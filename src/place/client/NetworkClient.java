package place.client;

import place.ObservableBoard;
import place.PlaceBoard;
import place.PlaceColor;
import place.PlaceTile;
import place.network.PlaceRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.*;
import java.sql.Timestamp;

/**
 * This class acts as the controller of the MVC, and is responsible for connecting a client
 * to the server, as well as handling PlaceRequests from the server.
 * @author Eric Chen
 */
public class NetworkClient {
    private Socket server;
    private ObjectInputStream networkIn;
    private ObjectOutputStream networkOut;
    private boolean isActive;
    private ObservableBoard board;

    /**
     * Constructor for the NetworkClient. The NetworkClient
     * logins in the Client, gets the board from the server,
     * and runs itself in a separate thread to receive PlaceRequests
     * from the server.
     * @param sock Socket connection to server
     * @param username the username of the client
     * @param input Copies the ObjectInputStream input from the client
     * @param output Copies the ObjectOutputStream output from the client
     */
    public NetworkClient(Socket sock, String username, ObjectInputStream input, ObjectOutputStream output) {
        this.server = sock;
        networkOut = output;
        networkIn = input;
        try{
            PlaceRequest<String> login = new PlaceRequest<>(PlaceRequest.RequestType.LOGIN, username);
            networkOut.writeUnshared(login);
            networkOut.flush();

            PlaceRequest<?> login_result = (PlaceRequest<?>) networkIn.readUnshared(); //Receives information about login success

            if (login_result.getType().equals(PlaceRequest.RequestType.LOGIN_SUCCESS) // If login is successful, then creates the board
                    /*&& login_result.getData().equals("LOGIN_SUCCESS")*/){
                System.out.println("Login was successful!");
                PlaceRequest<PlaceBoard> new_board = (PlaceRequest<PlaceBoard>)input.readUnshared(); //Creates the board
                board = new ObservableBoard(new_board.getData()); //Creates the observable model of the board
                isActive = true;

                // Run rest of client in separate thread.
                // This threads stops on its own at the end and
                // does not need to rendezvous with other software components.
                Thread netThread = new Thread( () -> this.run() );
                netThread.start();
            }
            else if(login_result.getType().equals(PlaceRequest.RequestType.ERROR)){
                System.out.println("Login failed. Username was already taken.");
                stop();
                System.exit(0);
            }
        }
        catch(IOException e){
            System.err.println(e + " @ init");
            stop();
        }
        catch(ClassNotFoundException e){
            System.err.println(e + " @ init");
            stop();
        }
    }

    /**
     * Method used to check if NetworkClient is running by the client
     * @return if the NetworkClient is running
     */
    public synchronized boolean isRunning(){
        return this.isActive;
    }

    /**
     * Stops the NetworkClient's run() loop
     */
    private synchronized void stop() {
        this.isActive = false;
    }

    /**
     * Closes the NetworkClient and all of the input and output streams in it
     */
    public void close() {
        try {
            this.server.close();
            networkIn.close();
            networkOut.close();
        }
        catch( IOException e ) {
            System.err.println(e.getMessage() + " @ close");
        }
    }

    /**
     * Receives the new tile inputted by the client and sends it to the server so that it can
     * update the board
     * @param new_tile the tile sent by the client
     */
    public void updateTile(PlaceTile new_tile){
        try{
            networkOut.writeUnshared(new PlaceRequest<PlaceTile>(PlaceRequest.RequestType.CHANGE_TILE, new_tile));
            networkOut.flush();
        }
        catch(IOException e){
            System.err.println(e.getMessage() + " @ updateTile");
        }

    }

    /**
     * Getter function for the ObservableBoard board
     * @return the board
     */
    public ObservableBoard getBoard(){
        return board;
    }


    /**
     * This method continues running until there is an error or the client or server disconnects
     * While running it waits for the server to send in a new PlaceRequest and then does something
     * according to the PlaceRequest received.
     */
    private void run(){
        while(isActive){
            try{
                PlaceRequest<?> update = (PlaceRequest<?>) networkIn.readUnshared();

                switch (update.getType()){
                    case TILE_CHANGED: //If the server sent a message that the tile changed on the board,
                        // the NetworkClient updates its board and in turn notifies the client that the board updated
                        board.setBoard((PlaceTile) update.getData());
                        break;
                    case ERROR: //If the server sent an error the NetworkClient ends itself
                        System.err.println(update.getData());
                        this.stop();
                        break;
                }
            }
            catch(IOException e){
                System.out.println("This Client disconnected from the server.");
                this.stop();
            }
            catch(ClassNotFoundException e){
                System.err.println(e.getMessage() + " @ run");
                this.stop();
            }
        }
        this.close();
    }

    /**
     * This method converts PlaceColor numbers into
     * an actual PlaceColor that can be used by the server
     * and/or client.
     * @param color the "number" of the color inputted
     * @return the color converted to PlaceColor
     */
    public static PlaceColor checkColor (String color){
        switch (color){
            case "0":
                return PlaceColor.BLACK;
            case "1":
                return PlaceColor.GRAY;
            case "2":
                return PlaceColor.SILVER;
            case "3":
                return PlaceColor.WHITE;
            case "4":
                return PlaceColor.MAROON;
            case "5":
                return PlaceColor.RED;
            case "6":
                return PlaceColor.OLIVE;
            case "7":
                return PlaceColor.YELLOW;
            case "8":
                return PlaceColor.GREEN;
            case "9":
                return PlaceColor.LIME;
            case "A":
                return PlaceColor.TEAL;
            case "B":
                return PlaceColor.AQUA;
            case "C":
                return PlaceColor.NAVY;
            case "D":
                return PlaceColor.BLUE;
            case "E":
                return PlaceColor.PURPLE;
            case "F":
                return PlaceColor.FUCHSIA;
        }
        return null;
    }

    /**
     * Getter method for the server Socket
     * @return the server Socket
     */
    public Socket getServer() {
        return server;
    }
}
