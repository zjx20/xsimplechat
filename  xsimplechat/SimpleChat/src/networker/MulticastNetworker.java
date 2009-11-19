package networker;

import model.*;

import java.io.*;
import java.net.*;

import util.Toolkit;

/**
 * <p>基于UDP的多播networker</p>
 * @author X
 *
 */
public class MulticastNetworker extends Networker {

	private static String DEFAULT_GROUP_IPADDRESS = "230.0.0.1";
	private MulticastSocket rSocket, sSocket;
	private InetAddress group;

	public MulticastNetworker(Controler controler) {
		super(controler);
		rSocket = sSocket = null;
		try {
			group = InetAddress.getByName(DEFAULT_GROUP_IPADDRESS);
		} catch (UnknownHostException e3) {
			e3.printStackTrace();
		}
	}

	@Override
	protected void receiveData() {
		try {
			if (rSocket == null) {
				rSocket = new MulticastSocket(DEFAULT_PORT);
				rSocket.joinGroup(group);
				//rSocket.setLoopbackMode(true); //不接收本机信息
			}

			byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			rSocket.receive(packet);
			System.out.println(packet.getLength());
			controler.processRawData((byte[]) Toolkit.fixArraySize(packet.getData(), packet
					.getLength()));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void sendData() {
		SendNode s = null;
		try {
			s = sendQueue.take();
		} catch (InterruptedException e2) {
			e2.printStackTrace();
			return;
		}
		int count = 0;
		while (count < DEFAULT_RETRY_TIME) { // 重试3次
			try {
				if (sSocket == null) {
					sSocket = new MulticastSocket();
					sSocket.joinGroup(group);
				}
				byte[] buf = s.getMsg();
				DatagramPacket packet = new DatagramPacket(buf, buf.length, group, DEFAULT_PORT);
				sSocket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("多播发送失败！" + DEFAULT_INTERVAL / 1000 + "秒后重试");
				try {
					Thread.sleep(DEFAULT_INTERVAL);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				count++;
				continue;
			}
			break;
		}
		controler.receipt(s.getSid(), count < DEFAULT_RETRY_TIME);
	}

	@Override
	public void closeNetworker() {
		super.closeNetworker();
		try {
			if (sSocket != null) {
				sSocket.leaveGroup(group);
				sSocket.close();
			}
			if (rSocket != null) {
				rSocket.leaveGroup(group);
				sSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
