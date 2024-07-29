package org.opencms.configuration.uuid;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.opencms.configuration.lang.Hex;

public class MacAddressSeeker {

    private static String macAddress = null;

    static {

	    try {
	        Class.forName("java.net.InterfaceAddress");
	        macAddress = Class.forName(
	                "com.tfsla.configuration.uuid.MacAddressSeeker$HardwareAddressLookup").newInstance().toString();
	    }
	    catch (ExceptionInInitializerError err) {
	        // Ignored.
	    }
	    catch (ClassNotFoundException ex) {
	        // Ignored.
	    }
	    catch (LinkageError err) {
	        // Ignored.
	    }
	    catch (IllegalAccessException ex) {
	        // Ignored.
	    }
	    catch (InstantiationException ex) {
	        // Ignored.
	    }
	    catch (SecurityException ex) {
	        // Ignored.
	    }

    if (macAddress == null) {

        Process p = null;
        BufferedReader in = null;

        try {
        	String osname = System.getProperty("os.name", "");

            if (osname.startsWith("Windows")) {
                p = Runtime.getRuntime().exec(
                        new String[] { "ipconfig", "/all" }, null);
            }
            // Solaris code must appear before the generic code
            else if (osname.startsWith("Solaris")
                    || osname.startsWith("SunOS")) {
                String hostName = getFirstLineOfCommand(
                        "uname", "-n" );
                if (hostName != null) {
                    p = Runtime.getRuntime().exec(
                            new String[] { "/usr/sbin/arp", hostName },
                            null);
                }
            }
            else if (new File("/usr/sbin/lanscan").exists()) {
                p = Runtime.getRuntime().exec(
                        new String[] { "/usr/sbin/lanscan" }, null);
            }
            else if (new File("/sbin/ifconfig").exists()) {
                p = Runtime.getRuntime().exec(
                        new String[] { "/sbin/ifconfig", "-a" }, null);
            }

            if (p != null) {
                in = new BufferedReader(new InputStreamReader(
                        p.getInputStream()), 128);
                String l = null;
                while ((l = in.readLine()) != null) {
                    macAddress = MACAddressParser.parse(l);
                    if (macAddress != null
                            && Hex.parseShort(macAddress) != 0xff) {
                        break;
                    }
                }
            }

        }
        catch (SecurityException ex) {
            // Ignore it.
        }
        catch (IOException ex) {
            // Ignore it.
        }
        finally {
            if (p != null) {
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (IOException ex) {
                        // Ignore it.
                    }
                }
                try {
                    p.getErrorStream().close();
                }
                catch (IOException ex) {
                    // Ignore it.
                }
                try {
                    p.getOutputStream().close();
                }
                catch (IOException ex) {
                    // Ignore it.
                }
                p.destroy();
            }
        }

    }

}
    static String getFirstLineOfCommand(String... commands) throws IOException {

        Process p = null;
        BufferedReader reader = null;

        try {
            p = Runtime.getRuntime().exec(commands);
            reader = new BufferedReader(new InputStreamReader(
                    p.getInputStream()), 128);

            return reader.readLine();
        }
        finally {
            if (p != null) {
                if (reader != null) {
                    try {
                        reader.close();
                    }
                    catch (IOException ex) {
                        // Ignore it.
                    }
                }
                try {
                    p.getErrorStream().close();
                }
                catch (IOException ex) {
                    // Ignore it.
                }
                try {
                    p.getOutputStream().close();
                }
                catch (IOException ex) {
                    // Ignore it.
                }
                p.destroy();
            }
        }

    }

    
    public static String getMACAddress() {
        return macAddress;
    }

    /**
     * Scans MAC addresses for good ones.
     */
    static class HardwareAddressLookup {

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            String out = null;
            try {
                Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
                if (ifs != null) {
                    while (ifs.hasMoreElements()) {
                        NetworkInterface iface = ifs.nextElement();
                        byte[] hardware = iface.getHardwareAddress();
                        if (hardware != null && hardware.length == 6
                                && hardware[1] != (byte) 0xff) {
                            out = Hex.append(new StringBuilder(36), hardware).toString();
                            break;
                        }
                    }
                }
            }
            catch (SocketException ex) {
                // Ignore it.
            }
            return out;
        }

    }

}
