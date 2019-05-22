package place.client.Bots;

import place.PlaceTile;
import place.client.ptui.ConsoleApplication;

import java.util.Random;

import static java.lang.Thread.sleep;


/**
 * AUTHOR: BENJAMIN GLOGER
 *
 * This is a Place bot that moves around the board like a snake
 */
public class SnakeBot extends AbstractBot {//ericben999!
    int row;
    int col;
    private String color;
    int dx;
    int dy;
    //private static int SLEEP_TIME = 500;

    public void init(){
        super.init();
        System.out.println("server closed: "+serverConn.getServer().isClosed());
    }

    /**
     * Used to get input from the user for row, col, and color, Xvelocity and Yvelocity
     */
    @Override
    public void getInput() {
        System.out.println("Enter coordinates, color, and velocity for x & y in the form: <row> <col> <color> <-1, 1> <-1, 1>");
        row = systemIn.nextInt();
        col = systemIn.nextInt();
        color = systemIn.next();
        int dx = systemIn.nextInt();
        int dy = systemIn.nextInt();


        while(!(row < DIM && col < DIM && col >= 0 && row >= 0 && checkColor(color)!= null && (dx==1||dx==-1)&& (dy==1||dy==-1))){
            System.out.println("Enter coordinates, color, and velocity in the form: <row> <col> <color> <-1, 1> <-1, 1>");
            row = systemIn.nextInt();
            col = systemIn.nextInt();
            color = systemIn.next();
            dx = systemIn.nextInt();
            dy = systemIn.nextInt();
        }
    }

    /**
     * This method runs the main loop of the client and adds the client to the model board
     * as an observer. It randomly chooses integer velocities between 1, 0 and -1.
     */
    public synchronized void run(){
        this.model.addObserver(this);



        Random r = new Random();
        //int dx = r.nextInt(3) -1;
        //int dy = r.nextInt(3) -1;

        while(isActive && serverConn.isRunning()){
            this.changeTile();

            if(col == 0) {
                dy = r.nextInt(2);//0 or 1
                if(dx == 0){
                    dx = 1;
                }
            }else if(col == DIM -1){
                dy = r.nextInt(2)-1;//0 or -1
                if(dx == 0){
                    dx = 1;
                }
            }

            if(row == 0) {
                dx = r.nextInt(2); // 0 or 1
                if(dy == 0 && col != 0 && col != DIM-1){
                    dy = 1;
                }
            }else if(row == DIM -1){
                dx = r.nextInt(2)-1 ; // 0 or -1
                if(dy == 0 && col != 0 && col != DIM-1){
                    dy = 1;
                }
            }

            if(dy == 0 && dx == 0){
                if(row == 0) {
                    dx = 1; // 0 or 1
                }else if(row == DIM -1){
                    dx = -1 ; // 0 or -1
                }else{
                    dx = 1;
                }
            }

            System.out.println("row: "+row+" col: "+col+" dy: "+dy+" dx:"+dx);
            row+=dx;
            col+=dy;

            try {
                sleep(SLEEP_TIME);
            }catch(InterruptedException e){
                System.out.println(e);
            }

        }

        //endClient(); // When the client is disconnected the loop stops running and endClient is called
    }

    /**
     * This method asks for the tile the client wishes to change and sends the request to
     * the server.
     * Notes: When the server disconnects and/or ends, this method needs to finish (user needs to input something)
     * before the client completely ends - Looking at possible solutions for this
     */
    public void changeTile(){
        PlaceTile new_tile = new PlaceTile(row, col, username, checkColor(color));
        serverConn.updateTile(new_tile); //Sends the tile to the NetworkClient so that they can update the board
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java PlaceClient host port username");
            System.exit(-1);
        }
        ConsoleApplication.launch(SnakeBot.class, args);
    }
}
