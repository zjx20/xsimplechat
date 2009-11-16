package model;

import java.util.concurrent.*;

/**
 * <p>����ͨ�ų����࣬��ʹControler�������ͨ�ŵ�ʵ��</p>
 * @author X
 *
 */
public abstract class Networker {

	public static final int DEFAULT_INTERVAL = 3000;
	public static final int DEFAULT_RETRY_TIME = 3;
	public static final int DEFAULT_TIMEOUT = 10000;
	public static final int DEFAULT_PORT = 2009;
	public static final int DEFAULT_BUFFER_SIZE = 1024;

	/**
	 * �߳̽�����ʶ
	 */
	protected boolean finish;
	protected Controler controler;
	protected BlockingQueue<SendNode> sendQueue;

	protected Networker() {

	}

	protected Networker(Controler controler) {
		this.controler = controler;
		sendQueue = new LinkedBlockingQueue<SendNode>();
		finish = false;
		new NetworkSender(this).start();
		new NetworkListener(this).start();
	}

	/**
	 * <p>���ַ������뷢�Ͷ���</p>
	 * <p>�÷������ڲ�����send(sid, s.getBytes())</p>
	 * @param sid ����id
	 * @param s �ַ�����Ϣ
	 */
	public void send(long sid, String s) {
		send(sid, s.getBytes());
	}

	/**
	 * <p>����Ϣ���뷢�Ͷ���</p>
	 * @param sid ����id
	 * @param buf ��Ϣ
	 */
	public void send(long sid, byte[] buf) {
		try {
			sendQueue.put(new SendNode(sid, buf));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>�ر�Networker</p>
	 * <p>һ��������������ã���Networker���������ķ����̺߳ͼ����߳̽������ý�����</p>
	 */
	public void closeNetworker() {
		finish = true;
	}

	/**
	 * <p>�Ӷ�����ȡ��һ����Ϣ�����ͣ�û����Ϣ����ʱ��������߳�������ֱ������Ϣ����</p>
	 * <p>������controler.receipt(sid, ���ͽ��)���������ͽ��</p>
	 */
	protected abstract void sendData();

	/**
	 * <p>�������ϻ�ȡһ����Ϣ��û����Ϣʱ��������߳�������ֱ������Ϣ����</p>
	 * <p>����ȡ������Ϣ�ύ��controler������controler.processData(data)</p>
	 */
	protected abstract void receiveData();

	/**
	 * <p>�����̣߳�ÿ�ε���ָ��networker��sendData()����������ʱ����</p>
	 * @author X
	 *
	 */
	protected class NetworkSender extends Thread {
		private Networker networker;

		protected NetworkSender(Networker networker) {
			this.networker = networker;
		}

		@Override
		public void run() {
			while (!networker.finish)
				networker.sendData();
		}
	}

	/**
	 * <p>�����̣߳�ÿ�ε���ָ��networker��receiveData()����������ʱ����</p>
	 * @author X
	 *
	 */
	protected class NetworkListener extends Thread {
		private Networker networker;

		public NetworkListener(Networker networker) {
			this.networker = networker;
		}

		@Override
		public void run() {
			while (!networker.finish)
				networker.receiveData();
		}
	}
}
