package controler;

import ui.*;
import model.*;
import networker.UnicastNetworker;

import java.net.*;
import java.util.Date;

public class ChatingControler extends Controler {

	public static final int AC_SENDMESSAGE = 1;

	public ChatingControler(Socket socket) {
		this.networker = new UnicastNetworker(socket, this);
		this.performer = new FrameChating(this);
	}

	@Override
	public void downConnect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void processMessage(byte[] buf) {
		performer.updateUI(FrameChating.UPDATE_NEWMESSAGE, new Date().getTime() + " "
				+ new String(buf));
	}

	@Override
	public void processUIAction(int type, String s) {
		if (type == AC_SENDMESSAGE) {
			networker.send(1, new Date().getTime() + " " + s);
		}
	}

	@Override
	public void receipt(int sid, boolean result) {
		// TODO Auto-generated method stub

	}

}
