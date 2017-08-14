package Main;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import Thread.RecvPacket;
import Thread.Recv_GNB;
import Thread.Recv_SR;
import Thread.Recv_SW;

public class UDPARQServer {
	
	static final int FRAME_SIZE = 508;
	static final int DEFAULT_PORT_NUM = 5566;
	static DatagramSocket dSocket;
	static DatagramPacket recvPacket, controlPacket;
	static Frame frame = new Frame();

	public static void main(String[] args) {

		int port = DEFAULT_PORT_NUM;
		boolean bConnect = false;
		int ID = 0;
		
		if(args.length != 1)
		{
			System.out.println("������ ��Ʈ ��ȣ�� �Է��ϼ���. �⺻ ��Ʈ ��ȣ 5566���� �����մϴ�.");
			//System.out.println(dSocket.getLocalAddress());
		}
		else
		{
			try{
			port = Integer.parseInt(args[0]);
			}catch(Exception e){
				System.out.println(e.getMessage());
				port = DEFAULT_PORT_NUM;
			}
		}
		
		try {
			byte[] buf = new byte[FRAME_SIZE];
			dSocket = new DatagramSocket(port);
			recvPacket = new DatagramPacket(buf,buf.length);
			
			//System.out.println(dSocket.);
			
			//System.out.println(dSocket.getLocalAddress());
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}
		
		System.out.println("Ŭ���̾�Ʈ�� Stop&Wait ��� ������ ��û�߽��ϴ�.");
		System.out.println("������ ��û�� �����߽��ϴ�.");
		
		RecvPacket rp = new Recv_SW(ID, dSocket);
		rp.start();
		
		/*
		
		while(!bConnect)
		{
			try {
				dSocket.receive(recvPacket);
			
				if(!frame.RecvCRCCheck(recvPacket.getData()))
				{
					System.out.println("Ŭ���̾�Ʈ�� ��û ��Ŷ�� ������ �ֽ��ϴ�.");
					bConnect = false;
				}
				else
				{
						Frame.CODE code = frame.getCode(recvPacket.getData());
					
						if(code == Frame.CODE.U_REQUEST_SW)
				       	 {
				       		 System.out.println("Ŭ���̾�Ʈ�� Stop&Wait ��� ������ ��û�߽��ϴ�.");
				       		 bConnect = true;
				       	 }
				       	 else if(code == Frame.CODE.U_REQUEST_GBN)
				       	 {
			       		 System.out.println("Ŭ���̾�Ʈ�� Go-N-Back ��� ������ ��û�߽��ϴ�.");
			       		 bConnect = true;
			       	 }
			       	 else if(code == Frame.CODE.U_REQUEST_SR)
			       	 {
			       		 System.out.println("Ŭ���̾�Ʈ�� Selective Repeat ��� ������ ��û�߽��ϴ�.");
			       		 bConnect = true;
			       	 }
			       	 else
			       	 {
			       		 System.out.println("Ŭ���̾�Ʈ�� �߸��� ������ ��Ŷ�� ���½��ϴ�.");
			       		 bConnect = false;
			       	 }
					
					if(bConnect)
					{
						System.out.println("������ ��û�� �����߽��ϴ�.");
	
						controlPacket = new DatagramPacket(frame.MakeFrame(ID, Frame.CODE.U_RECEIVE_READY
								, 0, 0, ""),FRAME_SIZE,recvPacket.getAddress(),recvPacket.getPort());
						
						dSocket.send(controlPacket);
						
						if(code == Frame.CODE.U_REQUEST_SW)
						{
							RecvPacket rp = new Recv_SW(ID, dSocket);
							rp.start();
						}
						else if(code == Frame.CODE.U_REQUEST_GBN)
						{
							RecvPacket rp = new Recv_GNB(ID, dSocket);
							rp.start();
						}
						else if(code == Frame.CODE.U_REQUEST_SR)
						{
							RecvPacket rp = new Recv_SR(ID, dSocket);
							rp.start();
						}
					}
					else
					{
						System.out.println("������ ��û�� �����߽��ϴ�.");
						
						controlPacket = new DatagramPacket(frame.MakeFrame(ID, Frame.CODE.U_REJECT
								, 0, 0, ""),FRAME_SIZE,recvPacket.getAddress(),recvPacket.getPort());
						
						dSocket.send(controlPacket);
					}
				}
			
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		*/
	}
}
