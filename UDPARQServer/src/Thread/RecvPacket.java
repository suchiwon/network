package Thread;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import Main.Frame;

public class RecvPacket extends Thread {
	
	static final int FRAME_SIZE = 508;
	
	protected Frame frame;
	protected DatagramSocket dSocket;
	protected DatagramPacket recvPacket, ACKPacket;
	protected int ID;
	protected int NR, NS;
	protected byte[] buf;
	protected String msg;
	protected Frame.CODE code;
	
	public RecvPacket(int _ID, DatagramSocket _dSocket)
	{
		ID = _ID;
		dSocket = _dSocket;
		NR = 0;
		NS = 0;
		buf = new byte[FRAME_SIZE];
		frame = new Frame();
	}

}
