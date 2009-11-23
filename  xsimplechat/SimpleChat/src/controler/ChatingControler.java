package controler;

import ui.*;
import model.*;
import networker.*;
import util.*;

import java.net.*;
import java.util.Date;
import java.util.HashMap;
import java.io.*;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * <p>˽��ר��controler</p>
 * @author X
 *
 */
public class ChatingControler extends Controler {

	public static final Object lock = new Object();

	public static final int AC_SENDMESSAGE = 1;
	public static final int AC_CLOSEWINDOW = 2;
	public static final int AC_SENDFILE = 3;
	public static final int AC_SAVECHATLOG = 4;
	public static final int AC_ACCEPTFILE = 5;

	public static final int SIZE_HEADER = 16;
	public static final int SIZE_FILENAME = 255; //���͵��ļ���������255���ַ�

	public static final String HEADER_CLIENTINFO = Toolkit.padString("CLIENTINFO", SIZE_HEADER);
	public static final String HEADER_MESSAGE = Toolkit.padString("MESSAGE", SIZE_HEADER);
	public static final String HEADER_FILE = Toolkit.padString("FILE", SIZE_HEADER);
	public static final String HEADER_FILE_ACCEPT = Toolkit.padString("FILEACCEPT", SIZE_HEADER);

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
		// TODO Auto-generated method stub

	}

	@Override
	public void processRawData(byte[] buf) {
		synchronized (lock) {
			System.out.print("������Ϣ��");
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
			//��������ļ�
			String fileName = new String(buf, SIZE_HEADER, SIZE_FILENAME).trim();
			long fileSize = Toolkit.byteArrayToLong((byte[]) Toolkit.cutArray(buf, SIZE_HEADER
					+ SIZE_FILENAME, 8));
			Object[] params = { fileName, fileSize };
			performer.updateUI(FrameChating.UPDATE_FILE_REQUEST, params);

		} else if (header.compareTo(HEADER_FILE_ACCEPT) == 0) {
			//�Է������ļ�����
			String fileName = new String(buf, SIZE_HEADER, SIZE_FILENAME).trim();
			int port = Toolkit.byteArrayToInt((byte[]) Toolkit.cutArray(buf, SIZE_HEADER
					+ SIZE_FILENAME, 4));
			sendFile(peerClientInfo, port, fileMap.get(fileName));
			
		} else if (header.compareTo(HEADER_CLIENTINFO) == 0) {

			byte[] temp = new byte[ClientInfo.SIZE_IDENTIFYINFO];
			System.arraycopy(buf, SIZE_HEADER, temp, 0, ClientInfo.SIZE_IDENTIFYINFO);
			peerClientInfo = ClientInfo.parseClientInfo(temp);
			if (peerClientInfo == null) {
				networker.closeNetworker();
			} else {
				System.out.println(peerClientInfo);
				this.performer = new FrameChating(this);
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
					SIZE_FILENAME); //�ļ���
			os.write(Toolkit.longToByteArray(sendFile.length()), 0, 8);
			networker.send(sid++, os.toByteArray());
		} else if (type == AC_SAVECHATLOG) {
			JFileChooser jfc = new JFileChooser();
			int r = jfc.showDialog(null, "����");
			if (r == JFileChooser.APPROVE_OPTION) {
				selectfile = jfc.getSelectedFile();
				try {
					FileWriter output = new FileWriter(selectfile.getPath() + ".txt");
					output.write((String) params[0]);
					output.close();
					JOptionPane.showMessageDialog(null, "�������", "GUIDES",
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
		} else if (type == AC_ACCEPTFILE) {
			//�����ļ�������Ҫ������String sendFileName,File localFile
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket();
				serverSocket.bind(null);
			} catch (IOException e) {
				// TODO ���߶Է������쳣
				e.printStackTrace();
				return;
			}
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(HEADER_FILE_ACCEPT.getBytes(), 0, SIZE_HEADER);
			os.write(Toolkit.padString((String) params[0], SIZE_FILENAME).getBytes(), 0,
					SIZE_FILENAME); //�����ļ���
			os.write(Toolkit.intToByteArray(serverSocket.getLocalPort()), 0, 4); //���ؼ����˿�
			networker.send(sid++, os.toByteArray());
			recieveFile(serverSocket, (File) params[1]);
		}
	}

	private void recieveFile(final ServerSocket serverSocket, final File saveFile) {
		new Thread(new Runnable() {

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
		}).start();
	}

	private void sendFile(final ClientInfo peer, final int port, final File localFile) {
		new Thread(new Runnable() {

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
					// TODO ֪ͨ����
					e.printStackTrace();
				}
			}
		});
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
