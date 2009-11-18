package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import model.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;

import java.util.Date;

import controler.ChatingControler;

public class FrameChating extends Performer {

	private TextArea receiveArea;
	private JTextArea sendArea;
	private JButton sendmsg;
	private JButton close;
	private JButton save;
	private JButton sendfile;
	private JLabel labname;
	private JLabel labfilename;
	private JLabel labsize;
	private JLabel labbar;
	private JLabel name;
	private JLabel filename;
	private JLabel size;
	private JProgressBar bar;
	private File selectfile;
	private File[] files;
	private JFrame  frame;

	
	public FrameChating(Controler controler1) {
		super(controler1);
		setEvent();
	}

	public static final int UPDATE_NEWMESSAGE = 1;
	public static final int UPDATE_CLOSE = 2;
	public static final int UPDATE_CONNECTSTATE = 3;
	public static final int UPDATE_SENDFILE = 4;
	
	@Override
	public void updateUI(int type, Object[] params) {
		if (type == UPDATE_NEWMESSAGE) {
			receiveArea.append((String)params[0]);
		}
		else if(type == UPDATE_CLOSE) {
		
		}
		else if(type == UPDATE_CONNECTSTATE) {
			//statebar.setText("params")
		}
		else if(type == UPDATE_SENDFILE) {

		}
	}

	public void updateUISend(int type, Object[] params) {
		if (type == UPDATE_NEWMESSAGE) {
			receiveArea.append((String)params[0]);
			sendArea.setText("");
		}
		else if (type == UPDATE_SENDFILE) {
			 filename.setText(files[0].getName());
		     size.setText(Long.toString(files[0].length()/1024)+"K");
		}
		else if (type == UPDATE_CLOSE) {
			frame.setVisible(false);
		}
		else if (type == UPDATE_CONNECTSTATE) {
			
		}
	}

	@Override
	protected void generateUI() {
		// TODO Auto-generated method stub

		JPanel msgPanel = new JPanel();
		msgPanel.setLayout(new BorderLayout());
		JPanel recePanel = new JPanel();
		recePanel.setLayout(new BorderLayout());
		receiveArea = new TextArea();
		recePanel.add(receiveArea,BorderLayout.CENTER);
		recePanel.setBorder(BorderFactory.createEtchedBorder());
		JPanel sendPanel = new JPanel();
		sendPanel.setLayout(new BorderLayout());
		sendArea = new JTextArea();
		sendPanel.add(sendArea,BorderLayout.CENTER);
		sendPanel.setBorder(BorderFactory.createEtchedBorder());
		sendPanel.setPreferredSize(new Dimension(180,100));
		msgPanel.add(recePanel,BorderLayout.CENTER);
		msgPanel.add(sendPanel,BorderLayout.SOUTH);
		
		ImageIcon icon=new  ImageIcon(getClass().getResource("picture.jpg"));
	    ImagePanel infoPanel=new ImagePanel(icon);//背景图片  
		labname = new JLabel("对方昵称:");
		labfilename = new JLabel("文件名称:");
		labsize = new JLabel("文件大小:");
		labbar = new JLabel("传输进度:");
		name = new JLabel("");
		filename = new JLabel("");
		size = new JLabel("");
		bar = new JProgressBar();
		infoPanel.setPreferredSize(new Dimension(180,360));
		name.setPreferredSize(new Dimension(100,20));
		filename.setPreferredSize(new Dimension(100,20));
		size.setPreferredSize(new Dimension(100,20));
		bar.setPreferredSize(new Dimension(100,20));
		
		infoPanel.setLayout(new FlowLayout());
		infoPanel.add(labname);
		infoPanel.add(name);
		infoPanel.add(labfilename);
		infoPanel.add(filename);
		infoPanel.add(labsize);
		infoPanel.add(size);
		infoPanel.add(labbar);
		infoPanel.add(bar);

		JPanel menuPanel = new JPanel();
		menuPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		sendmsg = new JButton("  发送   ");
		close = new JButton("关闭");
		save = new JButton("信息保存");
		sendfile = new JButton("传送文件");
		menuPanel.add(sendmsg);
		menuPanel.add(sendfile);
		menuPanel.add(save);
		menuPanel.add(close);
		menuPanel.setBorder(BorderFactory.createEtchedBorder());
		
		JFrame.setDefaultLookAndFeelDecorated(true);
		frame = new JFrame();
		frame.setLayout(new BorderLayout());
		frame.getContentPane().add(msgPanel,BorderLayout.CENTER);
		frame.getContentPane().add(infoPanel,BorderLayout.EAST);
		frame.getContentPane().add(menuPanel,BorderLayout.SOUTH);
		frame.setSize(550,500);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);

	}
	
	void setEvent(){
		sendmsg.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] params=new Object[1];
				params[0] = " ("+new Date().toLocaleString()+")"+"\n"+"   ";
				params[0] = params[0]+sendArea.getText()+"\n";
				controler.processUIAction(ChatingControler.AC_SENDMESSAGE, params);
			}
		});
		
		sendfile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] params=new Object[1];
				   JFileChooser chooser = new JFileChooser();
				    chooser.setMultiSelectionEnabled(true);
				    chooser.showOpenDialog(null);
				     files = chooser.getSelectedFiles();
				     controler.processUIAction(ChatingControler.AC_SENDFILE, params);	
			}
		});
		
		 save.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Object[] params = new Object[1];
					params[0] = receiveArea.getText().replaceAll("\n","\r\n");
					controler.processUIAction(ChatingControler.AC_SAVECHATLOG, params);
				}
			 });
		 
			close.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					controler.processUIAction(ChatingControler.AC_CLOSEWINDOW, null);	
				}
			});	 
	  }
}

class ImagePanel extends JPanel {
	
	 private Image img;
	 
	public ImagePanel(ImageIcon imageIcon) {
		setOpaque(false);
		img=imageIcon.getImage();
		setPreferredSize(new Dimension(imageIcon.getIconWidth(),imageIcon.getIconHeight()));
		}
		
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(img,0,0,this);
		}
                
	public void setImage(ImageIcon img) {
    	if(img!=null) {
    		this.img=img.getImage();
    		}
    }
}
