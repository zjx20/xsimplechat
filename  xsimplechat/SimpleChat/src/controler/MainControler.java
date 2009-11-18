package controler;

import model.ClientInfo;
import model.Controler;
import networker.*;
import server.*;
import ui.*;
import util.Toolkit;

import java.util.*;

public class MainControler extends Controler {

	public static final int DEAD_TIME = 30000;	//判定客户端断开时间长度
	
	private String uuid;
	private int localChatingPort;
	private ChatingServer chatingServer;
	private HashSet<String> existedClient;
	private HashMap<String, Integer> lastedReceive;

	public static final String HEADER_HEARTBEAT = Toolkit.padString("HEARTBEAT", 16); 
	
	public MainControler() {
		this.networker = new MulticastNetworker(this);
		this.performer = new FrameMain(this);
		uuid = UUID.randomUUID().toString();
		try {
			chatingServer = new ChatingServer();
			chatingServer.start();
			localChatingPort = chatingServer.getLocalPort();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void downConnect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void processRawData(byte[] buf) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processUIAction(int type, Object[] params) {
		// TODO Auto-generated method stub

	}

	public void processUIActionSend(int type, Object[] params) {
		
	}
	@Override
	public void receipt(long sid, boolean result) {
		// TODO Auto-generated method stub

	}

}
