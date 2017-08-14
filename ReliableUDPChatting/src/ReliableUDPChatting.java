import java.io.*;
import java.net.*;

public class ReliableUDPChatting {
	
	static final int MAX_BUFFER = 508;
	static final int MAX_INFORMATION_BUFFER = 500;
	static final int DEFAULT_SEND_PORT_NUM = 3000;
	static final int DEFAULT_RECV_PORT_NUM = 4000;
	static final int RESEND_NUM = 5;
	static final int WAIT_TIME = 1000;
	
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	
	static String address = null;
	static int sendPort = DEFAULT_SEND_PORT_NUM;
	static int recvPort = DEFAULT_RECV_PORT_NUM;
	static InetAddress sendAddress;
	
	static int ID = 0;
	
	public static void main(String[] args)
	{
		String temp;
		
		if(args.length != 1)
		{	
			System.out.println("자신의 포트 설정:");
			try {
				temp = br.readLine();
				recvPort = Integer.parseInt(temp);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else
		{
			address = args[0];
			sendPort = Integer.parseInt(args[1]);
			recvPort = Integer.parseInt(args[2]);
		}
		
		try {
			sendAddress = InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		Thread sendThread = new SendThread(ID);
		Thread recvThread = new RecvThread(recvPort,ID);
		
		sendThread.start();
		recvThread.start();
	}
}
