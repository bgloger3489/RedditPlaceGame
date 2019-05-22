package place.client.Bots;

import place.ObservableBoard;
import place.PlaceBoard;
import place.PlaceColor;
import place.PlaceTile;
import place.client.NetworkClient;
import place.client.ptui.ConsoleApplication;
import place.network.PlaceRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

import static java.lang.Thread.sleep;


/**
 * AUTHOR: BENJAMIN GLOGER
 *
 * This is the abstract class that all Place bots extend
 */
public abstract class AbstractBot extends ConsoleApplication implements Observer {//ericben999!
    protected ObjectInputStream input;
    protected ObjectOutputStream output;
    protected Scanner systemIn;
    protected NetworkClient serverConn;
    protected ObservableBoard model;
    protected boolean isActive;
    protected String username;
    protected int DIM;
    protected static int SLEEP_TIME = 500;
    protected int port;
    protected String host;

    // *** Your methods should have more descriptive names -> update vs refresh ???
    // *** Your update and refresh method call eachother recursively -> this is not good coding practice, as 1. you may run int memory issues with the stack 2. you wont ever break out of it, if a user wants to exit they should type exit not press the red square
    // *** Make sure you flush() whe writing to a stream

    /**
     * This method is responsible for logging the bot into the server
     */
    public void init() {
        List<String> args = super.getArguments();

        host = args.get(0);
        port = Integer.parseInt(args.get(1));
        username = args.get(2);

        isActive = true;
        try (
                Socket server = new Socket(host, port)
        ) {
            output = new ObjectOutputStream(server.getOutputStream()); // * These streams need to be closed -> put them into the ()s
            input = new ObjectInputStream(server.getInputStream());
            systemIn = new Scanner(System.in);
            serverConn = new NetworkClient(server, username, input, output);
            model = serverConn.getBoard();
            DIM = model.getBoard().DIM;
            System.out.println(model.getBoard());
            getInput();
            this.run();
        } catch (IOException e) {
            System.err.println(e + " @ init");
            endClient();
        }
    }

    /**
     * Called in initialize, so that the user can see the board after logging in, and input parameters into the bot
     */
    public abstract void getInput();

    /**
     * Used consoleApplication for ease of coding
     *
     * @param consoleIn
     * @param consoleOut
     */
    @Override
    public synchronized void go(Scanner consoleIn, PrintWriter consoleOut) {
    }

    /**
     * This method runs the main loop of the client and adds the client to the model board
     * as an observer
     */
    public abstract void run();


    /**
     * When the server updates the board and the board notifies observers,
     * the client updates their own board with the new tile
     * Note: This update method runs after the changeTile method runs
     * which doesn't cause issues, but makes the view a little strange looking
     * with the request for input coming before the actual board displays
     * - Seems consistent with how the instructors have the PTUI working,
     * so it doesn't seem to a major issue, but let me know if you think it is
     *
     * @param o
     * @param arg
     */
    @Override
    public void update(Observable o, Object arg) {
        System.out.println(model.getBoard());
    }

    /**
     * This method converts the color name the client inputted into
     * an actual PlaceColor that can be used by the server
     *
     * @param color the name of the color inputted
     * @return the color converted to PlaceColor
     */
    public static PlaceColor checkColor(String color) {
        switch (color) {
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
     * Ends the client and closes all inputs and outputs
     */
    public void endClient() {
        try {
            input.close();
            output.close();
            systemIn.close();
            isActive = false;
        } catch (IOException e) {
            System.err.println(e + " @ endClient");
            System.exit(-1);
        }

    }
}
