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
			System.out.println("서버의 포트 번호를 입력하세요. 기본 포트 번호 5566으로 진행합니다.");
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
		
		System.out.println("클라이언트가 Stop&Wait 방식 연결을 요청했습니다.");
		System.out.println("상대방의 요청을 수락했습니다.");
		
		RecvPacket rp = new Recv_SW(ID, dSocket);
		rp.start();
		
		/*
		
		while(!bConnect)
		{
			try {
				dSocket.receive(recvPacket);
			
				if(!frame.RecvCRCCheck(recvPacket.getData()))
				{
					System.out.println("클라이언트의 요청 패킷에 에러가 있습니다.");
					bConnect = false;
				}
				else
				{
						Frame.CODE code = frame.getCode(recvPacket.getData());
					
						if(code == Frame.CODE.U_REQUEST_SW)
				       	 {
				       		 System.out.println("클라이언트가 Stop&Wait 방식 연결을 요청했습니다.");
				       		 bConnect = true;
				       	 }
				       	 else if(code == Frame.CODE.U_REQUEST_GBN)
				       	 {
			       		 System.out.println("클라이언트가 Go-N-Back 방식 연결을 요청했습니다.");
			       		 bConnect = true;
			       	 }
			       	 else if(code == Frame.CODE.U_REQUEST_SR)
			       	 {
			       		 System.out.println("클라이언트가 Selective Repeat 방식 연결을 요청했습니다.");
			       		 bConnect = true;
			       	 }
			       	 else
			       	 {
			       		 System.out.println("클라이언트가 잘못된 형식의 패킷을 보냈습니다.");
			       		 bConnect = false;
			       	 }
					
					if(bConnect)
					{
						System.out.println("상대방의 요청을 수락했습니다.");
	
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
						System.out.println("상대방의 요청을 거절했습니다.");
						
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
