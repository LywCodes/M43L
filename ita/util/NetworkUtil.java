package ita.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class NetworkUtil {
    private NetworkUtil() {throw new IllegalStateException("Utility class");}
    public static InetAddress getLocalHost() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getLocalIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

}
