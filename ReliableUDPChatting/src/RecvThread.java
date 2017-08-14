import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class RecvThread extends Thread{
	
	static final int MAX_BUFFER = 508;
	static final int MAX_INFORMATION_BUFFER = 500;
	static final int DEFAULT_SEND_PORT_NUM = 3000;
	static final int DEFAULT_RECV_PORT_NUM = 4000;
	static final int RESEND_NUM = 5;
	static final int WAIT_TIME = 1000;
	
	byte[] buf = new byte[MAX_BUFFER];
	byte[] ACK_buf = new byte[MAX_BUFFER];
	
	String msg;
	InetAddress recvAddress;
	
	int recvPort;
	
	DatagramPacket recvPacket, ACKPacket;
	
	DatagramSocket dSocket;
	Frame frame;
	
	int NS, NR = 0;
	int ID;
	
	public RecvThread(int _recvPort, int _ID)
	{
		this.recvPort = _recvPort;
		this.ID = _ID;
		this.frame = new Frame();
	}
	
	public void run()
	{
		try {
			dSocket = new DatagramSocket(recvPort);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		while(!RecvInitPacket());
		
		while(true)
		{
			recvPacket = new DatagramPacket(buf,MAX_BUFFER);
			
			try {
				dSocket.receive(recvPacket);
				
				if(!frame.RecvCRCCheck(recvPacket.getData()))
				{
					System.out.println("CRC ���� ����");
				}
				
				msg = frame.getMessage(recvPacket.getData());
				
				NS = frame.getNS(recvPacket.getData());
				msg = frame.getMessage(recvPacket.getData());
				
				recvAddress = recvPacket.getAddress();
				
				System.out.println("client���� �޼��� ����");
				System.out.println("���� ACK NUM:" + NS + " " + NR);
				
				if(NR == NS)
				{
					System.out.println("�˸��� ACK");
					//System.out.println("client���� �޼��� ����");
					//System.out.println("Client IP:" + recvAddress.getHostAddress());
					//System.out.println("Client port:" + recvPacket.getPort());
					System.out.println("�޼���:" + msg);
					
					SwitchACK();
				}
				else
				{
					System.out.println("�˸��� ���� ACK");
				}
				
				ACK_buf = frame.MakeFrame(ID, Frame.CODE.S_RR, NS, NR, "");
			
				ACKPacket = new DatagramPacket(ACK_buf,MAX_BUFFER,
						recvPacket.getAddress(),recvPacket.getPort());
				dSocket.send(ACKPacket);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private boolean RecvInitPacket()
	{
		recvPacket = new DatagramPacket(buf,buf.length);
		
		boolean bSuccess = false;
		
		System.out.println("����� ä�� ��û ��Ŷ�� ��ٸ��� ���Դϴ�.");
		
		try {
			dSocket.receive(recvPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("����� ä�� ��û ��Ŷ�� �޾ҽ��ϴ�.");
		
		if(!frame.RecvCRCCheck(recvPacket.getData()))
		{
			System.out.println("CRC ���� ����");
			bSuccess = false;
		}
		
		Frame.CODE code = frame.getCode(recvPacket.getData());
		
		if(code == Frame.CODE.U_REQUEST_SW)
		{
			System.out.println("��밡 Stop&Wait ������� ��û�ؿԽ��ϴ�.");
			bSuccess = true;
		}
		else if(code == Frame.CODE.U_REQUEST_GBN)
		{
			System.out.println("��밡 Go-N-Back ������� ��û�ؿԽ��ϴ�.");
		}
		else if(code == Frame.CODE.U_REQUEST_SR)
		{
			System.out.println("��밡 Selective Repeat ������� ��û�ؿԽ��ϴ�.");
		}
		else
		{
			System.out.println("��밡 �߸��� ������ ��Ŷ�� ���½��ϴ�.");
		}
		
		/*
		if(bSuccess == true)
		{
			System.out.println("������ ��û�� �����Ϸ��� Y�� �Է���, �����Ϸ��� �׿��� ���ڿ��� �Է��Ͻʽÿ�.");
		
			try {
				String str = br.readLine();
			
				if(!str.equals("Y"))
					bSuccess = false;
			} catch (IOException e) {
					bSuccess = false;
			}
		}
		*/
		
		if(bSuccess == true)
		{
			byte[] frame = this.frame.MakeFrame(ID, Frame.CODE.U_RECEIVE_READY, 0, 0, "");
			ACKPacket = new DatagramPacket(frame,MAX_BUFFER,recvPacket.getAddress(),recvPacket.getPort());
			
			try {
				dSocket.send(ACKPacket);
				System.out.println("����� ��û�� �����߽��ϴ�.");
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			
			return true;
		}
		else
		{
			byte[] frame = this.frame.MakeFrame(ID, Frame.CODE.U_REJECT, 0, 0, "");
			ACKPacket = new DatagramPacket(frame,MAX_BUFFER,recvPacket.getAddress(),recvPacket.getPort());
			
			try {
				dSocket.send(ACKPacket);
				System.out.println("����� ��û�� �����Ͽ����ϴ�.");
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			
			return true;
		}
	}
	
	private void SwitchACK()
	{
		if(this.NR == 0)
			this.NR = 1;
		else if(this.NR == 1)
			this.NR = 0;
	}	
}

