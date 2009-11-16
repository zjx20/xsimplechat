package controler;

import ui.*;
import model.*;
import networker.*;
import util.*;

import java.net.*;
import java.util.Date;

/**
 * <p>˽��ר��controler</p>
 * @author X
 *
 */
public class ChatingControler extends Controler {

	public static final int AC_SENDMESSAGE = 1;
	public static final int AC_CLOSEWINDOW = 2;
	public static final int AC_SAVECHATLOG = 3;
	public static final int AC_SENDFILE = 4;

	public static final int SIZE_HEADER = 16;
	public static final int SIZE_VALIDATION_CODE = 128;

	public static final String HEADER_VALIDATE = Toolkit.padString("VALIDATE", SIZE_HEADER);
	public static final String HEADER_REPLY_VALIDATE = Toolkit.padString("REVALIDATE", SIZE_HEADER);
	public static final String HEADER_MESSAGE = Toolkit.padString("MESSAGE", SIZE_HEADER);
	public static final String HEADER_FILE = Toolkit.padString("FILE", SIZE_HEADER);

	private byte[] validationCode;
	private boolean verifiedConnection;
	private long sid;

	public ChatingControler(Socket socket) {
		this.networker = new UnicastNetworker(socket, this);
		verifiedConnection = false;
		validationCode = generateValidationCode();
		sid = 0;
		networker.send(sid++, Toolkit.generateSendData(HEADER_VALIDATE, validationCode));
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
	public void processData(byte[] buf) {
		//if (buf.length < SIZE_HEADER) //���Զ�����
		//	return;
		String header = new String(buf, 0, SIZE_HEADER);
		System.out.println("receive header:" + header);
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
			Object[] params=new Object[1];
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
		}
	}

	@Override
	public void receipt(long sid, boolean result) {
		// TODO Auto-generated method stub

	}

}
