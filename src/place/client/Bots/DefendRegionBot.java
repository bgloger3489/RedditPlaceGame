package place.client.Bots;

import place.PlaceColor;
import place.PlaceTile;
import place.client.ptui.ConsoleApplication;

import static java.lang.Thread.sleep;


/**
 * AUTHOR: BENJAMIN GLOGER
 *
 * A place bot that defends a region
 */
public class DefendRegionBot extends AbstractBot {//ericben999!
    private int TR;
    private int TC;
    private int BR;
    private int BC;
    private PlaceColor[][] region;

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
        System.out.println("Enter coordinates of a region to defend in the form: <top row> <top col> <bottom row> <bottom col>");
        TR = systemIn.nextInt();
        TC = systemIn.nextInt();
        BR = systemIn.nextInt();
        BC = systemIn.nextInt();


        while(!(TR < DIM && TC < DIM && TC >= 0 && TR >= 0 && BR < DIM && BC < DIM && BC >= 0 && BR >= 0 && TR<BR && TC<BC)){
            System.out.println("Enter coordinates of a region to defend in the form: <top row> <top col> <bottom row> <bottom col");
            TR = systemIn.nextInt();
            TC = systemIn.nextInt();
            BR = systemIn.nextInt();
            BC = systemIn.nextInt();
        }
        System.out.println(""+TR+ TC+ BR+ BC);
    }

    /**
     * This method runs the main loop of the client and adds the client to the model board
     * as an observer. It watches for changes in region, and corrects them.
     */
    public synchronized void run(){
        this.model.addObserver(this);

        region = new PlaceColor[BR-TR+1][BC-TC+1];


        //capture region
        for(int i =TR ; i<= BR; i++){
            for(int j = TC; j <= BC; j++){
                region[i-TR][j-TC] = model.getBoard().getBoard()[i][j].getColor();
            }
        }

        //main loop protecc
        while (isActive && serverConn.isRunning()) { // The program only runs the changeTile method when the
            // client is still connected and active and if the NetworkClient is still running
            for(int i =TR ; i<= BR; i++){
                for(int j = TC; j <= BC; j++){
                    if(model.getBoard().getTile(i,j).getColor() != region[i-TR][j-TC]){
                        changeTile(i,j,region[i-TR][j-TC]);
                    }
                }
            }
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
    public void changeTile(int row, int col, PlaceColor color){
        PlaceTile new_tile = new PlaceTile(row, col, username, color);
        serverConn.updateTile(new_tile); //Sends the tile to the NetworkClient so that they can update the board
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
        ConsoleApplication.launch(DefendRegionBot.class, args);
    }
}
