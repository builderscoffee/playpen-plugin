package eu.builderscoffee.playpen.utils;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

@UtilityClass
public class PortUtils {

    /**
     * Checks to see if a specific port is available.
     *
     * @param port the port to check for availability
     */
    public static boolean available(int port) {
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {}
            }
        }

        return false;
    }
}
