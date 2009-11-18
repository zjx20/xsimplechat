package model;

/**
 * <p>控制者类，用于连接底层Networker和表层Performer</p>
 * <p>主要负责完成逻辑功能</p>
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
	 * <p>预留给Networker的接口，用于处理生的网络数据</p>
	 * @param buf 未经加工的网络数据
	 */
	public abstract void processRawData(byte[] buf);

	/**
	 * <p>预留给Performer的接口，用于处理Performer所产生的事件</p>
	 * @param type 事件类型
	 * @param params 事件相关的参数
	 */
	public abstract void processUIAction(int type, Object[] params);

	/**
	 * <p>处理发送回执</p>
	 * @param sid 发送id
	 * @param result 发送结果
	 */
	public abstract void receipt(long sid, boolean result);

	public abstract void downConnect();
}
