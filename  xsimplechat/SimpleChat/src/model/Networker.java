package model;

import java.util.concurrent.*;

public abstract class Networker {

	public static final int DEFAULT_INTERVAL = 3000;
	public static final int DEFAULT_RETRY_TIME = 3;
	public static final int DEFAULT_TIMEOUT = 10000;
	public static final int DEFAULT_PORT = 2009;
	public static final int DEFAULT_BUFFER_SIZE = 1024;

	protected boolean finish;
	protected Controler controler;
	protected BlockingQueue<SendNode> sendQueue;

	protected Networker() {
		this(null);
	}

	protected Networker(Controler controler) {
		this.controler = controler;
		sendQueue = new LinkedBlockingQueue<SendNode>();
		finish = false;
		new NetworkSender(this).start();
		new NetworkListener(this).start();
	}

	public void send(int sid, String s) {
		send(sid, s.getBytes());
	}

	public void send(int sid, byte[] buf) {
		try {
			sendQueue.put(new SendNode(sid, buf));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public abstract void closeNetworker();

	protected abstract void sendMessage();

	protected abstract void receiveMessage();

	protected class NetworkSender extends Thread {
		private Networker networker;

		protected NetworkSender(Networker networker) {
			this.networker = networker;
		}

		@Override
		public void run() {
			while (!networker.finish)
				networker.sendMessage();
		}
	}

	protected class NetworkListener extends Thread {
		private Networker networker;

		public NetworkListener(Networker networker) {
			this.networker = networker;
		}

		@Override
		public void run() {
			while (!networker.finish)
				networker.receiveMessage();
		}
	}
}
