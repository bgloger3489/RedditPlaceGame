package place.client.gui;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Toggle;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToggleButton;
import javafx.geometry.Insets;
import javafx.scene.control.Tooltip;
import javafx.stage.WindowEvent;
import place.ObservableBoard;
import place.PlaceBoard;
import place.PlaceColor;
import place.PlaceTile;
import place.client.NetworkClient;
import place.network.PlaceRequest;
import place.server.PlaceServer;

import java.util.Observable;
import java.util.Observer;

import static java.lang.Thread.sleep;

/**
 * This class creates a GUI client for place that uses NetworkClient to
 * connect to the server. The board is displayed as a GUI for users
 * where they can select tiles to change the tiles colors and
 * change the color they want to use using a group of ToggleButtons
 * at the bottom of the screen.
 * Note that the parameters for this method is:
 * --host=<i>hostname</i> --port=<i>portnumber</i> --username=<i><nameofuser/i>
 * @author Eric Chen
 */
public class PlaceGUI extends Application implements Observer {
    private String username;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private ObservableBoard model;
    private NetworkClient serverConn;
    private boolean isActive;
    private GridPane pane; // Contains the board of tiles
    private HBox hBox; // Contains the buttons used to change colors
    private int DIM;
    private PlaceColor colorSelected; //The current color selected by the user
    private static int SLEEP_TIME = 500;

    public PlaceGUI(){super();}

    /**
     * Creates the GUI the user can use to change and see tiles
     * @param primaryStage the stage that serves as the GUI for the user
     */
    @Override
    public void start(Stage primaryStage){
        pane = initGridPane();
        primaryStage.setTitle("Place: " + username);
        BorderPane border = new BorderPane();
        hBox = new HBox();
        initColorSelect(); // Creates the buttons of color selection and puts them in hBox
        border.setCenter(pane);
        border.setBottom(hBox);
        Scene scene = new Scene(border);
        primaryStage.setScene(scene);
        primaryStage.show();

        /**
         * Closes the client after the GUI is closed. -> For some reason PlaceGUI
         * wasn't closing naturally after closing the window so this was used
         * to fix the issue.
         */
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                stop();
                Platform.exit();
            }
        });
    }

    /**
     * When a tile is updated and changed this method is called to update the
     * GUI with the tile change.
     * @param o unused
     * @param arg the PlaceTile that was changed
     */
    @Override
    public void update(Observable o, Object arg) {
        if (pane != null){ // Prevents the update method from being called while the GUI is not finished being created to prevent issues
            PlaceTile tileChanged = (PlaceTile) arg;
            CoordRect rectChanged = null;

            /**
             * This finds and matches up the coordinates of the changed tile
             * with that of the CoordRect it represents.
             */
            for (Node tile : pane.getChildren()){
                if (pane.getRowIndex(tile) == tileChanged.getRow() && pane.getColumnIndex(tile) == tileChanged.getCol()){
                    rectChanged = (CoordRect) tile;
                    break;
                }
            }

            rectChanged.setFill(convertColor(model.getBoard().getTile(rectChanged.getI(), rectChanged.getJ()).getColor()));
            rectChanged.setTime(new Date(tileChanged.getTime())); //Changes and formats the time the tile was changed
            setTileInfo(rectChanged); // Updates the ToolTip of the tile
        }
    }

    /**
     * This method initialises the NetworkClient, logs into the server and sets up
     * the basic framework of the client.
     */
    @Override
    public void init(){
        // Get host info from command line
        Map<String, String> args = getParameters().getNamed();

        // get host info and username from command line
        String host = args.get("host");
        int port = Integer.parseInt(args.get("port"));
        username = args.get("username");
        isActive = true;
        colorSelected = PlaceColor.WHITE; // Defaults the selected color to White when the GUI is created

        try(
                Socket server = new Socket(host, port)
        ){

            output = new ObjectOutputStream(server.getOutputStream());
            input = new ObjectInputStream(server.getInputStream());

            serverConn = new NetworkClient(server, username, input, output);
            model = serverConn.getBoard();
            DIM = model.getBoard().DIM;
            this.run();
        }
        catch(IOException e){
            System.err.println(e + " @ init");
            stop();
        }
    }

    /**
     * This method runs the main loop of the client and adds the client to the model board
     * as an observer
     */
    public synchronized void run(){
        try{
            Platform.runLater(() -> start(new Stage())); // Platform.runlater() leads to the GUI being created in the proper JavaFX thread
        }
        catch(Exception e){
            System.err.println(e + " @ run");
            stop();
        }

        this.model.addObserver(this);

        while (isActive && serverConn.isRunning()) { // The program only runs the changeTile method when the
            // client is still connected and active and if the NetworkClient is still running
            try{
                this.wait();

                try {
                    sleep(SLEEP_TIME);
                }catch(InterruptedException e){
                    System.out.println(e);
                }
            }
            catch(InterruptedException e){
                System.err.println(e + " @ run");
                stop();
            }
        }
        stop();
    }

    /**
     * Stops the client and closes all of the streams.
     */
    public void stop(){
        try{
            serverConn.close();
            input.close();
            output.close();
            isActive = false;
            System.exit(0);
        }
        catch(IOException e){
            System.err.println(e + " @ stop");
            System.exit(-1);
        }
    }

    /**
     * Launch the JavaFX GUI.
     *
     * @param args not used, here, but named arguments are passed to the GUI.
     * Format:
     * --host=<i>hostname</i> --port=<i>portnumber</i> --username=<i><nameofuser/i>
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java PlaceGUI host port username");
            System.exit(-1);
        } else {
            Application.launch(args);
        }
    }

    /**
     * This method initializes the board of tiles and creates it in a GridPane
     * @return the GridPane of all the CoordRect that correspond with the board of tiles
     */
    private GridPane initGridPane(){
        GridPane gridPane = new GridPane();
        for(int i = 0; i < DIM; i++){
            for(int j =0; j < DIM; j++){
                CoordRect tile = new CoordRect(i,j, DIM);
                tile.setFill(convertColor(model.getBoard().getTile(i,j).getColor()));
                tile.setOnMouseClicked((event) -> buttonClick(tile, this));
                tile.setTime(new Date(model.getBoard().getTile(i, j).getTime())); //Sets and formats the time and date that the tile was created
                setTileInfo(tile); //Sets the ToolTip to the tile
                gridPane.add(tile, j, i);
            }
        }
        return gridPane;
    }

    /**
     * This method creates the bottom bar of selectable colors
     * the user can use. The usage of char is to use ASCII values
     * in order to make it easier to create the buttons.
     * @return the toggle group
     */
    private void initColorSelect(){
        ToggleGroup tGroup = new ToggleGroup();
        for (int i = 48; i < 58; i++){ //48 -> 58 represent the ASCII values for 0-9
            createColorButton((char)i, tGroup);
        }
        for (int j = 65; j < 71; j++){ //65 -> 71 represent the ASCII values for A-F
            createColorButton((char)j, tGroup);
        }
    }

    /**
     * This method creates the individual buttons that change
     * the color the user is using in the GUI
     * @param character the hexadecimal number 0-F that corresponds to a color selectable
     * @param tGroup the ToggleGroup that all of the buttons will be placed in
     */
    private void createColorButton(char character, ToggleGroup tGroup){
        String text = Character.toString(character); //Converts the char into the actual string representation
        ToggleButton new_button = new ToggleButton(text);

        // If the color is too light for the text to displayed in white it is displayed in black instead
        // using CSS. The base color of the button (-fx-base) corresponds to that of text of the button
        if (text.equals("1") || text.equals("2") || text.equals("3")
                || text.equals("7") || text.equals("9") || text.equals("B")){
            new_button.setStyle("-fx-text-fill: black; " +
                    "-fx-base:" + NetworkClient.checkColor(text).getName());
        }
        // Likewise if the color is too dark for the text to be in black, the text is displayed
        // in white
        else{
            new_button.setStyle("-fx-text-fill: white; " +
                    "-fx-base:" + NetworkClient.checkColor(text).getName());
        }

        new_button.setEffect(new DropShadow()); //Gives the buttons a little bit of dimension
        new_button.setPrefWidth(500/16); //Sets the size of the button

        // Changes the current color the user is using when selected
        // to that of the color of the button
        new_button.setOnAction((ActionEvent e) -> {
            colorSelected = NetworkClient.checkColor(text);
        });
        hBox.getChildren().add(new_button); //adds the button to the hBox for display
        new_button.setToggleGroup(tGroup); //adds the button to the ToggleGroup so only one button is active at a time
    }

    /**
     * This method sets the ToolTip that appears when the user hovers
     * over a tile with their cursor.
     * @param rect the tile that the ToolTip is being applied to
     */
    private void setTileInfo(CoordRect rect){
        Rectangle label = new Rectangle(15, 15);
        label.setFill(rect.getFill());
        Tooltip buttonInfo = new Tooltip("\nCoordinate: (" + rect.getI() + "," + rect.getJ() + ")\n"
                + "Owner: " + model.getBoard().getTile(rect.getI(), rect.getJ()).getOwner() + "\n"
                + "Time: " + rect.getTime() + "\n"
                + "Color: " + model.getBoard().getTile(rect.getI(), rect.getJ()).getColor().getName() +"\n");
        buttonInfo.setGraphic(label);
        Tooltip.install(rect, buttonInfo);
    }

    /**
     * Sets the action of sending the tile update to the server
     * to the tile
     * @param b the tile or rectangle that can be selected to change color
     */
    public static void buttonClick(CoordRect b, PlaceGUI thisGUI){
        PlaceTile new_tile = new PlaceTile(b.getI(), b.getJ(), thisGUI.username, thisGUI.colorSelected);
        thisGUI.serverConn.updateTile(new_tile);
    }

    /**
     * Converts the PlaceColor to a regular Color that can be
     * used for things that require Color over PlaceColor
     * @param color the PlaceColor to be converted
     * @return the PlaceColor converted to Color
     */
    public static Color convertColor(PlaceColor color){
        return Color.rgb(color.getRed(),color.getGreen(),color.getBlue());
    }
}