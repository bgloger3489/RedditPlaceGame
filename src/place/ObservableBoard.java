package place;

import java.util.Observable;

/**
 * An observable class that "grants" PlaceBoard
 * Observable properties and allows it to notify observers when it updates
 * @author Eric Chen
 */
public class ObservableBoard extends Observable{
    // The PlaceBoard that become "observable" using this class
    private PlaceBoard board;

    /**
     * Constructor for the ObservableBoard that adds the PlaceBoard
     * to ObservableBoard to let it be "observable"
     * @param new_board the PlaceBoard that the ObservableBoard represents
     */
    public ObservableBoard(PlaceBoard new_board) {
        this.board = new_board;
    }

    /**
     * A getter method for the board that is stored in this class
     * @return board the PlaceBoard of tiles and information
     */
    public PlaceBoard getBoard() {
        return board;
    }

    /**
     * This method invokes the PlaceBoard's setTile function to change the board,
     * while also notifying observers that a change has occurred on the board, which will
     * tell the client and view to update itself. It sends the tile changed to the
     * observers.
     * @param tile the new tile that will update the board
     */
    public void setBoard(PlaceTile tile){
        board.setTile(tile);
        super.setChanged();
        super.notifyObservers(board.getTile(tile.getRow(), tile.getCol())); // Sends to the observers the tile that was changed
    }
}
