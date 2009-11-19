package model;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;

/**
 * <p>基于Controler模型的TCP服务器类</p>
 * <p>主要用于监听网络端口，一旦有连接，则产生一个指定类型的Controler实例</p>
 * <p>调用指定参数的Controler方法：public Controler(Socket)</p>
 * <pre>
 * 调用方法：
 * 	Server server = new Server(Controler.class);	//指定Controler接受连接
 * 	server.start();	//开始服务
 * </pre>
 * @author X
 *
 */
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
