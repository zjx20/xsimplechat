package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import model.*;

import java.awt.event.*;
import javax.swing.*;

import controler.ChatingControler;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


/**
 * @author X
 * 
 */
public class FrameMain extends Performer {

	private String nickname;
	private JTextArea receiveArea;
	private JTextArea sendArea;
	private JButton send;
	private JButton close;
	private JButton save;
	private JTextArea noticeArea;
	private List list;
	private JLabel notice;
	private JLabel listlab;
	private File selectfile;

	public FrameMain() {
		this(null);
	}
	
	public FrameMain(Controler bridge) {
		super(bridge);
		generateUI();
		setEvent();
	}
	
	public void generateUI(){
		nickname = JOptionPane.showInputDialog(null, "�����ǳ�","��½",JOptionPane.QUESTION_MESSAGE);
		
		receiveArea = new JTextArea();
		sendArea = new JTextArea();
		send = new JButton("����");
		close = new JButton("�ر�");
		save = new JButton("��Ϣ����");
		noticeArea = new JTextArea();
		list = new List();
		notice = new JLabel("Ⱥ����");
		listlab = new JLabel("���ߺ���");
		JPanel msgPanel = new JPanel();
		msgPanel.setLayout(new BorderLayout());
		JPanel recePanel = new JPanel();
		recePanel.setLayout(new BorderLayout());
		recePanel.add(receiveArea,BorderLayout.CENTER);
		recePanel.setBorder(BorderFactory.createEtchedBorder());
		JPanel sendPanel = new JPanel();
		sendPanel.setLayout(new BorderLayout());
		sendPanel.add(sendArea,BorderLayout.CENTER);
		sendPanel.setBorder(BorderFactory.createEtchedBorder());
		sendPanel.setPreferredSize(new Dimension(180,100));
		msgPanel.add(recePanel,BorderLayout.CENTER);
		msgPanel.add(sendPanel,BorderLayout.SOUTH);
		
		JPanel menuPanel = new JPanel();
		menuPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		menuPanel.add(send);
		menuPanel.add(save);
		menuPanel.add(close);
		menuPanel.setBorder(BorderFactory.createEtchedBorder());
		
		JPanel infoPanel = new JPanel();
		JPanel noticePanel = new JPanel();
		noticePanel.setLayout(new BorderLayout());
		noticePanel.add(notice,BorderLayout.NORTH);
		noticePanel.add(noticeArea,BorderLayout.CENTER);
		noticePanel.setBorder(BorderFactory.createEtchedBorder());
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BorderLayout());
		listPanel.add(listlab,BorderLayout.NORTH);
		listPanel.add(list,BorderLayout.CENTER);
		listPanel.setPreferredSize(new Dimension(180,200));
		listPanel.setBorder(BorderFactory.createEtchedBorder());
		infoPanel.setLayout(new BorderLayout());
		infoPanel.add(noticePanel,BorderLayout.CENTER);
		infoPanel.add(listPanel,BorderLayout.SOUTH);
		infoPanel.setPreferredSize(new Dimension(180,360));
		
		JFrame frame = new JFrame();
		frame.setLayout(new BorderLayout());
		frame.getContentPane().add(msgPanel,BorderLayout.CENTER);
		frame.getContentPane().add(infoPanel,BorderLayout.EAST);
		frame.getContentPane().add(menuPanel,BorderLayout.SOUTH);
		frame.setSize(500,400);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);	
		
	}

		public void setEvent(){
			send.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
				}
			});
			
			 save.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						
					}
				 });
				
				close.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						
					}
				});
	}
	
	@Override
	public void updateUI(int type, Object[] args) {
		// TODO Auto-generated method stub

	}
	
	public void updateUISend(int type, Object[] args) {
		
	}
}
