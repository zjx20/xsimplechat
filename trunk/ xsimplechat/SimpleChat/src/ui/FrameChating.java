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
import java.util.*;

public class FrameChating extends Performer {

	private JTextPane receiveText;
	private JScrollPane recepane;
	private JTextArea sendArea;
	private JButton sendmsg;
	private JButton close;
	private JButton save;
	private JButton sendfile;
	private JButton senddir;
	private JLabel labnotice;
	private JLabel labfilename;
	private JLabel labsize;
	private JLabel labbar;
	private JLabel filename;
	private JLabel size;
	private JProgressBar bar;
	private JLabel labdir;
	private JLabel labfilescount;
	private JLabel labbar2;
	private JLabel dirpath;
	private JLabel filescount;
	private JProgressBar bar2;
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

	public static final int UPDATE_MESSAGE_FORRECIEVER = 1;
	public static final int UPDATE_MESSAGE_FORSENDER = 2;
	public static final int UPDATE_CONNECT_DOWN = 3;
	public static final int UPDATE_CLOSE = 4;

	public static final int UPDATE_FILE_REQUEST_FORRECIEVER = 5;
	public static final int UPDATE_FILE_REQUEST_FORSENDER = 6;
	public static final int UPDATE_ACCEPT_FILE_FORSENDER = 7;
	public static final int UPDATE_REFUSE_FILE_FORSENDER = 8;
	public static final int UPDATE_ACCEPT_FILE_FORRECIEVER = 9;
	public static final int UPDATE_REFUSE_FILE_FORRECIEVER = 10;
	public static final int UPDATE_PROGRESS_FORRECIEVER = 11;
	public static final int UPDATE_PROGRESS_FORSENDER = 12;
	public static final int UPDATE_FILE_TRANSFER_COMPLETE = 13;

	public static final int UPDATE_DIRECTORY_REQUEST_FORRECIEVER = 14;
	public static final int UPDATE_DIRECTORY_REQUEST_FORSENDER = 15;
	public static final int UPDATE_ACCEPT_DIRECTORY_FORSENDER = 16;
	public static final int UPDATE_REFUSE_DIRECTORY_FORSENDER = 17;
	public static final int UPDATE_ACCEPT_DIRECTORY_FORRECIEVER = 18;
	public static final int UPDATE_REFUSE_DIRECTORY_FORRECIEVER = 19;
	public static final int UPDATE_DIRECTORY_PROGRESS_FORRECIEVER = 20;
	public static final int UPDATE_DIRECTORY_PROGRESS_FORSENDER = 21;
	public static final int UPDATE_DIRECTORY_TRANSFER_COMPLETE = 22;

	private String tempfile;
	private long tempsize;
	private int totalfiles;
	private int okedfiles;

	@Override
	public void updateUI(int type, Object[] params) {
		switch (type) {
		case UPDATE_MESSAGE_FORRECIEVER:
			appendReceiveText((String) params[0], Color.blue);
			break;
		case UPDATE_MESSAGE_FORSENDER:
			appendReceiveText((String) params[0], Color.GREEN.darker().darker());
			sendArea.setText("");
			break;
		case UPDATE_PROGRESS_FORRECIEVER:
			bar.setValue((Integer) params[0]);
			break;
		case UPDATE_PROGRESS_FORSENDER:
			bar.setValue((Integer) params[0]);
			break;
		case UPDATE_CONNECT_DOWN:
			JOptionPane.showMessageDialog(null, peerClientInfo.getNickName()
					+ "关闭了聊天窗口，连接已断开，请重新连接");
			break;
		case UPDATE_CLOSE:
			frame.setVisible(false);
			frame.dispose();
			break;
		case UPDATE_FILE_REQUEST_FORRECIEVER:
			tempfile = (String) params[0];
			generateFileUI();
			sendname.setText(tempfile);
			tempsize = (Long) params[1];
			sendsize.setText(tempsize / 1024 + "KB");
			setFileEvent();
			break;
		case UPDATE_FILE_REQUEST_FORSENDER:
			labnotice.setText("等待对方响应中...");
			labnotice.setVisible(true);
			break;
		case UPDATE_ACCEPT_FILE_FORRECIEVER:
			labfilename.setVisible(true);
			filename.setVisible(true);
			filename.setText(tempfile);
			labsize.setVisible(true);
			size.setVisible(true);
			size.setText(tempsize / 1024 + "KB");
			labbar.setVisible(true);
			bar.setVisible(true);
			break;
		case UPDATE_ACCEPT_FILE_FORSENDER:
			//JOptionPane.showMessageDialog(null, peerClientInfo.getNickName() + "同意接收文件："
			//		+ params[0], "文件传输", JOptionPane.INFORMATION_MESSAGE);
			labnotice.setVisible(false);
			labfilename.setVisible(true);
			labsize.setVisible(true);
			labbar.setVisible(true);
			bar.setVisible(true);
			filename.setVisible(true);
			filename.setText(files.getName());
			size.setVisible(true);
			size.setText(Long.toString(files.length() / 1024) + "KB");
			break;
		case UPDATE_REFUSE_FILE_FORSENDER:
			labnotice.setVisible(false);
			JOptionPane.showMessageDialog(null, peerClientInfo.getNickName() + "拒绝接收文件："
					+ params[0], "文件传输", JOptionPane.INFORMATION_MESSAGE);
			break;
		case UPDATE_FILE_TRANSFER_COMPLETE:
			//JOptionPane.showMessageDialog(null, params[0] + "传送完毕！", "文件传输",
			//		JOptionPane.INFORMATION_MESSAGE);
			labfilename.setVisible(false);
			labsize.setVisible(false);
			labbar.setVisible(false);
			filename.setVisible(false);
			size.setVisible(false);
			bar.setVisible(false);
			bar.setValue(0);
			break;
		case UPDATE_DIRECTORY_REQUEST_FORRECIEVER:
			new DialogRecieveDirectory((LinkedList<?>) params[0]);
			break;
		case UPDATE_DIRECTORY_REQUEST_FORSENDER:
			totalfiles = (Integer) params[0];
			okedfiles = 0;
			labnotice.setText("等待对方响应中...");
			labnotice.setVisible(true);
			break;
		case UPDATE_ACCEPT_DIRECTORY_FORSENDER:
			labnotice.setVisible(false);
			labdir.setVisible(true);
			labfilescount.setVisible(true);
			labbar2.setVisible(true);
			dirpath.setText((String) params[0]);
			dirpath.setVisible(true);
			bar2.setValue(0);
			bar2.setVisible(true);
			filescount.setText("0/" + totalfiles);
			filescount.setVisible(true);
			break;
		case UPDATE_REFUSE_DIRECTORY_FORSENDER:
			labnotice.setVisible(false);
			JOptionPane.showMessageDialog(null, peerClientInfo.getNickName() + "拒绝接收文件夹："
					+ params[0], "文件传输", JOptionPane.INFORMATION_MESSAGE);
			break;
		case UPDATE_ACCEPT_DIRECTORY_FORRECIEVER:
			labnotice.setVisible(false);
			labdir.setVisible(true);
			labfilescount.setVisible(true);
			labbar2.setVisible(true);
			dirpath.setText((String) params[0]);
			dirpath.setVisible(true);
			bar2.setValue(0);
			bar2.setVisible(true);
			filescount.setText("0/" + totalfiles);
			filescount.setVisible(true);
			break;
		case UPDATE_REFUSE_DIRECTORY_FORRECIEVER:
			break;
		case UPDATE_DIRECTORY_PROGRESS_FORRECIEVER:
			int num1 = (Integer) params[0],
			num2 = (Integer) params[1];
			filescount.setText(num1 + "/" + num2);
			bar2.setValue((int) ((double) num1 / num2 * 100.0));
			break;
		case UPDATE_DIRECTORY_PROGRESS_FORSENDER:
			okedfiles++;
			filescount.setText(okedfiles + "/" + totalfiles);
			bar2.setValue((int) ((double) okedfiles / totalfiles * 100.0));
			if (okedfiles == totalfiles) {
				Object[] param = { dirpath };
				updateUI(UPDATE_DIRECTORY_TRANSFER_COMPLETE, param);
			}
			break;
		case UPDATE_DIRECTORY_TRANSFER_COMPLETE:
			//JOptionPane.showMessageDialog(null, params[0] + "传送完毕！", "文件传输",
			//		JOptionPane.INFORMATION_MESSAGE);
			labdir.setVisible(false);
			labfilescount.setVisible(false);
			labbar2.setVisible(false);
			dirpath.setVisible(false);
			bar2.setVisible(false);
			filescount.setVisible(false);
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
		labnotice = new JLabel("");
		labnotice.setVisible(false);
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
		labdir = new JLabel("传输目录：");
		labdir.setVisible(false);
		labfilescount = new JLabel("传输个数：");
		labfilescount.setVisible(false);
		labbar2 = new JLabel("进度：");
		labbar2.setVisible(false);
		dirpath = new JLabel("");
		dirpath.setVisible(false);
		filescount = new JLabel("");
		filescount.setVisible(false);
		bar2 = new JProgressBar();
		bar2.setVisible(false);
		infoPanel.setPreferredSize(new Dimension(180, 360));
		labnotice.setPreferredSize(new Dimension(150, 20));
		filename.setPreferredSize(new Dimension(100, 20));
		size.setPreferredSize(new Dimension(100, 20));
		bar.setPreferredSize(new Dimension(100, 20));
		dirpath.setPreferredSize(new Dimension(100, 20));
		filescount.setPreferredSize(new Dimension(100, 20));
		bar2.setPreferredSize(new Dimension(100, 20));

		infoPanel.setLayout(new FlowLayout());
		infoPanel.add(labnotice);
		infoPanel.add(labfilename);
		infoPanel.add(filename);
		infoPanel.add(labsize);
		infoPanel.add(size);
		infoPanel.add(labbar);
		infoPanel.add(bar);
		infoPanel.add(labdir);
		infoPanel.add(dirpath);
		infoPanel.add(labfilescount);
		infoPanel.add(filescount);
		infoPanel.add(labbar2);
		infoPanel.add(bar2);

		JPanel menuPanel = new JPanel();
		menuPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		sendmsg = new JButton("  发送   ");
		close = new JButton("关闭");
		save = new JButton("信息保存");
		sendfile = new JButton("传送文件");
		senddir = new JButton("传送文件夹");
		menuPanel.add(sendmsg);
		menuPanel.add(sendfile);
		menuPanel.add(senddir);
		menuPanel.add(save);
		menuPanel.add(close);
		menuPanel.setBorder(BorderFactory.createEtchedBorder());

		peerClientInfo = ((ChatingControler) controler).getPeerClientInfo();

		JFrame.setDefaultLookAndFeelDecorated(true);
		frame = new JFrame();
		frame.setTitle("To: " + peerClientInfo.toString());
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
				chooser.showOpenDialog(frame);
				files = chooser.getSelectedFile();
				if (files == null)
					return;
				params[0] = files;
				controler.processUIAction(ChatingControler.AC_SENDFILE, params);
			}
		});

		senddir.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] params = new Object[1];
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.showOpenDialog(frame);
				files = chooser.getSelectedFile();
				if (files == null)
					return;
				params[0] = files;
				controler.processUIAction(ChatingControler.AC_SENDDIRECTORY, params);
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
		sendframe.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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
				jfc.setDialogType(JFileChooser.SAVE_DIALOG);
				jfc.setSelectedFile(new File(tempfile));
				int r = jfc.showSaveDialog(sendframe);
				if (r == JFileChooser.APPROVE_OPTION)
					selectfile = jfc.getSelectedFile();
				else
					return;
				Object[] params = new Object[3];
				params[0] = tempfile;
				params[1] = selectfile;
				params[2] = tempsize;
				controler.processUIAction(ChatingControler.AC_ACCEPT_FILEREQUEST, params);
				sendframe.setVisible(false);
				sendframe.dispose();
			}
		});

		refuse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] params = { tempfile };
				controler.processUIAction(ChatingControler.AC_REFUSE_FILEREQUEST, params);
				sendframe.setVisible(false);
				sendframe.dispose();
			}
		});
	}

	class DialogRecieveDirectory extends JDialog {

		private static final long serialVersionUID = 3321841463892131179L;

		LinkedList<?> filelist1;

		protected DialogRecieveDirectory(final LinkedList<?> filelist) {
			super(frame);
			final JDialog frame = this;
			this.filelist1 = filelist;
			DefaultListModel listmodel = new DefaultListModel();
			Iterator<?> it = filelist.iterator();
			while (it.hasNext()) {
				listmodel.addElement(it.next());
			}
			JList forfile = new JList(listmodel);
			JButton accept = new JButton("接收");
			JButton refuse = new JButton("取消");
			accept.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser jfc = new JFileChooser();
					jfc.setDialogType(JFileChooser.SAVE_DIALOG);
					jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int r = jfc.showSaveDialog(frame);
					if (r == JFileChooser.APPROVE_OPTION)
						selectfile = jfc.getSelectedFile();
					else
						return;
					Object[] params = { selectfile, filelist1 };
					controler.processUIAction(ChatingControler.AC_ACCEPT_DIRECTORYREQUEST, params);
					frame.setVisible(false);
					frame.dispose();
				}
			});
			refuse.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					Object[] params = { filelist1 };
					controler.processUIAction(ChatingControler.AC_REFUSE_DIRECTORYREQUEST, params);
					frame.setVisible(false);
					frame.dispose();
				}
			});
			JScrollPane scroll = new JScrollPane(forfile);
			Container pane = frame.getContentPane();
			pane.setLayout(new BorderLayout());
			pane.add(scroll, BorderLayout.CENTER);
			JPanel temppanel = new JPanel();
			temppanel.setLayout(new GridLayout(1, 2));
			temppanel.add(accept);
			temppanel.add(refuse);
			pane.add(temppanel, BorderLayout.SOUTH);
			frame.setTitle(peerClientInfo.getNickName() + "发送文件夹，是否接收？");
			frame.setSize(400, 500);
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					frame.setVisible(false);
					frame.dispose();
				}
			});
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		}
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
