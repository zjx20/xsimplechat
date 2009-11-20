package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import model.*;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.StyleConstants;

import controler.ChatingControler;
import controler.MainControler;
import model.ClientInfo;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;


/**
 * @author X
 * 
 */
public class FrameMain extends Performer {

	private static String nickname;
	private JTextPane receiveText;
	private JTextArea sendArea;
	private JButton send;
	private JButton close;
	private JButton save;
	private JTextArea noticeArea;
	private List list;
	private JLabel notice; 
	private JLabel listlab;
	private File selectfile;
	private JFrame frame;


	public FrameMain(Controler controler) {
		super(controler);
		setEvent();
	}

	public static String getNickName() {
		while (nickname == null || nickname.length() >9 || nickname.length() == 0) {
			nickname = JOptionPane.showInputDialog(null, "输入昵称","登陆",JOptionPane.QUESTION_MESSAGE);
			if(nickname == null || nickname.length() == 0) {
				JOptionPane.showMessageDialog(null, "输入昵称长度必须大于0","非法昵称",JOptionPane.ERROR_MESSAGE);
			}
			else if(nickname.length() > 9){
				JOptionPane.showMessageDialog(null, "输入昵称长度必须少于10","非法昵称",JOptionPane.ERROR_MESSAGE);
			}
		}
		return nickname;
	}

	@Override
	public void generateUI() {

		receiveText = new JTextPane();
		sendArea = new JTextArea();
		sendArea.setLineWrap(true); 
		send = new JButton("发送");
		close = new JButton("关闭");
		save = new JButton("信息保存");
		noticeArea = new JTextArea();
		noticeArea.setEditable(false);
		list = new List();
		notice = new JLabel("群公告");
		listlab = new JLabel("在线好友");
		JPanel msgPanel = new JPanel();
		msgPanel.setLayout(new BorderLayout());
		JScrollPane recepane = new JScrollPane(receiveText);
		recepane.setBorder(BorderFactory.createEtchedBorder());
		JScrollPane sendpane = new JScrollPane(sendArea);
		sendpane.setBorder(BorderFactory.createEtchedBorder());
		sendpane.setPreferredSize(new Dimension(180,100));
		msgPanel.add(recepane, BorderLayout.CENTER);
		msgPanel.add(sendpane, BorderLayout.SOUTH);

		JPanel menuPanel = new JPanel();
		menuPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		menuPanel.add(send);
		menuPanel.add(save);
		menuPanel.add(close);
		menuPanel.setBorder(BorderFactory.createEtchedBorder());

		JPanel infoPanel = new JPanel();
		JPanel noticePanel = new JPanel();
		noticePanel.setLayout(new BorderLayout());
		noticePanel.add(notice, BorderLayout.NORTH);
		noticePanel.add(noticeArea, BorderLayout.CENTER);
		noticePanel.setBorder(BorderFactory.createEtchedBorder());
	
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BorderLayout());
		listPanel.add(listlab, BorderLayout.NORTH);
		listPanel.add(list, BorderLayout.CENTER);
		listPanel.setPreferredSize(new Dimension(180, 200));
		listPanel.setBorder(BorderFactory.createEtchedBorder());
		infoPanel.setLayout(new BorderLayout());
		infoPanel.add(noticePanel, BorderLayout.CENTER);
		infoPanel.add(listPanel, BorderLayout.SOUTH);
		infoPanel.setPreferredSize(new Dimension(180, 360));

		JFrame.setDefaultLookAndFeelDecorated(true);
		frame = new JFrame();
		frame.setLayout(new BorderLayout());
		frame.getContentPane().add(msgPanel, BorderLayout.CENTER);
		frame.getContentPane().add(infoPanel, BorderLayout.EAST);
		frame.getContentPane().add(menuPanel, BorderLayout.SOUTH);
		frame.setSize(550, 500);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);

	}
	private InetAddress localIPAddress;
	public void setEvent() {
		/*if(nickname != null) {
			Object[] params = new Object[1];
			params[0] = getNickName();
			controler.processUIAction(MainControler.AC_LOGIN, params);
			//list.add(getNickName);
		}*/
		
		send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] params = new Object[2];
				params[0] = getNickName() + "   ("+new Date().toLocaleString()+")"+"\n";
				params[0] = params[0]+"     "+sendArea.getText()+"\n";
				controler.processUIAction(MainControler.AC_SEND_MESSAGE, params);
			}
		});

		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] params = new Object[1];
				params[0] = receiveText.getText().replaceAll("\n","\r\n");
				controler.processUIAction(MainControler.AC_SAVE_CHATLOG, params);
			}
		});

		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] params = new Object[1];
				params[0] = getNickName();
				controler.processUIAction(MainControler.AC_CLOSE_WINDOW, params);
			}
		});
		
		list.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] name = list.getSelectedItems();
				Object[] params = new Object[1];
				params[0] = name[0];
				controler.processUIAction(MainControler.AC_SINGLE_TALK, params);
			}
		});
	}

	public static final int UPDATE_LOGIN = 1;
	public static final int UPDATE_GROUPMESSAGE = 2;
	public static final int UPDATE_LOGOUT = 3;
	public static final int UPDATE_CLOSEWINDOW =4;

	@Override
	public void updateUI(int type, Object[] args) {
		switch (type) {
		case UPDATE_LOGIN:
			list.add((String)(args[0]));
			break;
		case UPDATE_GROUPMESSAGE:
			System.out.println("msg:"+(String) (args[0]));
			appendReceiveText((String)args[0],Color.blue);
		//	appendReceiveText((String)args[1],Color.black);
			sendArea.setText("");
			break;
		case UPDATE_LOGOUT:
			ClientInfo info = (ClientInfo)args[0];
			list.remove(info.toString());
			break;
		case UPDATE_CLOSEWINDOW:
			frame.setVisible(false);
			break;
		}
			
	}
	
	public void appendReceiveText(String sendInfo, Color color) {
		javax.swing.text.Style style = receiveText.addStyle("title", null);
		if (color != null) {
			StyleConstants.setForeground(style, color);
		} else {
			StyleConstants.setForeground(style, Color.BLACK);
		}
		receiveText.setEditable(true);
		receiveText.setCaretPosition(receiveText.getDocument().getLength());
		receiveText.setCharacterAttributes(style, false);
		receiveText.replaceSelection(sendInfo);
		receiveText.setEditable(false);
	}
}
