package controler;

import ui.*;
import model.*;
import networker.*;
import util.*;

import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.io.*;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * <p>私聊专用controler</p>
 * @author X
 *
 */
public class ChatingControler extends Controler {

	public static final Object lock=new Object();
	
	public static final int AC_SENDMESSAGE = 1;
	public static final int AC_CLOSEWINDOW = 2;
	public static final int AC_SENDFILE = 3;
	public static final int AC_SAVECHATLOG = 4;

	public static final int SIZE_HEADER = 16;

	public static final String HEADER_CLIENTINFO = Toolkit.padString("CLIENTINFO", SIZE_HEADER);
	public static final String HEADER_MESSAGE = Toolkit.padString("MESSAGE", SIZE_HEADER);
	public static final String HEADER_FILE = Toolkit.padString("FILE", SIZE_HEADER);

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
		//if (buf.length < SIZE_HEADER) //忽略短数据
		//	return;
		synchronized(lock) {
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
		}
		else if (type == AC_SENDFILE) {
			performer.updateUI(FrameChating.UPDATE_SENDFILE_MYSELF, params);
		}
		else if (type == AC_SAVECHATLOG) {
	        JFileChooser jfc = new JFileChooser( );  
            int r = jfc.showDialog(null, "保存");   
               if (r == JFileChooser.APPROVE_OPTION) {   
               selectfile = jfc.getSelectedFile();   
               try{
               	FileWriter output = new FileWriter(selectfile.getPath()+".txt");
               	output.write((String)params[0]);
               	output.close();
               	JOptionPane.showMessageDialog(null, "保存完毕","GUIDES",JOptionPane.INFORMATION_MESSAGE);
               }
               catch(IOException ex) {
               	JOptionPane.showMessageDialog(null, "The file does not exist.","GUIDES",JOptionPane.INFORMATION_MESSAGE);
               }
            }
		}
		else if (type == AC_CLOSEWINDOW) {
			performer.updateUI(FrameChating.UPDATE_CLOSE, params);
			performer.updateUI(FrameChating.UPDATE_CLOSE_MYSELF, params);
			networker.closeNetworker();
		}
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
