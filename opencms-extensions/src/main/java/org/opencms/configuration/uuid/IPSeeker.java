package org.opencms.configuration.uuid;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IPSeeker {
	public static String getIPAddress() {

		Enumeration<NetworkInterface> nicList;
		NetworkInterface nic;
		Enumeration<InetAddress> nicAddrList;
		InetAddress nicAddr;
		try {
			nicList = NetworkInterface.getNetworkInterfaces();
			while (nicList.hasMoreElements()) {
				nic = nicList.nextElement();
				if (!nic.isLoopback() && nic.isUp()) {
					byte[] hardware = nic.getHardwareAddress();
					if (hardware != null && hardware.length == 6
							&& hardware[1] != (byte) 0xff)
					{
						nicAddrList = nic.getInetAddresses();
						while (nicAddrList.hasMoreElements()) {
							nicAddr = nicAddrList.nextElement();
							if (nicAddr instanceof Inet4Address) { 
								try {
									return nicAddr.getHostAddress();
								} catch (Exception e) {
								}
							} 


						}
					}
				}
			}
		} catch (SocketException e1) {
			System.out
			.println("SocketException handled in Networking.getIPAddress!.");
		}
		return "";
	}

}
