package ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import model.*;
import javax.swing.*;

import controler.ChatingControler;

public class FrameChating extends Performer {

	JLabel display = new JLabel();
	JTextArea text = new JTextArea();
	JButton send = new JButton("·¢ËÍ");

	public FrameChating(Controler controler1) {
		super(controler1);
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		send.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] params=new Object[1];
				params[0]=text.getText();
				controler.processUIAction(ChatingControler.AC_SENDMESSAGE, params);
			}
		});
		panel.add(display, BorderLayout.NORTH);
		panel.add(text, BorderLayout.CENTER);
		panel.add(send, BorderLayout.SOUTH);
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		frame.setSize(400, 400);
		frame.setVisible(true);
	}

	public static final int UPDATE_NEWMESSAGE = 1;
	public static final int UPDATE_CLOSE = 2;
	public static final int UPDATE_CONNECTSTATE = 3;
	
	@Override
	public void updateUI(int type, Object[] params) {
		if (type == UPDATE_NEWMESSAGE) {
			display.setText((String)params[0]);
		}
		else if(type==UPDATE_CLOSE) {
			//thisframe.setVisible(false);
			//thisframe.dispose();
		}
		else if(type==UPDATE_CONNECTSTATE) {
			//statebar.setText("params")
		}
	}

	@Override
	protected void generateUI() {
		// TODO Auto-generated method stub
		
	}

}
