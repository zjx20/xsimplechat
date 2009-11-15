package model;

public abstract class Controler {

	protected Performer performer;
	protected Networker networker;
	protected boolean connecting;

	protected Controler() {
		connecting = true;
	}

	public abstract void processMessage(byte[] buf);

	public abstract void processUIAction(int type, String s);

	public abstract void receipt(int sid, boolean result);

	public abstract void downConnect();
}
