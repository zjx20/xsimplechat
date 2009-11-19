package controler;

import model.*;
import networker.*;
import server.*;
import ui.*;
import util.Toolkit;

import java.net.*;
import java.util.*;

public class MainControler extends Controler {

	public static final int TIME_CHECK_ALIVE = 2000;
	public static final int TIME_SEND_HEARTBEAT = 1500;
	public static final int TIME_JUDGE_DEAD = 5000; //�ж��ͻ��˶Ͽ�ʱ�䳤��
	public static final int SIZE_HEADER = 16;

	private String uuid = UUID.randomUUID().toString();
	private int localChatingPort;
	private InetAddress localIPAddress;
	private ChatingServer chatingServer;
	private Set<ClientInfo> existedClient = Collections.synchronizedSet(new HashSet<ClientInfo>());
	private Map<ClientInfo, Long> lastReceive = Collections
			.synchronizedMap(new HashMap<ClientInfo, Long>());
	private byte[] identifyInfo;
	private String nickName;

	public static final String HEADER_HEARTBEAT = Toolkit.padString("HEARTBEAT", SIZE_HEADER);
	public static final String HEADER_GROUPMESSAGE = Toolkit.padString("GROUPMESSAGE", SIZE_HEADER);

	public MainControler() {
		nickName = FrameMain.getNickName();
		if (nickName == null)
			System.exit(0);
		this.performer = new FrameMain(this);
		this.networker = new MulticastNetworker(this);
		try {
			chatingServer = new ChatingServer();
			chatingServer.start();
			localChatingPort = chatingServer.getLocalPort();
			localIPAddress = InetAddress.getLocalHost();

			ClientInfo.setCurrentClientInfo(new ClientInfo(uuid, nickName, localIPAddress,
					localChatingPort));
			identifyInfo = ClientInfo.getIdentifyInfo();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		Timer checkTimer = new Timer();
		checkTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				checkAlive();
			}
		}, TIME_CHECK_ALIVE, TIME_CHECK_ALIVE);

		Timer sendTimer = new Timer();
		sendTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				sendHeartbeat();
			}
		}, TIME_SEND_HEARTBEAT, TIME_SEND_HEARTBEAT);
	}

	protected void sendHeartbeat() {
		networker.send(0, Toolkit.generateSendData(HEADER_HEARTBEAT, identifyInfo));
	}

	protected void checkAlive() {
		Iterator<ClientInfo> it = existedClient.iterator();
		long now = new Date().getTime();
		while (it.hasNext()) {
			ClientInfo c = it.next();
			if (now - lastReceive.get(c).longValue() > TIME_JUDGE_DEAD) {
				// TODO ֪ͨ����
				lastReceive.remove(c);
				it.remove();
			}
		}
	}

	/**
	 * <p>���յ��ͻ�����Ϣ������������ʱ��</p>
	 * @param clientInfo �ͻ�����Ϣ
	 */
	private void receiveFromClient(ClientInfo clientInfo) {
		if (!existedClient.contains(clientInfo.getUuid())) {
			// TODO ��������
			existedClient.add(clientInfo);
		}
		lastReceive.put(clientInfo, new Long(new Date().getTime()));
	}

	@Override
	public void processRawData(byte[] buf) {
		// TODO Auto-generated method stub
		String header = new String(buf, 0, SIZE_HEADER);
		System.out.println("header:" + header);
		if (header.compareTo(HEADER_GROUPMESSAGE) == 0) {
			byte[] id = (byte[]) Toolkit.cutArray(buf, SIZE_HEADER, ClientInfo.SIZE_IDENTIFYINFO);
			ClientInfo clientInfo = ClientInfo.parseClientInfo(id);
			receiveFromClient(clientInfo);
			int pos = SIZE_HEADER + ClientInfo.SIZE_IDENTIFYINFO, len = buf.length - pos;
			Object[] params = new Object[2];
			params[0] = new String(buf, pos, len);
			params[1] = clientInfo;
			performer.updateUI(FrameMain.UPDATE_GROUPMESSAGE, params);
		} else if (header.compareTo(HEADER_HEARTBEAT) == 0) {
			byte[] id = (byte[]) Toolkit.cutArray(buf, SIZE_HEADER, ClientInfo.SIZE_IDENTIFYINFO);
			ClientInfo clientInfo = ClientInfo.parseClientInfo(id);
			receiveFromClient(clientInfo);
		}
	}

	public static final int AC_SEND_MESSAGE = 1;
	public static final int AC_CLOSE_WINDOW = 2;
	public static final int AC_SINGLE_TALK = 3;

	@Override
	public void processUIAction(int type, Object[] params) {
		switch (type) {
		case AC_SEND_MESSAGE:
			networker.send(0, Toolkit.generateSendData(HEADER_GROUPMESSAGE, (byte[]) Toolkit
					.mergeArray(identifyInfo, ((String) params[0]).getBytes())));
			break;
		}
	}

	@Override
	public void receipt(long sid, boolean result) {
		// TODO Auto-generated method stub

	}

}