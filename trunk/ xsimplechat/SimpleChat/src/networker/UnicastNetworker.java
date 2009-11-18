package networker;

import util.*;
import model.*;

import java.io.*;
import java.net.*;

/**
 * <p>基于socket的单播networker</p>
 * @author X
 *
 */
public class UnicastNetworker extends Networker {

	public static final byte START_OF_HEADER = 0x01;
	public static final byte END_OF_TRANSMISSION = 0x04;
	public static final byte ESCAPE = 0x1B;

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

	private byte[] encodeForSend(byte[] a) {
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

	private byte[] decodeForReceive(byte[] a) {
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
}

