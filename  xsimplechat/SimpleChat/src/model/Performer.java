package model;

public abstract class Performer {

	protected Controler controler;

	protected Performer() {
		this(null);
	}

	protected Performer(Controler controler) {
		this.controler = controler;
	}

	public abstract void updateUI(int type, String s);

}
