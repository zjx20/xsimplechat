package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import model.*;
import javax.swing.*;

import controler.ChatingControler;

public class FrameChating extends Performer {

	public static final int UPDATE_NEWMESSAGE = 1;

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
				controler.processUIAction(ChatingControler.AC_SENDMESSAGE, text.getText());
			}
		});
		panel.add(display, BorderLayout.NORTH);
		panel.add(text, BorderLayout.CENTER);
		panel.add(send, BorderLayout.SOUTH);
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		frame.setSize(400, 400);
		frame.setVisible(true);
	}

	@Override
	public void updateUI(int type, String s) {
		if (type == UPDATE_NEWMESSAGE) {
			display.setText(s);
		}
	}

}
