package place.client.Bots;

import place.PlaceTile;
import place.client.ptui.ConsoleApplication;

import static java.lang.Thread.sleep;


/**
 * AUTHOR: BENJAMIN GLOGER
 *
 * A place bot that defends a region
 */
public class DefendSquareBot extends AbstractBot {//ericben999!
    private int row;
    private int col;
    private String color;


    /**
     * Initializes the bot. Calls the init of the super class
     */
    public void init(){
        super.init();
        System.out.println("server closed: "+serverConn.getServer().isClosed());
    }

    /**
     * Used to get input from the user for row, col, and color
     */
    public void getInput(){
        System.out.println("Enter coordinates of square to defend in the form: <row> <col> <color>");
        row = systemIn.nextInt();
        col = systemIn.nextInt();
        color = systemIn.next();


        while(!(row < DIM && col < DIM && col >= 0 && row >= 0 && checkColor(color)!= null)){
            System.out.println("Enter coordinates of square to defend in the form: <row> <col> <color>");
            row = systemIn.nextInt();
            col = systemIn.nextInt();
            color = systemIn.next();
        }
    }

    /**
     * This method runs the main loop of the client and adds the client to the model board
     * as an observer. It checks for changes on the square, and corrects them.
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
     * This method asks for the tile the client wishes to change and sends the request to
     * the server.
     * Notes: When the server disconnects and/or ends, this method needs to finish (user needs to input something)
     * before the client completely ends - Looking at possible solutions for this
     */
    public void changeTile(){
            if (model.getBoard().getBoard()[row][col].getColor() != checkColor(color)){ //Checks to see if the color inputted corresponds to a color available
                PlaceTile new_tile = new PlaceTile(row, col, username, checkColor(color));
                serverConn.updateTile(new_tile); //Sends the tile to the NetworkClient so that they can update the board
            }
    }


    /**
     * Main method for the program
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java PlaceClient host port username");
            System.exit(-1);
        }
        ConsoleApplication.launch(DefendSquareBot.class, args);
    }
}
