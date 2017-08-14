package Thread;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import Main.Frame;

public class SendPacket extends Thread {
	
	static final int FRAME_SIZE = 508;
	static final int WAIT_TIME = 2000;
	static final int RECV_TIME_SLICE = 50;
	static final int RESEND_NUM = 5;
	
	protected int port;
	protected InetAddress sendAddress;
	protected DatagramSocket dSocket;
	protected DatagramPacket sendPacket, ACKPacket;
	protected String msg;
	protected int NS, NR;
	protected byte[] buf;
	protected Frame frame;
	protected int ID;
	protected boolean bReceive;
	protected int resendNum;
	protected Frame.CODE code;
	
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	
	SendPacket(int _ID, int _port, InetAddress _sendAddress, DatagramSocket _dSocket)
	{
		this.ID = _ID;
		this.port = _port;
		this.sendAddress = _sendAddress;
		this.dSocket = _dSocket;
		frame = new Frame();
		resendNum = 0;
		NS = 0;
		NR = 0;
		bReceive = false;
		buf = new byte[FRAME_SIZE];
		ACKPacket = new DatagramPacket(buf,buf.length);
	}
	
}
