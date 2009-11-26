package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.StyleConstants;

import model.*;
import controler.*;

import java.io.*;
import java.util.Date;

public class FrameChating extends Performer {

	private JTextPane receiveText;
	private JScrollPane recepane;
	private JTextArea sendArea;
	private JButton sendmsg;
	private JButton close;
	private JButton save;
	private JButton sendfile;
	private JLabel labfilename;
	private JLabel labsize;
	private JLabel labbar;
	private JLabel filename;
	private JLabel size;
	private JProgressBar bar;
	private File selectfile;
	private File files;
	private JFrame frame;
	private JFrame sendframe;
	private JLabel labsendname;
	private JLabel sendname;
	private JLabel labsendsize;
	private JLabel sendsize;
	private JButton accept;
	private JButton refuse;
	private ClientInfo peerClientInfo;

	public FrameChating(Controler controler1) {
		super(controler1);
		setEvent();
	}

	public static final int UPDATE_NEWMESSAGE = 1;
	public static final int UPDATE_CONNECTSTATE = 2;
	public static final int UPDATE_NEWMESSAGE_MYSELF = 3;
	public static final int UPDATE_CLOSE_MYSELF = 4;
	public static final int UPDATE_FILE_REQUEST = 5;
	public static final int UPDATE_ACCEPT_FILE = 6;
	public static final int UPDATE_REFUSE_FILE = 7;
	public static final int UPDATE_ACCEPT_FILE_REQUEST = 8;
	public static final int UPDATE_PROGRESS = 9;
	public static final int UPDATE_PROGRESS_MYSELF = 10;
	public static final int UPDATE_FILE_TRANSFER_COMPLETE = 11;

	private String tempfile;
	private long tempsize;

	@Override
	public void updateUI(int type, Object[] params) {
		switch (type) {
		case UPDATE_NEWMESSAGE:
			appendReceiveText((String) params[0], Color.blue);
			break;
		case UPDATE_NEWMESSAGE_MYSELF:
			appendReceiveText((String) params[0], Color.GREEN);
			sendArea.setText("");
			break;
		case UPDATE_PROGRESS:
			bar.setValue((Integer) params[0]);
			break;
		case UPDATE_PROGRESS_MYSELF:
			bar.setValue((Integer) params[0]);
			break;
		case UPDATE_CONNECTSTATE:
			JOptionPane.showMessageDialog(null, peerClientInfo.getNickName()
					+ "关闭了聊天窗口，连接已断开，请重新连接");
			break;
		case UPDATE_CLOSE_MYSELF:
			frame.setVisible(false);
			frame.dispose();
			break;
		case UPDATE_FILE_REQUEST:
			tempfile = (String) params[0];
			generateFileUI();
			sendname.setText(tempfile);
			tempsize = (Long) params[1];
			sendsize.setText(tempsize / 1024 + "KB");
			setFileEvent();
			break;
		case UPDATE_ACCEPT_FILE_REQUEST:
			labfilename.setVisible(true);
			filename.setText(tempfile);
			labsize.setVisible(true);
			size.setText(tempsize / 1024 + "KB");
			labbar.setVisible(true);
			bar.setVisible(true);
			break;
		case UPDATE_ACCEPT_FILE:
			JOptionPane.showMessageDialog(null, peerClientInfo.getNickName() + "同意接收文件："
					+ params[0], "文件传输", JOptionPane.INFORMATION_MESSAGE);
			labfilename.setVisible(true);
			labsize.setVisible(true);
			labbar.setVisible(true);
			bar.setVisible(true);
			filename.setText(files.getName());
			size.setText(Long.toString(files.length() / 1024) + "KB");
			break;
		case UPDATE_REFUSE_FILE:
			JOptionPane.showMessageDialog(null, peerClientInfo.getNickName() + "拒绝接收文件："
					+ params[0], "文件传输", JOptionPane.INFORMATION_MESSAGE);
			break;
		case UPDATE_FILE_TRANSFER_COMPLETE:
			JOptionPane.showMessageDialog(null, params[0] + "传送完毕！", "文件传输",
					JOptionPane.INFORMATION_MESSAGE);
			labfilename.setVisible(false);
			labsize.setVisible(false);
			labbar.setVisible(false);
			bar.setVisible(false);
			break;
		}
	}

	@Override
	protected void generateUI() {

		JPanel msgPanel = new JPanel();
		msgPanel.setLayout(new BorderLayout());
		receiveText = new JTextPane();
		recepane = new JScrollPane(receiveText);
		receiveText.setEditable(false);
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
		labfilename = new JLabel("文件名称:");
		labfilename.setVisible(false);
		labsize = new JLabel("文件大小:");
		labsize.setVisible(false);
		labbar = new JLabel("传输进度:");
		labbar.setVisible(false);
		filename = new JLabel("");
		size = new JLabel("");
		bar = new JProgressBar();
		bar.setBackground(Color.white);
		bar.setVisible(false);
		bar.setStringPainted(true);
		infoPanel.setPreferredSize(new Dimension(180, 360));
		filename.setPreferredSize(new Dimension(100, 20));
		size.setPreferredSize(new Dimension(100, 20));
		bar.setPreferredSize(new Dimension(100, 20));

		infoPanel.setLayout(new FlowLayout());
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

		peerClientInfo = ((ChatingControler) controler).getPeerClientInfo();

		JFrame.setDefaultLookAndFeelDecorated(true);
		frame = new JFrame();
		frame.setTitle(peerClientInfo.toString());
		frame.setLayout(new BorderLayout());
		frame.getContentPane().add(msgPanel, BorderLayout.CENTER);
		frame.getContentPane().add(infoPanel, BorderLayout.EAST);
		frame.getContentPane().add(menuPanel, BorderLayout.SOUTH);
		frame.setSize(550, 500);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

	}

	private void sendMessage() {
		Object[] params = new Object[1];
		Date time = new java.util.Date();
		String nowtime = java.text.DateFormat.getInstance().format(time);
		params[0] = ClientInfo.getCurrentClientInfo().getNickName() + "    (" + nowtime + ")"
				+ "\n" + "   ";
		params[0] = params[0] + sendArea.getText() + "\n";
		controler.processUIAction(ChatingControler.AC_SENDMESSAGE, params);
	}

	class HotKeyListener extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
				sendMessage();
			}
		}

	}

	void setEvent() {
		sendmsg.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendMessage();
			}
		});

		sendfile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] params = new Object[1];
				JFileChooser chooser = new JFileChooser();
				chooser.showOpenDialog(null);
				files = chooser.getSelectedFile();
				if (files == null)
					return;
				params[0] = files;
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

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				controler.processUIAction(ChatingControler.AC_CLOSEWINDOW, null);
			}
		});
		
		recepane.getViewport().addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				receiveText.scrollRectToVisible(new Rectangle(0, receiveText.getHeight()
						- recepane.getHeight() + 25, receiveText.getWidth(),
						recepane.getHeight() - 25));
			}
		});

		sendArea.addKeyListener(new HotKeyListener());
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
		sendname.setBounds(70, 60, 200, 25);
		sendpanel.add(sendname);
		labsendsize = new JLabel("文件大小:");
		labsendsize.setBounds(10, 90, 55, 25);
		sendpanel.add(labsendsize);
		sendsize = new JLabel("");
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
				int r = jfc.showDialog(null, "保存");
				if (r == JFileChooser.APPROVE_OPTION)
					selectfile = jfc.getSelectedFile();
				else
					return;
				Object[] params = new Object[3];
				params[0] = tempfile;
				params[1] = selectfile;
				params[2] = tempsize;
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

	private static final long serialVersionUID = -2239038373691280627L;
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
