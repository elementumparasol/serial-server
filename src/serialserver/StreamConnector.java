package serialserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;
import serialserver.util.Latch;

/**
 * Read from input stream and write to output stream 
 */
public class StreamConnector extends Thread {

    private static Logger log = Logger.getLogger(StreamConnector.class.getName());

    private InputStream in;
    private OutputStream out;

    private final boolean debug;
    private final Latch latch;
    private final boolean block;
    
    private volatile boolean should_exit = false;

    public StreamConnector(Latch _latch, InputStream _in, OutputStream _out, boolean _debug, boolean _block) {
        in = _in;
        out = _out;
        debug = _debug;
        latch = _latch;
        block = _block;
    }

    @Override
    public void run() {
        try {
            log.info("Starting connector thread #" + getId());
            while (!should_exit) {
                if(in.available() > 0 || this.block) {
                    int data = in.read();
                    if(data == -1) {
                        break;
                    }
                    out.write(data);
                } else {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                    }
                }
            }
            log.info("Normal exit thread #" + getId());
        } catch (IOException ex) {
            log.severe("Thread #" + getId() + " " + ex.getMessage());
        } finally {
            latch.decrement();
        }
    }

    public void exit() {
        should_exit = true;
    }
}
