package model;

import java.util.concurrent.*;

/**
 * <p>网络通信抽象类，可使Controler无需关心通信的实现</p>
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
	 * 线程结束标识
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
	 * <p>将字符串放入发送队列</p>
	 * <p>该方法在内部调用send(sid, s.getBytes())</p>
	 * @param sid 发送id
	 * @param s 字符串消息
	 */
	public void send(long sid, String s) {
		send(sid, s.getBytes());
	}

	/**
	 * <p>将消息放入发送队列</p>
	 * @param sid 发送id
	 * @param buf 消息
	 */
	public void send(long sid, byte[] buf) {
		try {
			sendQueue.put(new SendNode(sid, buf));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>关闭Networker</p>
	 * <p>一旦这个函数被调用，该Networker派生出来的发送线程和监听线程将被永久结束。</p>
	 */
	public void closeNetworker() {
		finish = true;
	}

	/**
	 * <p>从队列中取出一条消息并发送，没有消息发送时，令调用线程阻塞，直到有消息发送</p>
	 * <p>最后调用controler.receipt(sid, 发送结果)，反馈发送结果</p>
	 */
	protected abstract void sendData();

	/**
	 * <p>从网络上获取一条消息，没有消息时，令调用线程阻塞，直到有消息到达</p>
	 * <p>将获取到的消息提交给controler，调用controler.processData(data)</p>
	 */
	protected abstract void receiveData();

	public static final byte START_OF_HEADER = 0x01;
	public static final byte END_OF_TRANSMISSION = 0x04;
	public static final byte ESCAPE = 0x1B;

	/**
	 * <p>帧界定编码</p>
	 * @param a 源byte数组
	 * @return 编码后的byte数组
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
	 * <p>帧界定解码</p>
	 * @param a 源byte数组
	 * @return 解码后的byte数组
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
	 * <p>发送线程，每次调用指定networker的sendData()方法，空闲时阻塞</p>
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
	 * <p>监听线程，每次调用指定networker的receiveData()方法，空闲时阻塞</p>
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
