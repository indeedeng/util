package com.indeed.util.core;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author ketan
 *
 */
public class NetUtils {
    private static final Logger log = Logger.getLogger(NetUtils.class);
    private static volatile Optional<String> OPT_HOSTNAME = Optional.absent();

    /**
     * Determine the hostname of the machine that we are on. Do not allow, blank or 0.0.0.0
     * as valid hostnames. The host name will be save into the OPT_HOSTNAME static variable.
     *
     * We should not have to worry about threading, because the worst that should happen is multiple threads
     * lookup the hostname at the same time, and its pointer is changed multiple times. The optional is immutable.
     *
     * @return hostname
     * @throws java.net.UnknownHostException unable to lookup a host name correctly
     */
    @Nonnull
    public static String determineHostName() throws UnknownHostException {
        if (!OPT_HOSTNAME.isPresent()) {
            final String hostName = InetAddress.getLocalHost().getHostName();
            if (Strings.isNullOrEmpty(hostName)) {
                throw new UnknownHostException("Unable to lookup localhost, got back empty hostname");
            }
            if (Strings.nullToEmpty(hostName).equals("0.0.0.0")) {
                throw new UnknownHostException("Unable to resolve correct hostname saw bad host");
            }
            OPT_HOSTNAME = Optional.of(hostName);
            return OPT_HOSTNAME.get();
        } else {
            return OPT_HOSTNAME.get();
        }
    }

    /**
     * Same as determineHostName, but will use default value instead of throwing UnknownHostException
     * @param defaultValue
     * @return
     */
    @Nonnull
    public static String determineHostName(@Nonnull final String defaultValue) {
        checkNotNull(defaultValue, "Unable to use default value of null for hostname");
        if (!OPT_HOSTNAME.isPresent()) {
            try {
                return determineHostName(); // this will get and save it.
            } catch (final UnknownHostException e) {
                log.error("Unable to find hostname " + e.getMessage(), e); // exception to pick up in logsig
            }
        }
        return OPT_HOSTNAME.or(defaultValue);
    }

    /**
     * Make a best effort to determine the IP address of this machine
     */
    public static String determineIpAddress() throws SocketException {
        for (final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces(); networkInterfaces.hasMoreElements();) {
            final NetworkInterface nextInterface = networkInterfaces.nextElement();
            if (! nextInterface.isLoopback() && ! nextInterface.isVirtual() && ! nextInterface.isPointToPoint()) {
                for (final Enumeration<InetAddress> addresses = nextInterface.getInetAddresses(); addresses.hasMoreElements(); ) {
                    final InetAddress inetAddress = addresses.nextElement();
                    final byte[] address = inetAddress.getAddress();
                    if ((address.length == 4) //  we don't need no steenking IPv6
                                && (address[0] != 127 || address[1] != 0)) {    //  don't want localhost IP
                        return inetAddress.getHostAddress();
                    }
                }
            }
        }
        return null;
    }

    private final static Pattern IPV4_ADDRESS = Pattern.compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})");

    /**
     * Converts an IPv4 address to raw bytes, returning a byte[4], or null if the input is malformed.
     */
    public static byte[] getRawAddress(String ipv4Address) {
        Matcher m = IPV4_ADDRESS.matcher(ipv4Address);
        if (!m.find()) {
            return null;
        }
        byte[] addr = new byte[4];
        for (int i = 0; i < 4; i++) {
            int intVal = Integer.parseInt(m.group(i+1)) & 0x00ff;
            addr[i] = (byte) intVal;
        }
        return addr;
    }
}
