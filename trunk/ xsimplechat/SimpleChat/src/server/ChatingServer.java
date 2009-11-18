package server;

import controler.ChatingControler;
import model.Server;

/**
 * <p>私聊服务，指定ChatingControler提供服务</p>
 * @see Server
 * @author X
 *
 */
public class ChatingServer extends Server {

	/**
	 * <p>JVM随机分配监听端口</p>
	 * @throws NoSuchMethodException 找不到构造方法：ChatingServer(Socket)
	 */
	public ChatingServer() throws NoSuchMethodException {
		super(ChatingControler.class);
	}

	/**
	 * <p>指定监听端口</p>
	 * @param port 指定的端口号
	 * @throws NoSuchMethodException 找不到构造方法：ChatingServer(Socket)
	 */
	public ChatingServer(int port) throws NoSuchMethodException {
		super(ChatingControler.class, port);
	}
}
