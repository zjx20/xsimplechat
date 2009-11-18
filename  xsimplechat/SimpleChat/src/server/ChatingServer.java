package server;

import controler.ChatingControler;
import model.Server;

/**
 * <p>˽�ķ���ָ��ChatingControler�ṩ����</p>
 * @see Server
 * @author X
 *
 */
public class ChatingServer extends Server {

	/**
	 * <p>JVM�����������˿�</p>
	 * @throws NoSuchMethodException �Ҳ������췽����ChatingServer(Socket)
	 */
	public ChatingServer() throws NoSuchMethodException {
		super(ChatingControler.class);
	}

	/**
	 * <p>ָ�������˿�</p>
	 * @param port ָ���Ķ˿ں�
	 * @throws NoSuchMethodException �Ҳ������췽����ChatingServer(Socket)
	 */
	public ChatingServer(int port) throws NoSuchMethodException {
		super(ChatingControler.class, port);
	}
}
