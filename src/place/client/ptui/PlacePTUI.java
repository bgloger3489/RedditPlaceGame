package place.client.ptui;

import place.*;
import place.client.NetworkClient;
import place.network.PlaceRequest;
import place.server.NetworkServer;

import java.util.Observable;
import java.util.Observer;
import java.util.*;
import java.io.*;
import java.net.Socket;
import static java.lang.Thread.sleep;

/**
 * This class creates a PTUI client for place that uses NetworkClient to
 * connect to the server. The board is displayed as a matrix of numbers
 * corresponding to colors and the tiles changes through an input of
 * (<row><column><color>).
 * @author Eric Chen
 */
public class PlacePTUI extends ConsoleApplication implements Observer {//ericben999!
    private ObjectInputStream userIn;
    private ObjectOutputStream userOut;
    private Scanner systemIn;
    private NetworkClient serverConn;
    private ObservableBoard model;
    private boolean isActive;
    private String username;
    private static int SLEEP_TIME = 500;

    /**
     * This method initializes the PTUI, connects to the server through serverConn,
     * gets the board from the NetworkClient and runs the client.
     */
    public void init(){
        List<String> args = super.getArguments();

        String host = args.get(0);
        int port = Integer.parseInt(args.get(1));
        username = args.get(2);

        isActive = true;
        try(
                Socket server = new Socket(host, port)
                ){
                userOut = new ObjectOutputStream(server.getOutputStream());
                userIn = new ObjectInputStream(server.getInputStream());
                systemIn = new Scanner(System.in);
                serverConn = new NetworkClient(server, username, userIn, userOut);
                model = serverConn.getBoard();
                System.out.println(model.getBoard());
                this.run();
        }
        catch(IOException e){
            System.err.println(e + " @ init");
            endClient();
        }
    }

    /**
     * This method is unused in favor of using run() instead.
     * Included to follow ConsoleApplication.
     * @param consoleIn  the source of the user input
     * @param consoleOut the destination where text output should be printed
     */
    @Override
    public synchronized void go(Scanner consoleIn, PrintWriter consoleOut) {
    }

    /**
     * This method runs the main loop of the client and adds the client to the model board
     * as an observer. After a tile is changed the client sleeps before updating again.
     */
    public synchronized void run(){
        this.model.addObserver(this);
        while (isActive && serverConn.isRunning()) { // The program only runs the changeTile method when the
            // client is still connected and active and if the NetworkClient is still running
            this.changeTile();
            try {
                sleep(SLEEP_TIME);
            }catch(InterruptedException e){
                System.out.println(e);
            }
        }
        endClient(); // When the client is disconnected the loop stops running and endClient is called
    }

    /**
     * When the server updates the board and the board notifies observers,
     * the client updates their own board with the new tile
     * Note: This update method runs after the changeTile method runs
     * which doesn't cause issues, but makes the view a little strange looking
     * with the request for input coming before the actual board displays
     * - Seems consistent with how the instructors have the PTUI working,
     * so it doesn't seem to a major issue, but let me know if you think it is
     * @param o unused
     * @param arg the tile that was changed (unused)
     */
    @Override
    public void update(Observable o, Object arg){
        System.out.println(model.getBoard());
    }

    /**
     * This method asks for the tile the client wishes to change and sends the request to
     * the server so that the board can be updated.
     */
    private void changeTile(){
        boolean color_works = false;

        while (!color_works){
            System.out.print("Input the tile you wish to change (row col color): ");
            int row = systemIn.nextInt();
            int col = systemIn.nextInt();
            String color = systemIn.next();
            if (row == -1){
                color_works = true; // In this case color_works is set to true to end the loop and exit it ending the client
                endClient();
            }
            else{
                if (NetworkClient.checkColor(color) != null){ //Checks to see if the color inputted corresponds to a color available
                    color_works = true;
                    PlaceTile new_tile = new PlaceTile(row, col, username, NetworkClient.checkColor(color));
                    serverConn.updateTile(new_tile); //Sends the tile to the NetworkClient so that they can update the board
                }
                else{
                    System.out.println("Color invalid. Try again.");
                }
            }
        }
    }

    /**
     * Ends the client and closes all inputs and outputs.
     */
    private void endClient(){
        try{
            userIn.close();
            userOut.close();
            systemIn.close();
            isActive = false;
        }
        catch(IOException e){
            System.err.println(e + " @ endClient");
            System.exit(-1);
        }

    }

    /**
     * The main function that launches the PlacePTUI.
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java PlaceClient host port username");
            System.exit(-1);
        }
        ConsoleApplication.launch(PlacePTUI.class, args);
    }
}
