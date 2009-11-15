package model;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;

public abstract class Server extends Thread {
	private Class<?> classControler;
	private ServerSocket sSocket;
	private Constructor<?> constructor;

	protected Server(Class<?> classControler) throws NoSuchMethodException {
		try {
			sSocket = new ServerSocket();
			sSocket.bind(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.classControler = classControler;
		constructor = getConstructor();
	}

	protected Server(Class<?> classControler, int port) throws NoSuchMethodException {
		try {
			sSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.classControler = classControler;
		constructor = getConstructor();
	}

	private Constructor<?> getConstructor() throws NoSuchMethodException {
		int i;
		Constructor<?>[] c = classControler.getConstructors();
		for (i = 0; i < c.length; i++) {
			if ((c[i].getParameterAnnotations().length == 1)
					&& (c[i].getParameterTypes()[0].getName().compareTo("java.net.Socket") == 0)) {
				return c[i];
			}
		}
		throw new NoSuchMethodException("找不到指定构造方法：public Controler(Socket)");
	}

	public int getLocalPort() {
		return sSocket.getLocalPort();
	}

	@Override
	public void run() {
		while (true) {
			try {
				Socket socket = sSocket.accept();
				constructor.newInstance(socket);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
