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
	public static final int DEFAULT_BUFFER_SIZE = 10240;

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

	public static final byte START_OF_HEADER = 0x01;
	public static final byte END_OF_TRANSMISSION = 0x04;
	public static final byte ESCAPE = 0x1B;

	/**
	 * <p>֡�綨����</p>
	 * @param a Դbyte����
	 * @return ������byte����
	 */
	public static byte[] encodeForSend(byte[] a) {
		int count = 0;
		for (int i = 0; i < a.length; i++) {
			if (a[i] == START_OF_HEADER || a[i] == END_OF_TRANSMISSION || a[i] == ESCAPE)
				count++;
		}
		byte[] result = new byte[a.length + count + 2];
		int pos = 0;
		result[pos++] = START_OF_HEADER;
		for (int i = 0; i < a.length; i++) {
			if (a[i] == START_OF_HEADER) {
				result[pos++] = ESCAPE;
				result[pos++] = 'x';
			} else if (a[i] == END_OF_TRANSMISSION) {
				result[pos++] = ESCAPE;
				result[pos++] = 'y';
			} else if (a[i] == ESCAPE) {
				result[pos++] = ESCAPE;
				result[pos++] = 'z';
			} else
				result[pos++] = a[i];
		}
		result[pos++] = END_OF_TRANSMISSION;
		return result;
	}

	/**
	 * <p>֡�綨����</p>
	 * @param a Դbyte����
	 * @return ������byte����
	 */
	public static byte[] decodeForReceive(byte[] a) {
		int count = 0;
		for (int i = 0; i < a.length; i++) {
			if (a[i] == ESCAPE)
				count++;
		}
		byte[] result = new byte[a.length - count];
		int pos = 0;
		for (int i = 0; i < a.length; i++) {
			if (a[i] == ESCAPE) {
				i++;
				if (a[i] == 'x')
					result[pos++] = START_OF_HEADER;
				else if (a[i] == 'y')
					result[pos++] = END_OF_TRANSMISSION;
				else if (a[i] == 'z')
					result[pos++] = ESCAPE;
			} else
				result[pos++] = a[i];
		}
		return result;
	}

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
