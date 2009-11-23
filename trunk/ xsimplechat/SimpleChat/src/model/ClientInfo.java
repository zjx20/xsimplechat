package model;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientInfo {

	public static final int SIZE_UUID = 36;
	public static final int SIZE_IPADDRESS = 4;
	public static final int SIZE_PORT = 2;
	public static final int SIZE_NICKNAME_WITH_LENGTH = 30;
	public static final int SIZE_IDENTIFYINFO = SIZE_UUID + SIZE_IPADDRESS + SIZE_PORT
			+ SIZE_NICKNAME_WITH_LENGTH;

	private static ClientInfo currentClientInfo;
	private static byte[] identifyInfo;

	/**
	 * <p>获取当前运行的ClientInfo</p>
	 * @return 当前运行的ClientInfo
	 */
	public static ClientInfo getCurrentClientInfo() {
		return currentClientInfo;
	}

	/**
	 * <p>设置当前运行的ClientInfo</p>
	 * @param currentClientInfo ClientInfo对象
	 */
	public static void setCurrentClientInfo(ClientInfo currentClientInfo) {
		ClientInfo.currentClientInfo = currentClientInfo;
	}

	public static byte[] getIdentifyInfo() {
		if (identifyInfo == null) {
			identifyInfo = generateIdentifyInfo(currentClientInfo);
		}
		return identifyInfo;
	}

	public static ClientInfo parseClientInfo(byte[] data) {
		if (data.length < SIZE_IDENTIFYINFO)
			return null;
		String uuid = new String(data, 0, SIZE_UUID);
		int pos = SIZE_UUID;
		byte[] temp = new byte[SIZE_IPADDRESS];
		for (int i = 0; i < SIZE_IPADDRESS; i++)
			temp[i] = data[pos++];
		InetAddress ipAddress = null;
		try {
			ipAddress = InetAddress.getByAddress(temp);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		int port = 0;
		port |= (data[pos++] & 0xff) << 8;
		port |= data[pos++] & 0xff;
		int nickNameLength = data[pos++] & 0xff;
		String nickName = new String(data, pos, nickNameLength);
		return new ClientInfo(uuid, nickName, ipAddress, port);
	}

	public static byte[] generateIdentifyInfo(ClientInfo clientInfo) {
		byte[] result = new byte[SIZE_IDENTIFYINFO];
		byte[] temp = clientInfo.getUuid().getBytes();
		int pos = 0;
		for (int i = 0; i < temp.length; i++)
			result[pos++] = temp[i];
		temp = clientInfo.getIpAddress().getAddress();
		for (int i = 0; i < temp.length; i++)
			result[pos++] = temp[i];
		int port = clientInfo.getPort();
		result[pos++] = (byte) ((port >>> 8) & 0xff);
		result[pos++] = (byte) (port & 0xff);
		temp = clientInfo.getNickName().getBytes();
		result[pos++] = (byte) temp.length;
		for (int i = 0; i < temp.length; i++)
			result[pos++] = temp[i];
		return result;
	}

	private String uuid;
	private String nickName;
	private InetAddress ipAddress;
	private int port;

	public ClientInfo(String uuid, String nickName, InetAddress ipAddress, int port) {
		this.uuid = uuid;
		this.nickName = nickName;
		this.ipAddress = ipAddress;
		this.port = port;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public InetAddress getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ClientInfo))
			return false;
		return this.uuid.compareTo(((ClientInfo) obj).getUuid()) == 0;
	}

	@Override
	public int hashCode() {
		return uuid.hashCode();
	}

	@Override
	public String toString() {
		String result = "";
		result += "NickName:" + nickName + "\n";
		result += "IPAddress:" + ipAddress.getHostAddress() + "\n";
		//result += "UUID:" + uuid + "\n";
		//result += "Port:" + port + "\n";
		return result;
	}
}
