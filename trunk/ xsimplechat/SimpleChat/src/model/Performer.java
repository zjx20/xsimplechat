package model;

/**
 * <p>表现类，用于人机交互</p>
 * @author X
 *
 */
public abstract class Performer {

	protected Controler controler;

	protected Performer(Controler controler) {
		this.controler = controler;
		generateUI();
	}

	/**
	 * <p>预留给Controler的接口，用于更新Performer</p>
	 * @param type 更新类型
	 * @param params 更新参数
	 */
	public abstract void updateUI(int type, Object[] params);

	/**
	 * <p>Performer用于生成界面的方法</p>
	 */
	protected abstract void generateUI();
}
