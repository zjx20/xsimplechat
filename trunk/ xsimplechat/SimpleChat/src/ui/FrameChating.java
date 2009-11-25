package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
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
import javax.swing.text.StyleConstants;

import java.util.Date;

import controler.ChatingControler;

public class FrameChating extends Performer {

	private JTextPane receiveText;
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
	private JFrame frame;
	private JFrame sendframe;
	private JLabel labsendname;
	private JLabel sendname;
	private JLabel labsendsize;
	private JLabel sendsize;
	private JButton accept;
	private JButton refuse;

	public FrameChating(Controler controler1) {
		super(controler1);
		setEvent();
	}

	public static final int UPDATE_NEWMESSAGE = 1;
	public static final int UPDATE_CLOSE = 2;
	public static final int UPDATE_CONNECTSTATE = 3;
	public static final int UPDATE_SENDFILE = 4;
	public static final int UPDATE_NEWMESSAGE_MYSELF = 5;
	public static final int UPDATE_CLOSE_MYSELF = 6;
	public static final int UPDATE_CONNECTSTATE_MYSELF = 7;
	public static final int UPDATE_SENDFILE_MYSELF = 8;
	public static final int UPDATE_FILE_REQUEST = 9;
	public static final int UPDATE_ACCEPT_FILE = 10;
	public static final int UPDATE_REFUSE_FILE = 11;
	public static final int UPDATE_ACCEPT_FILE_REQUEST = 12;
	
private String tempfile;

	@Override
	public void updateUI(int type, Object[] params) {
		switch (type) {
		case UPDATE_NEWMESSAGE:
			appendReceiveText((String) params[0], Color.blue);
			break;
		case UPDATE_CLOSE:
			break;
		case UPDATE_CONNECTSTATE:
			bar.setString(" " + "%");
			break;
		case UPDATE_SENDFILE:
			break;
		case UPDATE_NEWMESSAGE_MYSELF:
			appendReceiveText((String) params[0], Color.blue);
			sendArea.setText("");
			break;
		case UPDATE_SENDFILE_MYSELF:
			break;
		case UPDATE_CLOSE_MYSELF:
			frame.setVisible(false);
			frame.dispose();
			break;
		case UPDATE_CONNECTSTATE_MYSELF:
			bar.setString(" " + "%");
			break;
		case UPDATE_FILE_REQUEST:
			tempfile=(String)params[0];
			generateFileUI();
			setFileEvent();
			break;
		case UPDATE_ACCEPT_FILE:
			JOptionPane
					.showMessageDialog(null, "对方同意发送文件", "文件传输", JOptionPane.INFORMATION_MESSAGE);
			labfilename.setVisible(true);
			labsize.setVisible(true);
			labbar.setVisible(true);
			bar.setVisible(true);
			filename.setText(files[0].getName());
			size.setText(Long.toString(files[0].length() / 1024) + "K");
			break;
		case UPDATE_REFUSE_FILE:
			JOptionPane
					.showMessageDialog(null, "对方拒绝发送文件", "文件传输", JOptionPane.INFORMATION_MESSAGE);
		case UPDATE_ACCEPT_FILE_REQUEST:
			labfilename.setVisible(true);
			//filename.setText((String)params[0]);
			labsize.setVisible(true);
			//size.setText((String)params[1]);
			labbar.setVisible(true);
			bar.setVisible(true);
		}
	}

	@Override
	protected void generateUI() {
		// TODO Auto-generated method stub

		JPanel msgPanel = new JPanel();
		msgPanel.setLayout(new BorderLayout());
		receiveText = new JTextPane();
		JScrollPane recepane = new JScrollPane(receiveText);
		recepane.setBorder(BorderFactory.createEtchedBorder());
		sendArea = new JTextArea();
		sendArea.setLineWrap(true);
		JScrollPane sendpane = new JScrollPane(sendArea);
		sendpane.setBorder(BorderFactory.createEtchedBorder());
		sendpane.setPreferredSize(new Dimension(180, 100));
		msgPanel.add(recepane, BorderLayout.CENTER);
		msgPanel.add(sendpane, BorderLayout.SOUTH);

		ImageIcon icon = new ImageIcon(getClass().getResource("picture.jpg"));
		ImagePanel infoPanel = new ImagePanel(icon);//背景图片  
		labname = new JLabel("对方昵称:");
		labfilename = new JLabel("文件名称:");
		labfilename.setVisible(false);
		labsize = new JLabel("文件大小:");
		labsize.setVisible(false);
		labbar = new JLabel("传输进度:");
		labbar.setVisible(false);
		name = new JLabel("");
		filename = new JLabel("");
		size = new JLabel("");
		bar = new JProgressBar();
		bar.setBackground(Color.white);
		bar.setVisible(false);
		bar.setStringPainted(true);
		infoPanel.setPreferredSize(new Dimension(180, 360));
		name.setPreferredSize(new Dimension(100, 20));
		filename.setPreferredSize(new Dimension(100, 20));
		size.setPreferredSize(new Dimension(100, 20));
		bar.setPreferredSize(new Dimension(100, 20));

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
		frame.getContentPane().add(msgPanel, BorderLayout.CENTER);
		frame.getContentPane().add(infoPanel, BorderLayout.EAST);
		frame.getContentPane().add(menuPanel, BorderLayout.SOUTH);
		frame.setSize(550, 500);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);

	}

	void setEvent() {
		sendmsg.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] params = new Object[1];
				params[0] = " (" + new Date().toLocaleString() + ")" + "\n" + "   ";
				params[0] = params[0] + sendArea.getText() + "\n";
				controler.processUIAction(ChatingControler.AC_SENDMESSAGE, params);
			}
		});

		sendfile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] params = new Object[1];
				JFileChooser chooser = new JFileChooser();
				chooser.setMultiSelectionEnabled(true);
				chooser.showOpenDialog(null);
				files = chooser.getSelectedFiles();
				params[0] = files[0];
				controler.processUIAction(ChatingControler.AC_SENDFILE, params);
			}
		});

		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] params = new Object[1];
				params[0] = receiveText.getText().replaceAll("\n", "\r\n");
				controler.processUIAction(ChatingControler.AC_SAVECHATLOG, params);
			}
		});

		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controler.processUIAction(ChatingControler.AC_CLOSEWINDOW, null);
			}
		});
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

	protected void generateFileUI() {
		JFrame.setDefaultLookAndFeelDecorated(true);
		sendframe = new JFrame();
		sendframe.setLayout(null);
		Container sendpanel = sendframe.getContentPane();
		labsendname = new JLabel("文件名称:");
		labsendname.setBounds(10, 60, 55, 25);
		sendpanel.add(labsendname);
		sendname = new JLabel("");
		//	sendname.setText((String) params[0]);
		sendname.setBounds(70, 60, 100, 25);
		sendpanel.add(sendname);
		labsendsize = new JLabel("文件大小:");
		labsendsize.setBounds(10, 90, 55, 25);
		sendpanel.add(labsendsize);
		sendsize = new JLabel("");
		//	sendsize.setText((String) params[1]);
		sendsize.setBounds(70, 90, 100, 25);
		sendpanel.add(sendsize);
		accept = new JButton("接收");
		accept.setBounds(10, 200, 100, 30);
		sendpanel.add(accept);
		refuse = new JButton("拒绝");
		refuse.setBounds(150, 200, 100, 30);
		sendpanel.add(refuse);
		sendframe.setSize(300, 300);
		sendframe.setVisible(true);
		sendframe.setLocationRelativeTo(null);
		sendframe.setResizable(false);
	}

	void setFileEvent() {
		accept.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				int r = jfc.showDialog(null, "保存路径");
				if (r == JFileChooser.APPROVE_OPTION) {
					selectfile = jfc.getSelectedFile();
				}
				Object[] params = new Object[2];
				params[0] = tempfile;
				params[1] = selectfile.getPath();
				sendframe.setVisible(false);
				controler.processUIAction(ChatingControler.AC_ACCEPT_FILEREQUEST, params);
			}
		});

		refuse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendframe.setVisible(false);
				Object[] params = { tempfile };
				controler.processUIAction(ChatingControler.AC_REFUSE_FILEREQUEST, params);
			}
		});
	}

}

class ImagePanel extends JPanel {

	private Image img;

	public ImagePanel(ImageIcon imageIcon) {
		setOpaque(false);
		img = imageIcon.getImage();
		setPreferredSize(new Dimension(imageIcon.getIconWidth(), imageIcon.getIconHeight()));
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(img, 0, 0, this);
	}

	public void setImage(ImageIcon img) {
		if (img != null) {
			this.img = img.getImage();
		}
	}

}
