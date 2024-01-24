package com.indeed.util.core;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/** @author ketan */
public class NetUtils {
    private static final Logger log = LoggerFactory.getLogger(NetUtils.class);
    private static volatile Optional<String> OPT_HOSTNAME = Optional.absent();

    /**
     * Determine the hostname of the machine that we are on. Do not allow, blank or 0.0.0.0 as valid
     * hostnames. The host name will be save into the OPT_HOSTNAME static variable.
     *
     * <p>We should not have to worry about threading, because the worst that should happen is
     * multiple threads lookup the hostname at the same time, and its pointer is changed multiple
     * times. The optional is immutable.
     *
     * @return hostname
     * @throws java.net.UnknownHostException unable to lookup a host name correctly
     */
    @NonNull
    public static String determineHostName() throws UnknownHostException {
        if (!OPT_HOSTNAME.isPresent()) {
            final String hostName = InetAddress.getLocalHost().getHostName();
            if (Strings.isNullOrEmpty(hostName)) {
                throw new UnknownHostException(
                        "Unable to lookup localhost, got back empty hostname");
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
     * Same as determineHostName, but will use default value instead of throwing
     * UnknownHostException
     *
     * @param defaultValue The default hostname to use in the event one is not preset.
     * @return The detected hostname if present, or the default value.
     */
    @NonNull
    public static String determineHostName(@NonNull final String defaultValue) {
        checkNotNull(defaultValue, "Unable to use default value of null for hostname");
        if (!OPT_HOSTNAME.isPresent()) {
            try {
                return determineHostName(); // this will get and save it.
            } catch (final UnknownHostException e) {
                log.error(
                        "Unable to find hostname " + e.getMessage(),
                        e); // exception to pick up in logsig
            }
        }
        return OPT_HOSTNAME.or(defaultValue);
    }

    /**
     * Make a best effort to determine the IP address of this machine.
     *
     * @return The ip address of the machine if we could determine it. Null otherwise.
     * @throws SocketException In the event that we fail to extract an ip from a network interface.
     */
    @Nullable
    public static String determineIpAddress() throws SocketException {
        SocketException caughtException = null;
        for (final Enumeration<NetworkInterface> networkInterfaces =
                        NetworkInterface.getNetworkInterfaces();
                networkInterfaces.hasMoreElements(); ) {
            try {
                final NetworkInterface nextInterface = networkInterfaces.nextElement();
                if (!nextInterface.isLoopback()
                        && !nextInterface.isVirtual()
                        && !nextInterface.isPointToPoint()) {
                    for (final Enumeration<InetAddress> addresses =
                                    nextInterface.getInetAddresses();
                            addresses.hasMoreElements(); ) {
                        final InetAddress inetAddress = addresses.nextElement();
                        final byte[] address = inetAddress.getAddress();
                        if ((address.length == 4) //  we don't need no steenking IPv6
                                && ((address[0] != 127)
                                        || (address[1] != 0))) { //  don't want localhost IP
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            } catch (SocketException ex) {
                // We're not going to extract an IP address from this interface, so move on to the
                // next one.
                // If we find a qualifying interface, we won't care that some other interface gave
                // us problems.
                // If we don't find one, this exception might be informative.
                caughtException = ex;
            }
        }

        if (caughtException != null) {
            throw caughtException;
        }
        return null;
    }

    private static final Pattern IPV4_ADDRESS =
            Pattern.compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})");

    /**
     * Converts an IPv4 address to raw bytes, returning a byte[4], or null if the input is
     * malformed.
     *
     * @param ipv4Address The string representation of an ipv4 address.
     * @return A {@code byte[]} containing the parts of the v4 ip address.
     */
    public static byte[] getRawAddress(final String ipv4Address) {
        final Matcher m = IPV4_ADDRESS.matcher(ipv4Address);
        if (!m.find()) {
            return null;
        }
        final byte[] addr = new byte[4];
        for (int i = 0; i < 4; i++) {
            final int intVal = Integer.parseInt(m.group(i + 1)) & 0x00ff;
            addr[i] = (byte) intVal;
        }
        return addr;
    }
}
