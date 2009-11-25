package demo;

import controler.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.*;

import util.Toolkit;

import networker.MulticastNetworker;

import model.ClientInfo;
import model.Networker;

public class Demo3 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String ip="172.16.34.245";
		String nickname="X";
		

		new MainControler();
		try {
			cheatclient=new ClientInfo(UUID.randomUUID().toString(),nickname,InetAddress.getByName(ip),5432);
			group = InetAddress.getByName(MulticastNetworker.DEFAULT_GROUP_IPADDRESS);
			sSocket = new MulticastSocket();
			sSocket.joinGroup(group);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		id = ClientInfo.generateIdentifyInfo(cheatclient);
		heart=Toolkit.generateSendData(MainControler.HEADER_HEARTBEAT, id);
		Timer sendTimer = new Timer();
		sendTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				sendHeartbeat();
			}
		}, 0, MainControler.TIME_SEND_HEARTBEAT);
	}

	private static InetAddress group;
	static MulticastSocket sSocket;
	static byte[] id;
	static ClientInfo cheatclient;
	static byte[] heart;

	private static void sendHeartbeat() {
		byte[] buf = Networker.encodeForSend(heart);
		DatagramPacket packet = new DatagramPacket(buf, buf.length, group, Networker.DEFAULT_PORT);
		try {
			sSocket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
