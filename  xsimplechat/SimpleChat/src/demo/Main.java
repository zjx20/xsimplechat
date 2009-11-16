package demo;
import model.*;
import controler.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Main {
	//static String ip = "172.16.16.155";
	static String ip = "127.0.0.1";
	static int port=5432;
	static int port1=7894;
	static int port2=7895;

	public static void main(String[] args) {
		//if(args.length!=1) System.exit(0);
		//port=Integer.parseInt(args[0]);
		Server cServer = null;
		try {
			cServer = new ChatingServer(ChatingControler.class,port);
			cServer.start();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println(cServer.getLocalPort());
//		Scanner in=new Scanner(System.in);
//		System.out.print("是否发起连接（y|n）：");
//		String op=in.next();
//		if(op.compareTo("y")==0)
//			connect();
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				connect();
			}}).start();
	}

	static void connect() {
//		Scanner in = new Scanner(System.in);
//		System.out.print("输入远程端口：");
//		port = in.nextInt();
		Socket socket = null;
		try {
			socket = new Socket(InetAddress.getByName(ip), port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new ChatingControler(socket);
	}
}

class ChatingServer extends Server {

	protected ChatingServer(Class<?> cControler) throws NoSuchMethodException {
		super(cControler);
	}

	public ChatingServer(Class<?> classControler, int port) throws NoSuchMethodException {
		super(classControler, port);
		// TODO Auto-generated constructor stub
	}

}
