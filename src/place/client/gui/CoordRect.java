package place.client.gui;

import javafx.scene.control.Button;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import place.PlaceColor;

import java.sql.Time;
import java.util.*;
import java.sql.Timestamp;

/**
 * AUTHOR: Benjamin Gloger
 */

/**
 * Class for CoordRect, essentially just a Rectangle, but holds other information -> I would've passed
 * the i and j info into the buttonClick method in, but they are not final
 * Using getTime returned only a set of numbers representing the time the PlaceTile was created
 * Date formats the information to include the date and time, so CoordRect stores it for convenience
 */
public class CoordRect extends Rectangle {

    //row and column
    private int i;
    private int j;

    // The time the rectangle was created
    private Date date;

    /**
     * Constructor for CoordRect
     * @param i - int, row
     * @param j - int, col
     */
    public CoordRect(int i, int j, int DIM){
        super(500/DIM,500/DIM);
        this.i = i;
        this.j = j;
    }

    /**
     * Getter method for i
     * @return - int, i
     */
    public int getI() {
        return i;
    }

    /**
     * Getter method for j
     * @return - int, j
     */
    public int getJ() {
        return j;
    }

    /**
     * Sets the date and time for when the CoordRect was created
     * @param newDate
     */
    public void setTime(Date newDate){
        date = newDate;
    }

    /**
     * Getter method for the date and time the CoordRect was created
     * @return
     */
    public Date getTime(){
        return date;
    }

}
