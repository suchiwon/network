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
					System.out.println("CRC 에러 검출");
				}
				
				msg = frame.getMessage(recvPacket.getData());
				
				NS = frame.getNS(recvPacket.getData());
				msg = frame.getMessage(recvPacket.getData());
				
				recvAddress = recvPacket.getAddress();
				
				System.out.println("client에서 메세지 전송");
				System.out.println("전송 ACK NUM:" + NS + " " + NR);
				
				if(NR == NS)
				{
					System.out.println("알맞은 ACK");
					//System.out.println("client에서 메세지 전송");
					//System.out.println("Client IP:" + recvAddress.getHostAddress());
					//System.out.println("Client port:" + recvPacket.getPort());
					System.out.println("메세지:" + msg);
					
					SwitchACK();
				}
				else
				{
					System.out.println("알맞지 않은 ACK");
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
		
		System.out.println("상대의 채팅 요청 패킷을 기다리는 중입니다.");
		
		try {
			dSocket.receive(recvPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("상대의 채팅 요청 패킷을 받았습니다.");
		
		if(!frame.RecvCRCCheck(recvPacket.getData()))
		{
			System.out.println("CRC 에러 검출");
			bSuccess = false;
		}
		
		Frame.CODE code = frame.getCode(recvPacket.getData());
		
		if(code == Frame.CODE.U_REQUEST_SW)
		{
			System.out.println("상대가 Stop&Wait 방식으로 요청해왔습니다.");
			bSuccess = true;
		}
		else if(code == Frame.CODE.U_REQUEST_GBN)
		{
			System.out.println("상대가 Go-N-Back 방식으로 요청해왔습니다.");
		}
		else if(code == Frame.CODE.U_REQUEST_SR)
		{
			System.out.println("상대가 Selective Repeat 방식으로 요청해왔습니다.");
		}
		else
		{
			System.out.println("상대가 잘못된 형식의 패킷을 보냈습니다.");
		}
		
		/*
		if(bSuccess == true)
		{
			System.out.println("상대방의 요청을 수락하려면 Y를 입력을, 거절하려면 그외의 문자열을 입력하십시오.");
		
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
				System.out.println("상대의 요청을 수락했습니다.");
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
				System.out.println("상대의 요청을 거절하였습니다.");
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

