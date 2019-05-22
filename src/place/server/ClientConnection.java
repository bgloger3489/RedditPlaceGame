package place.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/***
 * AUTHOR: Benjamin Gloger
 *
 * A simple class that encapsulates all the IO for the client
 */
public class ClientConnection {
    private Socket socket;
    private String username;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    /**
     * Constructor
     * @param s Socket
     * @param user username
     * @param in ObjectInputStream
     * @param out ObjectOutputStream
     */
    public ClientConnection(Socket s, String user, ObjectInputStream in, ObjectOutputStream out){
        socket = s;
        username = user;
        this.in = in;
        this.out = out;
    }


    /**
     * Getter method for Socket
     * @return
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Getter method for username
     * @return
     */
    public String getUsername() {
        return username;
    }

    /**
     * Getter method for ObjectOutputStream
     * @return
     */
    public ObjectOutputStream getOut() {
        return out;
    }

    /**
     * Getter method for ObjectInputStream
     * @return
     */
    public ObjectInputStream getIn() {
        return in;
    }
}
