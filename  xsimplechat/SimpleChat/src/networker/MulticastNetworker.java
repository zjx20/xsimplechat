package networker;

import model.*;

import java.io.*;
import java.net.*;

/**
 * <p>����UDP�Ķಥnetworker</p>
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

	private boolean startSign = false, endSign = false;
	private ByteArrayOutputStream tempStream = new ByteArrayOutputStream();

	@Override
	protected void receiveData() {
		try {
			if (rSocket == null) {
				rSocket = new MulticastSocket(DEFAULT_PORT);
				rSocket.joinGroup(group);
				//rSocket.setLoopbackMode(true); //�����ձ�����Ϣ
			}

			byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			rSocket.receive(packet);
			int amount = packet.getLength();
			if (amount <= 0)
				return;
			int i = 0;
			while (i < amount) {
				if (startSign ^ endSign) {
					int s = i;
					for (; i < amount; i++)
						if (buf[i] == END_OF_TRANSMISSION)
							break;
					tempStream.write(buf, s, i - s);
					if (i < amount) {
						endSign = !endSign;
						controler.processRawData(decodeForReceive(tempStream.toByteArray()));
						tempStream.reset();
						i++;
					}
				} else {
					for (; i < amount; i++)
						if (buf[i] == START_OF_HEADER)
							break;
					if (i < amount) {
						startSign = !startSign;
						i++;
					}
				}
			}
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
		while (count < DEFAULT_RETRY_TIME) { // ����3��
			try {
				if (sSocket == null) {
					sSocket = new MulticastSocket();
					sSocket.joinGroup(group);
				}
				byte[] buf = encodeForSend(s.getMsg());
				DatagramPacket packet = new DatagramPacket(buf, buf.length, group, DEFAULT_PORT);
				sSocket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("�ಥ����ʧ�ܣ�" + DEFAULT_INTERVAL / 1000 + "�������");
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
