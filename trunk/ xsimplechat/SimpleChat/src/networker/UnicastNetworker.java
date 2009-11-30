package networker;

import model.*;

import java.io.*;
import java.net.*;

/**
 * <p>基于socket的单播networker</p>
 * @author X
 *
 */
public class UnicastNetworker extends Networker {

	protected Socket socket;

	public UnicastNetworker(Socket socket, Controler controler) {
		super(controler);
		this.socket = socket;
	}

	private boolean startSign = false, endSign = false;
	private ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
	private byte[] buf = new byte[DEFAULT_BUFFER_SIZE];

	@Override
	protected void receiveData() {
		if (socket == null || socket.isClosed()) {
			if(!isClosed())
				controler.downConnect();
			return;
		}

		int amount;
		try {
			amount = socket.getInputStream().read(buf);
			if (amount < 0 && !isClosed())
				controler.downConnect();
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
			//System.out.println("Don't worry about this exception, it is not a serious problem.");
			if(!isClosed())
				controler.downConnect();
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
			if(!isClosed())
				controler.downConnect();
			return;
		}

		boolean result;
		try {
			socket.getOutputStream().write(encodeForSend(s.getMsg()));
			socket.getOutputStream().flush();
			result = true;
		} catch (IOException e) {
			e.printStackTrace();
			if (e.getMessage().indexOf("closed") != -1 && !isClosed())
				controler.downConnect();
			result = false;
		}
		controler.receipt(s.getSid(), result);
	}

	@Override
	public void closeNetworker() {
		super.closeNetworker();
		if (socket != null && !socket.isClosed()) { //断开连接
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
