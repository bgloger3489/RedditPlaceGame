package place.client.Bots;

import place.PlaceTile;
import place.client.ptui.ConsoleApplication;

import static java.lang.Thread.sleep;


/**
 * AUTHOR: BENJAMIN GLOGER
 *
 * A Place bot for filling in the board
 */
public class FillBot extends AbstractBot {//ericben999!
    private String color;
    //private static int SLEEP_TIME = 500;

    public void init(){
        super.init();
        System.out.println("server closed: "+serverConn.getServer().isClosed());
    }

    /**
     * Used to get input from the user for color
     */
    @Override
    public void getInput() {
        System.out.println("Enter desired fill color in the form: <color>");
        color = systemIn.next();


        while(checkColor(color)== null){
            System.out.println("Enter desired fill color in the form: <color>");
            color = systemIn.next();
        }
    }

    /**
     * This method runs the main loop of the client and adds the client to the model board
     * as an observer. It goes down the row, down the col, filling in blocks with inputted color.
     */
    public synchronized void run(){
        this.model.addObserver(this);

        for (int i =0; i< DIM; i++ ) {
            for (int j = 0; j < DIM; j++) {
                this.changeTile(i, j);
                try {
                    sleep(SLEEP_TIME);
                }catch(InterruptedException e){
                    System.out.println(e);
                }
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
    public void changeTile(int row, int col){
        PlaceTile new_tile = new PlaceTile(row, col, username, checkColor(color));
        serverConn.updateTile(new_tile); //Sends the tile to the NetworkClient so that they can update the board
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java PlaceClient host port username");
            System.exit(-1);
        }
        ConsoleApplication.launch(FillBot.class, args);
    }
}
