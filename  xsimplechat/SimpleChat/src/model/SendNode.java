package model;

public class SendNode {
	private byte[] msg;
	private long sid;

	public SendNode() {
		this(0, new byte[0]);
	}

	public SendNode(long sid, byte[] msg) {
		this.sid = sid;
		this.msg = msg;
	}

	public long getSid() {
		return sid;
	}

	public byte[] getMsg() {
		return msg;
	}
}
