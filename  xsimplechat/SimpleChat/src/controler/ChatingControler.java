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
	public static final int AC_SENDDIRECTORY = 7;
	public static final int AC_ACCEPT_DIRECTORYREQUEST = 8;
	public static final int AC_REFUSE_DIRECTORYREQUEST = 9;

	public static final int SIZE_HEADER = 16;
	public static final int SIZE_FILENAME = 512; //传送的文件名不超过的字符数

	public static final String HEADER_CLIENTINFO = Toolkit.padString("CLIENTINFO", SIZE_HEADER);
	public static final String HEADER_MESSAGE = Toolkit.padString("MESSAGE", SIZE_HEADER);
	public static final String HEADER_FILE = Toolkit.padString("FILE", SIZE_HEADER);
	public static final String HEADER_FILE_ACCEPT = Toolkit.padString("FILEACCEPT", SIZE_HEADER);
	public static final String HEADER_FILE_REFUSE = Toolkit.padString("FILEREFUSE", SIZE_HEADER);
	public static final String HEADER_DIRECTORY = Toolkit.padString("DIRECTORY", SIZE_HEADER);
	public static final String HEADER_DIRECTORY_ACCEPT = Toolkit.padString("DIRECTORYACCEPT",
			SIZE_HEADER);
	public static final String HEADER_DIRECTORY_REFUSE = Toolkit.padString("DIRECTORYREFUSE",
			SIZE_HEADER);

	private HashMap<String, File> fileMap = new HashMap<String, File>();
	private HashMap<String, File> dirMap = new HashMap<String, File>();
	private long sid = 0;
	private ClientInfo peerClientInfo;
	private File selectfile;

	public ChatingControler(Socket socket) {
		this.networker = new UnicastNetworker(socket, this);
		networker.send(sid++, Toolkit.generateSendData(HEADER_CLIENTINFO, ClientInfo
				.getIdentifyInfo()));
	}

	public void downConnect() {
		performer.updateUI(FrameChating.UPDATE_CONNECT_DOWN, null);
		networker.closeNetworker();
		MainControler.removeFromSingleTalking(peerClientInfo);
	}

	@Override
	public void processRawData(byte[] buf) {

		String header = new String(buf, 0, SIZE_HEADER);
		System.out.println(header);
		if (header.compareTo(HEADER_MESSAGE) == 0) {

			if (peerClientInfo == null) {
				networker.closeNetworker();
			}
			Object[] params = new Object[1];
			params[0] = new String(buf, SIZE_HEADER, buf.length - SIZE_HEADER);
			performer.updateUI(FrameChating.UPDATE_MESSAGE_FORRECIEVER, params);

		} else if (header.compareTo(HEADER_FILE) == 0) {
			//请求接收文件
			String fileName = new String(buf, SIZE_HEADER, SIZE_FILENAME).trim();
			long fileSize = Toolkit.byteArrayToLong((byte[]) Toolkit.cutArray(buf, SIZE_HEADER
					+ SIZE_FILENAME, 8));
			Object[] params = { fileName, fileSize };
			performer.updateUI(FrameChating.UPDATE_FILE_REQUEST_FORRECIEVER, params);

		} else if (header.compareTo(HEADER_FILE_ACCEPT) == 0) {
			//对方接受文件请求
			String fileName = new String(buf, SIZE_HEADER, SIZE_FILENAME).trim();
			Object[] params = { fileName };
			performer.updateUI(FrameChating.UPDATE_ACCEPT_FILE_FORSENDER, params);
			int port = Toolkit.byteArrayToInt((byte[]) Toolkit.cutArray(buf, SIZE_HEADER
					+ SIZE_FILENAME, 4));
			if (fileName.startsWith("File:"))
				sendFile(peerClientInfo, port, fileMap.get(fileName),
						FrameChating.UPDATE_DIRECTORY_PROGRESS_FORSENDER);
			else
				sendFile(peerClientInfo, port, fileMap.get(fileName),
						FrameChating.UPDATE_FILE_TRANSFER_COMPLETE);
			fileMap.remove(fileName);

		} else if (header.compareTo(HEADER_FILE_REFUSE) == 0) {
			//对方拒接文件请求
			String fileName = new String(buf, SIZE_HEADER, SIZE_FILENAME).trim();
			Object[] params = { fileName };
			performer.updateUI(FrameChating.UPDATE_REFUSE_FILE_FORSENDER, params);
			fileMap.remove(fileName);

		} else if (header.compareTo(HEADER_CLIENTINFO) == 0) {

			byte[] temp = new byte[ClientInfo.SIZE_IDENTIFYINFO];
			System.arraycopy(buf, SIZE_HEADER, temp, 0, ClientInfo.SIZE_IDENTIFYINFO);
			peerClientInfo = ClientInfo.parseClientInfo(temp);
			if (peerClientInfo == null) {
				networker.closeNetworker();
			} else {
				this.performer = new FrameChating(this);
				MainControler.addToSingleTalking(peerClientInfo);
			}

		} else if (header.compareTo(HEADER_DIRECTORY) == 0) {

			Scanner in = new Scanner(new ByteArrayInputStream(buf, SIZE_HEADER, buf.length
					- SIZE_HEADER));
			LinkedList<String> files = new LinkedList<String>();
			while (in.hasNextLine()) {
				String temp = in.nextLine();
				files.add(temp);
			}
			Object[] params = { files };
			performer.updateUI(FrameChating.UPDATE_DIRECTORY_REQUEST_FORRECIEVER, params);

		} else if (header.compareTo(HEADER_DIRECTORY_ACCEPT) == 0) {

			String dir = new String(buf, SIZE_HEADER, buf.length - SIZE_HEADER);
			Object[] params = { dirMap.get(dir).getAbsolutePath() };
			dirMap.remove(dir);
			performer.updateUI(FrameChating.UPDATE_ACCEPT_DIRECTORY_FORSENDER, params);

		} else if (header.compareTo(HEADER_DIRECTORY_REFUSE) == 0) {

			String dir = new String(buf, SIZE_HEADER, buf.length - SIZE_HEADER);
			Object[] params = { dirMap.get(dir).getAbsolutePath() };
			dirMap.remove(dir);
			performer.updateUI(FrameChating.UPDATE_REFUSE_DIRECTORY_FORSENDER, params);

		}
	}

	@Override
	public void processUIAction(int type, Object[] params) {
		if (type == AC_SENDMESSAGE) {

			networker.send(sid++, Toolkit.generateSendData(HEADER_MESSAGE, ((String) params[0])
					.getBytes()));
			performer.updateUI(FrameChating.UPDATE_MESSAGE_FORSENDER, params);

		} else if (type == AC_SENDFILE) {

			File sendFile = (File) params[0];
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(HEADER_FILE.getBytes(), 0, SIZE_HEADER);
			os.write(Toolkit.padString(sendFile.getName(), SIZE_FILENAME).getBytes(), 0,
					SIZE_FILENAME); //文件名
			os.write(Toolkit.longToByteArray(sendFile.length()), 0, 8);
			networker.send(sid++, os.toByteArray());
			fileMap.put(sendFile.getName(), sendFile);
			performer.updateUI(FrameChating.UPDATE_FILE_REQUEST_FORSENDER, null);

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
			networker.closeNetworker();

		} else if (type == AC_ACCEPT_FILEREQUEST) {
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket();
				serverSocket.bind(null);
			} catch (IOException e) {
				// TODO 告诉对方发生异常
				e.printStackTrace();
				return;
			}
			performer.updateUI(FrameChating.UPDATE_ACCEPT_FILE_FORRECIEVER, null);
			recieveFile(serverSocket, (File) params[1], (Long) params[2], (String) params[0], null);

		}

		else if (type == AC_REFUSE_FILEREQUEST) {
			//拒绝接收文件
			String fileName = (String) params[0];
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(HEADER_FILE_REFUSE.getBytes(), 0, SIZE_HEADER);
			os.write(Toolkit.padString(fileName, SIZE_FILENAME).getBytes(), 0, SIZE_FILENAME);
			networker.send(sid++, os.toByteArray());

		} else if (type == AC_SENDDIRECTORY) {
			//发送文件夹
			sendDir((File) params[0]);

		} else if (type == AC_ACCEPT_DIRECTORYREQUEST) {
			//接收文件夹
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket();
				serverSocket.bind(null);
			} catch (IOException e) {
				// TODO 告诉对方发生异常
				e.printStackTrace();
				return;
			}
			recieveDir(serverSocket, (File) params[0], (LinkedList<?>) params[1]);
			networker.send(sid++, Toolkit.generateSendData(HEADER_DIRECTORY_ACCEPT,
					((String) ((LinkedList<?>) params[1]).get(0)).getBytes()));
			String temp = (String) ((LinkedList<?>) params[1]).get(0);
			Object[] arg = { temp.substring(temp.indexOf(':') + 1, temp.lastIndexOf('|') - 1) };
			performer.updateUI(FrameChating.UPDATE_ACCEPT_DIRECTORY_FORRECIEVER, arg);

		} else if (type == AC_REFUSE_DIRECTORYREQUEST) {
			//拒绝接收文件夹
			networker.send(sid++, Toolkit.generateSendData(HEADER_DIRECTORY_REFUSE,
					((String) ((LinkedList<?>) params[0]).get(0)).getBytes()));
			performer.updateUI(FrameChating.UPDATE_REFUSE_DIRECTORY_FORRECIEVER, params);

		}
	}

	private void recieveFile(final ServerSocket serverSocket1, final File saveFile1,
			final long fileSize1, final String remoteFileName1, final Object lock1) {
		new Thread() {
			private ServerSocket serverSocket = serverSocket1;
			private File saveFile = saveFile1;
			private long fileSize = fileSize1;
			private String remoteFileName = remoteFileName1;
			private final Object lock = lock1;

			@Override
			public void run() {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				os.write(HEADER_FILE_ACCEPT.getBytes(), 0, SIZE_HEADER);
				os.write(Toolkit.padString(remoteFileName, SIZE_FILENAME).getBytes(), 0,
						SIZE_FILENAME); //接收文件名
				os.write(Toolkit.intToByteArray(serverSocket.getLocalPort()), 0, 4); //本地监听端口
				networker.send(sid++, os.toByteArray());
				try {
					Socket socket = serverSocket.accept();
					FileOutputStream fos = new FileOutputStream(saveFile);
					BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
					byte[] buf = new byte[1024 * 128];
					int amount;
					long sum = 0;
					Object[] params = new Object[1];
					while ((amount = bis.read(buf)) != -1) {
						fos.write(buf, 0, amount);
						sum += amount;
						params[0] = (int) ((double) sum / fileSize * 100.0);
						performer.updateUI(FrameChating.UPDATE_PROGRESS_FORRECIEVER, params);
					}
					bis.close();
					fos.close();
					socket.close();
					params[0] = saveFile.getName();
					performer.updateUI(FrameChating.UPDATE_FILE_TRANSFER_COMPLETE, params);
					if (lock != null) {
						synchronized (lock) {
							lock.notify();
						}
					}
				} catch (IOException e) {
					// TODO 传输中断
					e.printStackTrace();
				}
			}
		}.start();
	}

	private void recieveDir(final ServerSocket serverSocket1, final File saveDir1,
			final LinkedList<?> files1) {
		new Thread() {
			private ServerSocket serverSocket = serverSocket1;
			private File saveDir = saveDir1;
			private LinkedList<?> files = files1;
			private final Object lock = new Object();

			@Override
			public void run() {
				int i = 0, len = files.size();
				Iterator<?> it = files.iterator();
				while (it.hasNext()) {
					String temp = (String) it.next();
					String name = temp.substring(temp.indexOf(':') + 1, temp.lastIndexOf('|') - 1)
							.trim();
					if (File.separatorChar == '\\')
						name = name.replaceAll("/", "\\" + File.separator); //平台有关的路径分隔符
					System.out.println(name);
					File tempfile = new File(saveDir.getAbsolutePath() + File.separator + name);
					if (temp.startsWith("Dir:")) {
						tempfile.mkdirs();
					} else {
						long size = Long.parseLong(temp.substring(temp.indexOf('|') + 1,
								temp.lastIndexOf(' ') - 1).trim());
						recieveFile(serverSocket, tempfile, size, temp, lock);
						synchronized (lock) {
							try {
								lock.wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					Object[] params = { ++i, len };
					performer.updateUI(FrameChating.UPDATE_DIRECTORY_PROGRESS_FORRECIEVER, params);
				}
				performer.updateUI(FrameChating.UPDATE_DIRECTORY_TRANSFER_COMPLETE, null);
				try {
					serverSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}.start();
	}

	private void sendFile(final ClientInfo peer1, final int port1, final File localFile1,
			final int type1) {
		new Thread() {
			private ClientInfo peer = peer1;
			private int port = port1;
			private File localFile = localFile1;
			private int type = type1;

			@Override
			public void run() {
				long fileSize = localFile.length();
				try {
					Socket socket = new Socket(peer.getIpAddress(), port);
					FileInputStream fis = new FileInputStream(localFile);
					OutputStream os = socket.getOutputStream();
					byte[] buf = new byte[1024 * 128];
					Object[] params = new Object[1];
					int amount;
					long sum = 0;
					while ((amount = fis.read(buf)) != -1) {
						os.write(buf, 0, amount);
						sum += amount;
						params[0] = (int) ((double) sum / fileSize * 100.0);
						performer.updateUI(FrameChating.UPDATE_PROGRESS_FORSENDER, params);
					}
					fis.close();
					os.close();
					socket.close();
					params[0] = localFile.getName();
					performer.updateUI(type, params);
					performer.updateUI(FrameChating.UPDATE_FILE_TRANSFER_COMPLETE, params);
				} catch (IOException e) {
					// TODO 传输中断
					e.printStackTrace();
				}
			}
		}.start();
	}

	private void sendDir(File localDir) {
		LinkedList<FileBean> fileList = new LinkedList<FileBean>();
		int total = DFSDir(fileList, localDir, "");
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(Toolkit.padString(HEADER_DIRECTORY, SIZE_HEADER).getBytes(), 0, SIZE_HEADER);
		Iterator<FileBean> it = fileList.descendingIterator();
		while (it.hasNext()) {
			FileBean fileBean = it.next();
			String temp = fileBean.getIdString();
			if (temp.startsWith("File:"))
				fileMap.put(temp, fileBean.getFile());
			os.write((temp + "\r\n").getBytes(), 0, temp.length() + 1);
		}
		networker.send(sid++, os.toByteArray());
		Object[] params = { total };
		dirMap.put(fileList.getLast().getIdString(), localDir);
		performer.updateUI(FrameChating.UPDATE_DIRECTORY_REQUEST_FORSENDER, params);
	}

	private int DFSDir(List<FileBean> fileList, File cur, String prefix) {
		if (cur == null)
			return 0;
		if (cur.isFile()) {
			String idString = "File: " + prefix + cur.getName() + " | " + cur.length() + " Bytes";
			fileList.add(new FileBean(cur, idString));
			return 1;
		}
		if (cur.isDirectory()) {
			File[] child = cur.listFiles();
			int i, len = child.length;
			int sum = 0;
			for (i = 0; i < len; i++) {
				sum += DFSDir(fileList, child[i], prefix + cur.getName() + "/");
			}
			String idString = "Dir: " + prefix + cur.getName() + " | " + sum + " Files";
			fileList.add(new FileBean(cur, idString));
			return sum;
		}
		return 0;
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

class FileBean {
	private File file;
	private String idString;

	public FileBean(File file, String idString) {
		super();
		this.file = file;
		this.idString = idString;
	}

	public File getFile() {
		return file;
	}

	public String getIdString() {
		return idString;
	}
}
