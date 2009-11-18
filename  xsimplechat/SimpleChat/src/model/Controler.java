package model;

/**
 * <p>�������࣬�������ӵײ�Networker�ͱ��Performer</p>
 * <p>��Ҫ��������߼�����</p>
 * @author X
 *
 */
public abstract class Controler {

	protected Performer performer;
	protected Networker networker;
	protected boolean connecting;

	protected Controler() {
		connecting = true;
	}

	/**
	 * <p>Ԥ����Networker�Ľӿڣ����ڴ���������������</p>
	 * @param buf δ���ӹ�����������
	 */
	public abstract void processRawData(byte[] buf);

	/**
	 * <p>Ԥ����Performer�Ľӿڣ����ڴ���Performer���������¼�</p>
	 * @param type �¼�����
	 * @param params �¼���صĲ���
	 */
	public abstract void processUIAction(int type, Object[] params);

	/**
	 * <p>�����ͻ�ִ</p>
	 * @param sid ����id
	 * @param result ���ͽ��
	 */
	public abstract void receipt(long sid, boolean result);

	public abstract void downConnect();
}
