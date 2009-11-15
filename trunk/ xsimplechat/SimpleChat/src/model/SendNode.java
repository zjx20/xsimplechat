package model;

public class SendNode {
	private byte[] msg;
	private int sid;

	public SendNode() {
		this(0, new byte[0]);
	}

	public SendNode(int sid, byte[] msg) {
		this.sid = sid;
		this.msg = msg;
	}

	public int getSid() {
		return sid;
	}

	public byte[] getMsg() {
		return msg;
	}
}
