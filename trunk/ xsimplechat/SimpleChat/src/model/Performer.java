package model;

/**
 * <p>�����࣬�����˻�����</p>
 * @author X
 *
 */
public abstract class Performer {

	protected Controler controler;

	public Performer(Controler controler) {
		this.controler = controler;
		generateUI();
	}

	/**
	 * <p>Ԥ����Controler�Ľӿڣ����ڸ���Performer</p>
	 * @param type ��������
	 * @param params ���²���
	 */
	public abstract void updateUI(int type, Object[] params);
	/**
	 * <p>Performer�������ɽ���ķ���</p>
	 */
	protected abstract void generateUI();
}
