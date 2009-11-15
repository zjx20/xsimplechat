package networker;

import util.*;
import model.*;

import java.io.*;
import java.net.*;

public class UnicastNetworker extends Networker {

	private Socket socket;

	public UnicastNetworker(Socket socket, Controler controler) {
		super(controler);
		this.socket = socket;
	}

	@Override
	protected void receiveMessage() {
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
			buf = (byte[]) Toolkit.fixArraySize(buf, amount);
			controler.processMessage(buf);
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
	protected void sendMessage() {
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
				socket.getOutputStream().write(s.getMsg());
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
		finish = true;
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
