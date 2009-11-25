package controler;

import ui.*;
import model.*;
import networker.*;
import util.*;

import java.net.*;
import java.util.*;
import java.io.*;

import javax.swing.*;

/**
 * <p>私聊专用controler</p>
 * @author X
 *
 */
public class ChatingControler extends Controler {

	public static final Object lock = new Object();

	public static final int AC_SENDMESSAGE = 1;
	public static final int AC_CLOSEWINDOW = 2;
	public static final int AC_SENDFILE = 3;
	public static final int AC_SAVECHATLOG = 4;
	public static final int AC_ACCEPT_FILEREQUEST = 5;
	public static final int AC_REFUSE_FILEREQUEST = 6;

	public static final int SIZE_HEADER = 16;
	public static final int SIZE_FILENAME = 255; //传送的文件名不超过255个字符

	public static final String HEADER_CLIENTINFO = Toolkit.padString("CLIENTINFO", SIZE_HEADER);
	public static final String HEADER_MESSAGE = Toolkit.padString("MESSAGE", SIZE_HEADER);
	public static final String HEADER_FILE = Toolkit.padString("FILE", SIZE_HEADER);
	public static final String HEADER_FILE_ACCEPT = Toolkit.padString("FILEACCEPT", SIZE_HEADER);
	public static final String HEADER_FILE_REFUSE = Toolkit.padString("FILEREFUSE", SIZE_HEADER);

	private HashMap<String, File> fileMap = new HashMap<String, File>();
	private long sid = 0;
	private ClientInfo peerClientInfo;
	private File selectfile;

	public ChatingControler(Socket socket) {
		this.networker = new UnicastNetworker(socket, this);
		networker.send(sid++, Toolkit.generateSendData(HEADER_CLIENTINFO, ClientInfo
				.getIdentifyInfo()));
	}

	public void downConnect() {
		networker.closeNetworker();
		MainControler.removeFromSingleTalking(peerClientInfo);
	}

	@Override
	public void processRawData(byte[] buf) {
		synchronized (lock) {
			System.out.print("接收信息：");
			printByte(buf);
		}
		String header = new String(buf, 0, SIZE_HEADER);
		//System.out.println("receive header:" + header);
		if (header.compareTo(HEADER_MESSAGE) == 0) {

			if (peerClientInfo == null) {
				networker.closeNetworker();
			}
			Object[] params = new Object[1];
			params[0] = new String(buf, SIZE_HEADER, buf.length - SIZE_HEADER);
			performer.updateUI(FrameChating.UPDATE_NEWMESSAGE, params);

		} else if (header.compareTo(HEADER_FILE) == 0) {
			//请求接收文件
			String fileName = new String(buf, SIZE_HEADER, SIZE_FILENAME).trim();
			long fileSize = Toolkit.byteArrayToLong((byte[]) Toolkit.cutArray(buf, SIZE_HEADER
					+ SIZE_FILENAME, 8));
			Object[] params = { fileName, fileSize };
			performer.updateUI(FrameChating.UPDATE_FILE_REQUEST, params);

		} else if (header.compareTo(HEADER_FILE_ACCEPT) == 0) {
			//对方接受文件请求
			performer.updateUI(FrameChating.UPDATE_ACCEPT_FILE, null);
			String fileName = new String(buf, SIZE_HEADER, SIZE_FILENAME).trim();
			int port = Toolkit.byteArrayToInt((byte[]) Toolkit.cutArray(buf, SIZE_HEADER
					+ SIZE_FILENAME, 4));
			sendFile(peerClientInfo, port, fileMap.get(fileName));
			fileMap.remove(fileName);

		} else if (header.compareTo(HEADER_FILE_REFUSE) == 0) {
			//对方拒接文件请求
			performer.updateUI(FrameChating.UPDATE_REFUSE_FILE, null);
			String fileName = new String(buf, SIZE_HEADER, SIZE_FILENAME).trim();
			fileMap.remove(fileName);

		} else if (header.compareTo(HEADER_CLIENTINFO) == 0) {

			byte[] temp = new byte[ClientInfo.SIZE_IDENTIFYINFO];
			System.arraycopy(buf, SIZE_HEADER, temp, 0, ClientInfo.SIZE_IDENTIFYINFO);
			peerClientInfo = ClientInfo.parseClientInfo(temp);
			if (peerClientInfo == null) {
				networker.closeNetworker();
			} else {
				System.out.println(peerClientInfo);
				this.performer = new FrameChating(this);
				MainControler.addToSingleTalking(peerClientInfo);
			}

		}
	}

	@Override
	public void processUIAction(int type, Object[] params) {
		if (type == AC_SENDMESSAGE) {

			networker.send(sid++, Toolkit.generateSendData(HEADER_MESSAGE, (new Date().getTime()
					+ " " + params[0]).getBytes()));
			performer.updateUI(FrameChating.UPDATE_NEWMESSAGE_MYSELF, params);

		} else if (type == AC_SENDFILE) {

			performer.updateUI(FrameChating.UPDATE_SENDFILE_MYSELF, params);
			File sendFile = (File) params[0];
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(HEADER_FILE.getBytes(), 0, SIZE_HEADER);
			os.write(Toolkit.padString(sendFile.getName(), SIZE_FILENAME).getBytes(), 0,
					SIZE_FILENAME); //文件名
			os.write(Toolkit.longToByteArray(sendFile.length()), 0, 8);
			networker.send(sid++, os.toByteArray());
			fileMap.put(sendFile.getName(), sendFile);

		} else if (type == AC_SAVECHATLOG) {

			JFileChooser jfc = new JFileChooser();
			int r = jfc.showDialog(null, "保存");
			if (r == JFileChooser.APPROVE_OPTION) {
				selectfile = jfc.getSelectedFile();
				try {
					FileWriter output = new FileWriter(selectfile.getPath() + ".txt");
					output.write((String) params[0]);
					output.close();
					JOptionPane.showMessageDialog(null, "保存完毕", "GUIDES",
							JOptionPane.INFORMATION_MESSAGE);
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(null, "The file does not exist.", "GUIDES",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}

		} else if (type == AC_CLOSEWINDOW) {

			performer.updateUI(FrameChating.UPDATE_CLOSE, params);
			performer.updateUI(FrameChating.UPDATE_CLOSE_MYSELF, params);
			networker.closeNetworker();

		} else if (type == AC_ACCEPT_FILEREQUEST) {
			//接受文件请求，需要参数：String sendFileName,File localFile
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket();
				serverSocket.bind(null);
			} catch (IOException e) {
				// TODO 告诉对方发生异常
				e.printStackTrace();
				return;
			}
			recieveFile(serverSocket, new File((String) params[1]));
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(HEADER_FILE_ACCEPT.getBytes(), 0, SIZE_HEADER);
			os.write(Toolkit.padString((String) params[0], SIZE_FILENAME).getBytes(), 0,
					SIZE_FILENAME); //接收文件名
			os.write(Toolkit.intToByteArray(serverSocket.getLocalPort()), 0, 4); //本地监听端口
			networker.send(sid++, os.toByteArray());

		}

		else if (type == AC_REFUSE_FILEREQUEST) {
			//拒绝接收文件
			String fileName = (String) params[0];
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(HEADER_FILE_REFUSE.getBytes(), 0, SIZE_HEADER);
			os.write(Toolkit.padString(fileName, SIZE_FILENAME).getBytes(), 0, SIZE_FILENAME);
			networker.send(sid++, os.toByteArray());

		}
	}

	private void recieveFile(final ServerSocket serverSocket1, final File saveFile1) {
		new Thread() {
			private ServerSocket serverSocket = serverSocket1;
			private File saveFile = saveFile1;

			@Override
			public void run() {
				try {
					Socket socket = serverSocket.accept();
					FileOutputStream fos = new FileOutputStream(saveFile);
					BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
					byte[] buf = new byte[1024 * 128];
					int amount;
					while ((amount = bis.read(buf)) != -1) {
						fos.write(buf, 0, amount);
					}
					bis.close();
					fos.close();
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private void sendFile(final ClientInfo peer1, final int port1, final File localFile1) {
		new Thread() {
			private ClientInfo peer = peer1;
			private int port = port1;
			private File localFile = localFile1;

			@Override
			public void run() {
				try {
					Socket socket = new Socket(peer.getIpAddress(), port);
					FileInputStream fis = new FileInputStream(localFile);
					OutputStream os = socket.getOutputStream();
					byte[] buf = new byte[1024 * 128];
					int amount;
					while ((amount = fis.read(buf)) != -1) {
						os.write(buf, 0, amount);
					}
					fis.close();
					os.close();
					socket.close();
				} catch (IOException e) {
					// TODO 通知出错
					e.printStackTrace();
				}
			}
		}.start();
	}

	public ClientInfo getPeerClientInfo() {
		return peerClientInfo;
	}

	@Override
	public void receipt(long sid, boolean result) {
		// TODO Auto-generated method stub

	}

	static public synchronized void printByte(byte[] a) {
		printByte(a, a.length);
	}

	static public synchronized void printByte(byte[] a, int len) {
		int i;
		for (i = 0; i < len; i++) {
			System.out.print((a[i] & 0xff) + " ");
		}
		System.out.println();
	}
}
