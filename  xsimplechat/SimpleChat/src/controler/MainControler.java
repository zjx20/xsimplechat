package controler;

import model.*;
import networker.*;
import server.*;
import ui.*;
import util.Toolkit;

import java.net.*;
import java.util.*;
import java.io.*;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class MainControler extends Controler {

	public static final int TIME_CHECK_ALIVE = 3000;
	public static final int TIME_SEND_HEARTBEAT = 2000;
	public static final int TIME_JUDGE_DEAD = 5000; //判定客户端断开时间长度
	public static final int SIZE_HEADER = 16;

	private String uuid = UUID.randomUUID().toString();
	private int localChatingPort;
	private InetAddress localIPAddress;
	private ChatingServer chatingServer;
	private Set<ClientInfo> existedClient = Collections.synchronizedSet(new HashSet<ClientInfo>());
	private static Set<ClientInfo> singleTalking = Collections
			.synchronizedSet(new HashSet<ClientInfo>());
	private Map<ClientInfo, Long> lastReceive = Collections
			.synchronizedMap(new HashMap<ClientInfo, Long>());
	private byte[] identifyInfo;
	private String nickName;
	private File selectfile;

	public static final String HEADER_HEARTBEAT = Toolkit.padString("HEARTBEAT", SIZE_HEADER);
	public static final String HEADER_GROUPMESSAGE = Toolkit.padString("GROUPMESSAGE", SIZE_HEADER);

	public MainControler() {
		nickName = FrameMain.getNickName();
		if (nickName == null)
			System.exit(0);
		this.performer = new FrameMain(this);
		this.networker = new MulticastNetworker(this);
		try {
			//chatingServer = new ChatingServer();
			chatingServer = new ChatingServer(5432);	//指定监听端口
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
		}, 0, TIME_SEND_HEARTBEAT);
	}

	/**
	 * <p>将c加入私聊集合中</p>
	 * @param c ClientInfo对象
	 */
	public static void addToSingleTalking(ClientInfo c) {
		singleTalking.add(c);
	}

	/**
	 * <p>将c移出私聊集合</p>
	 * @param c ClientInfo对象
	 */
	public static void removeFromSingleTalking(ClientInfo c) {
		singleTalking.remove(c);
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
				Object[] params = new Object[1];
				params[0] = c;
				performer.updateUI(FrameMain.UPDATE_LOGOUT, params);
				lastReceive.remove(c);
				it.remove();
			}
		}
	}

	/**
	 * <p>接收到客户端信息，更新最后接收时间</p>
	 * @param clientInfo 客户端信息
	 */
	private void receiveFromClient(ClientInfo clientInfo) {
		if (!existedClient.contains(clientInfo)) {
			Object[] params = new Object[1];
			params[0] = clientInfo;
			performer.updateUI(FrameMain.UPDATE_LOGIN, params);
			existedClient.add(clientInfo);
		}
		lastReceive.put(clientInfo, new Long(new Date().getTime()));
	}

	@Override
	public void processRawData(byte[] buf) {
		// TODO Auto-generated method stub
		String header = new String(buf, 0, SIZE_HEADER);
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

	public static final int AC_LOGIN = 1;
	public static final int AC_SEND_MESSAGE = 2;
	public static final int AC_CLOSE_WINDOW = 3;
	public static final int AC_SINGLE_TALK = 4;
	public static final int AC_SAVE_CHATLOG = 5;

	@Override
	public void processUIAction(int type, Object[] params) {
		switch (type) {
		case AC_LOGIN:
			performer.updateUI(FrameMain.UPDATE_LOGIN, params);
			break;
		case AC_SEND_MESSAGE:
			networker.send(0, Toolkit.generateSendData(HEADER_GROUPMESSAGE, (byte[]) Toolkit
					.mergeArray(identifyInfo, ((String) params[0]).getBytes())));
			break;
		case AC_SAVE_CHATLOG:
			JFileChooser jfc = new JFileChooser();
			int r = jfc.showDialog(null, "保存");
			if (r == JFileChooser.APPROVE_OPTION) {
				selectfile = jfc.getSelectedFile();
				try {
					FileWriter output = new FileWriter(selectfile.getPath() + ".txt");
					output.write((String) params[0]);
					output.close();
					JOptionPane.showMessageDialog(null, "保存完毕", "GUIDES",
							JOptionPane.INFORMATION_MESSAGE);
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(null, "The file does not exist.", "GUIDES",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
			break;
		case AC_CLOSE_WINDOW:
			performer.updateUI(FrameMain.UPDATE_CLOSEWINDOW, params);
			networker.closeNetworker();
			System.exit(0);
			break;
		case AC_SINGLE_TALK:
			ClientInfo target = (ClientInfo) params[0];
			if (singleTalking.contains(target))
				return;
			try {
				addToSingleTalking(target);
				Socket socket = new Socket(target.getIpAddress(), target.getPort());
				new ChatingControler(socket);
			} catch (IOException e) {
				removeFromSingleTalking(target);
				e.printStackTrace();
			}
			break;
		}
	}

	@Override
	public void receipt(long sid, boolean result) {
		// TODO Auto-generated method stub

	}

}