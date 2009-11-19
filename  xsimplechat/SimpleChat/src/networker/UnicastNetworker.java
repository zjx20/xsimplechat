package networker;

import model.*;

import java.io.*;
import java.net.*;

import controler.ChatingControler;

/**
 * <p>基于socket的单播networker</p>
 * @author X
 *
 */
public class UnicastNetworker extends Networker {

	static final Object lock=new Object();

	private Socket socket;

	public UnicastNetworker(Socket socket, Controler controler) {
		super(controler);
		this.socket = socket;
	}

	private boolean startSign = false, endSign = false;
	private ByteArrayOutputStream tempStream = new ByteArrayOutputStream();

	@Override
	protected void receiveData() {
		if (socket == null || socket.isClosed()) {
			controler.downConnect();
			closeNetworker();
			return;
		}
		byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
		int amount;
		try {
			amount = socket.getInputStream().read(buf);
			synchronized (lock) {
				System.out.print(Thread.currentThread() + " res ");
				ChatingControler.printByte(buf, amount);
			}
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
			System.out.println("Don't worry about this exception, it is not a serious problem.");
			e.printStackTrace();
			if (e.getMessage().indexOf("Connection reset") != -1) { //连接断开导致异常
				controler.downConnect();
				closeNetworker();
			}
		}
	}

	@Override
	protected void sendData() {
		SendNode s = null;
		try {
			s = sendQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return;
		}
		if (socket == null || socket.isClosed()) {
			controler.downConnect();
			closeNetworker();
			return;
		}

		int count = 0;
		while (count < DEFAULT_RETRY_TIME) {
			try {
				synchronized (lock) {
					System.out.print(Thread.currentThread() + " send ");
					ChatingControler.printByte(encodeForSend(s.getMsg()));
				}
				socket.getOutputStream().write(encodeForSend(s.getMsg()));
				socket.getOutputStream().flush();
			} catch (IOException e) {
				e.printStackTrace();
				count++;
				try {
					Thread.sleep(DEFAULT_INTERVAL); //等待一段时间重新发送
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				continue;
			}
			break;
		}
		controler.receipt(s.getSid(), count < DEFAULT_RETRY_TIME);
	}

	@Override
	public void closeNetworker() {
		super.closeNetworker();
		if (socket != null && socket.isConnected()) { //断开连接
			try {
				socket.getOutputStream().close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

