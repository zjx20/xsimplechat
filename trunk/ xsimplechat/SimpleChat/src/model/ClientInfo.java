package model;

public class ClientInfo {
	private String uuid;
	private String userName;
	private String ipAddress;
	private int port;

	public ClientInfo() {
		this("", "", "", 0);
	}

	public ClientInfo(String uuid, String userName, String ipAddress, int port) {
		this.uuid = uuid;
		this.userName = userName;
		this.ipAddress = ipAddress;
		this.port = port;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
