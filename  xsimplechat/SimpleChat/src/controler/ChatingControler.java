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
 * <p>˽��ר��controler</p>
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
	public static final int SIZE_VALIDATION_CODE = 12;

	public static final String HEADER_VALIDATE = Toolkit.padString("VALIDATE", SIZE_HEADER);
	public static final String HEADER_REPLY_VALIDATE = Toolkit.padString("REVALIDATE", SIZE_HEADER);
	public static final String HEADER_MESSAGE = Toolkit.padString("MESSAGE", SIZE_HEADER);
	public static final String HEADER_FILE = Toolkit.padString("FILE", SIZE_HEADER);

	private byte[] validationCode;
	private boolean verifiedConnection;
	private long sid;
	private File selectfile;

	public ChatingControler(Socket socket) {
		this.networker = new UnicastNetworker(socket, this);
		verifiedConnection = false;
		validationCode = generateValidationCode();
		sid = 0;
		networker.send(sid++, Toolkit.generateSendData(HEADER_VALIDATE, validationCode));
		synchronized(lock) {
			System.out.print("��֤��Ϣ��");
			printByte(Toolkit.generateSendData(HEADER_VALIDATE, validationCode));
		}
	}

	/**
	 * <p>����һ���������֤�롣</p>
	 * @return ������֤��
	 */
	private byte[] generateValidationCode() {
		int i;
		byte[] result = new byte[SIZE_VALIDATION_CODE];
		long seed = new Date().getTime();
		for (i = 0; i < SIZE_VALIDATION_CODE; i++) {
			result[i] = (byte) (Math.round(Math.random() * seed) & 0xff);
		}
		return result;
	}

	/**
	 * <p>������֤��������Ӧ�롣</p>
	 * <p>�����hash�㷨</p>
	 * @param code ��֤��
	 * @return ���ش�Ӧ��
	 */
	private byte[] calcReplyCode(byte[] code) {
		byte[] result = new byte[SIZE_VALIDATION_CODE];
		int temp, count = 0;
		int i;
		for (i = 0; i * i < SIZE_VALIDATION_CODE; i++) {
			temp = code[i * i] & 0xff; //�������λ��������
			while (temp != 0) {
				if ((temp & 1) == 1)
					count++;
				temp >>>= 1; //�޷�����λ����
			}
		}
		for (i = 0; i < SIZE_VALIDATION_CODE; i++)
			result[i] = (byte) (code[SIZE_VALIDATION_CODE - i - 1] ^ count);
		return result;
	}

	@Override
	public void downConnect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void processRawData(byte[] buf) {
		//if (buf.length < SIZE_HEADER) //���Զ�����
		//	return;
		synchronized(lock) {
			System.out.print("������Ϣ��");
			printByte(buf);
		}
		String header = new String(buf, 0, SIZE_HEADER);
		//System.out.println("receive header:" + header);
		if (header.compareTo(HEADER_VALIDATE) == 0) {
			//�յ���֤����
			if (buf.length < SIZE_HEADER + SIZE_VALIDATION_CODE) //��֤�볤�Ȳ���ȷ
				networker.closeNetworker();

			byte[] code = new byte[SIZE_VALIDATION_CODE];
			for (int i = 0; i < SIZE_VALIDATION_CODE; i++)
				code[i] = buf[SIZE_HEADER + i]; //ȡ��֤��

			networker.send(sid++, Toolkit.generateSendData(HEADER_REPLY_VALIDATE,
					calcReplyCode(code))); //��Ӧ
		} else if (header.compareTo(HEADER_REPLY_VALIDATE) == 0) {
			//�յ���֤��Ӧ
			if (buf.length < SIZE_HEADER + SIZE_VALIDATION_CODE) //��Ӧ�볤�Ȳ���ȷ
				networker.closeNetworker();

			byte[] reply1 = new byte[SIZE_VALIDATION_CODE];
			for (int i = 0; i < SIZE_VALIDATION_CODE; i++)
				reply1[i] = buf[SIZE_HEADER + i]; //ȡ��Ӧ��

			boolean flag = true;
			byte[] reply2 = calcReplyCode(validationCode);
			//��֤��Ӧ��
			for (int i = 0; i < SIZE_VALIDATION_CODE; i++) {
				if (reply1[i] != reply2[i]) {
					flag = false;
					break;
				}
			}
			if (flag) {
				verifiedConnection = true;
				performer = new FrameChating(this); //��֤�ɹ�
			} else {
				networker.closeNetworker();
			}
		} else if (header.compareTo(HEADER_MESSAGE) == 0) {
			if (!verifiedConnection) {
				networker.closeNetworker(); //δ����֤�����ӣ��ر�
			}
			Object[] params = new Object[1];
			params[0] = new String(buf, SIZE_HEADER, buf.length - SIZE_HEADER);
			performer.updateUI(FrameChating.UPDATE_NEWMESSAGE, params);
		} else if (header.compareTo(HEADER_FILE) == 0) {
		}
	}

	@Override
	public void processUIAction(int type, Object[] params) {
		if (type == AC_SENDMESSAGE) {
			networker.send(sid++, Toolkit.generateSendData(HEADER_MESSAGE, (new Date().getTime()
					+ " " + params[0]).getBytes()));
			performer.updateUISend(FrameChating.UPDATE_NEWMESSAGE, params);
		}
		else if (type == AC_SENDFILE) {
			performer.updateUISend(FrameChating.UPDATE_SENDFILE, params);
		}
		else if (type == AC_SAVECHATLOG) {
	        JFileChooser jfc = new JFileChooser( );  
            int r = jfc.showDialog(null, "����");   
               if (r == JFileChooser.APPROVE_OPTION) {   
               selectfile = jfc.getSelectedFile();   
               try{
               	FileWriter output = new FileWriter(selectfile.getPath()+".txt");
               	output.write((String)params[0]);
               	output.close();
               	JOptionPane.showMessageDialog(null, "�������","GUIDES",JOptionPane.INFORMATION_MESSAGE);
               }
               catch(IOException ex) {
               	JOptionPane.showMessageDialog(null, "The file does not exist.","GUIDES",JOptionPane.INFORMATION_MESSAGE);
               }
}
		}
		else if (type == AC_CLOSEWINDOW) {
			performer.updateUI(FrameChating.UPDATE_CLOSE, params);
			performer.updateUISend(FrameChating.UPDATE_CLOSE, params);
			networker.closeNetworker();
		}
	}
	

	@Override
	public void receipt(long sid, boolean result) {
		// TODO Auto-generated method stub

	}

	private synchronized void printByte(byte[] a) {
		int i;
		for (i = 0; i < a.length; i++) {
			System.out.print((a[i] & 0xff) + " ");
		}
		System.out.println();
	}
}
