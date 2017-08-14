import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class SendThread extends Thread{
	
	static final int MAX_BUFFER = 508;
	static final int MAX_INFORMATION_BUFFER = 500;
	static final int DEFAULT_SEND_PORT_NUM = 3000;
	static final int DEFAULT_RECV_PORT_NUM = 4000;
	static final int RESEND_NUM = 5;
	static final int WAIT_TIME = 1000;
	
	byte[] buf = new byte[MAX_BUFFER];
	byte[] buf_ACK = new byte[MAX_BUFFER];
	
	String msg;
	InetAddress recvAddress;
	int NS = 0;
	int NR = 0;
	DatagramPacket sendPacket, ACKPacket;
	String temp;
	
	int ID = 0;
	
	String address = null;
	int sendPort = DEFAULT_SEND_PORT_NUM;
	InetAddress sendAddress;
	
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	
	DatagramSocket dSocket;
	
	Frame frame;
	
	int resendNum = 0;
	
	boolean bReceive = false;
	
	public SendThread(int _ID)
	{	
		this.ID = _ID;
		this.frame = new Frame();
	}
	
	public void run()
	{
		try {
			dSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		System.out.println("상대의 ip를 설정해 주십시오.");
		
		System.out.println("상대 ip 설정:");
		try {
			address = br.readLine();
			sendAddress = InetAddress.getByName(address);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("상대의 포트 설정:");
		try {
			temp = br.readLine();
			sendPort = Integer.parseInt(temp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(!InitialByUFormat())
		{
			System.out.println("상대측과의 연결 실패로 종료합니다.");
			return;
		}
		
		while(true)
		{
			System.out.print("input data:");
			
			try {
				msg = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(msg.length() == 0)
				break;
			
			buf = new byte[MAX_BUFFER];
			buf = frame.MakeFrame(ID, Frame.CODE.I, NS, NR, msg);
			
			sendPacket = new DatagramPacket(buf,buf.length,sendAddress,sendPort);
			try {
				dSocket.send(sendPacket);
				dSocket.setSoTimeout(WAIT_TIME);
				bReceive = false;
				resendNum = 0;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			ACKPacket = new DatagramPacket(buf_ACK,MAX_BUFFER);
			
			while(!bReceive)
			{
				try {
					dSocket.receive(ACKPacket);
					bReceive = true;
					
					if(!this.frame.RecvCRCCheck(ACKPacket.getData()))
					{
						System.out.println("CRC 에러 검출");
					}
					
					Frame.CODE code = frame.getCode(ACKPacket.getData());
					
					if(code == Frame.CODE.S_RR)
					{
						NR = frame.getNR(ACKPacket.getData());
						
						System.out.println("응답 ACK number:" + NR + " " + NS);
						
						if(NS != NR)
						{
							System.out.println("알맞은 ACK");
							SwitchACK();
						}
					}
					
				} catch (SocketTimeoutException e){
					try {
						if(resendNum >= RESEND_NUM)
						{
							System.out.println("재전송 시도 회수를 초과하였습니다. 서버 ip를 다시 선택해주십시오.");
							
							System.out.println("서버 ip 설정:");
							try {
								address = br.readLine();
							} catch (IOException e2) {
								e.printStackTrace();
							}
							try {
								sendAddress = InetAddress.getByName(address);
							} catch (UnknownHostException e3) {
								e.printStackTrace();
							}
							
							System.out.println("포트 설정:");
							try {
								temp = br.readLine();
								sendPort = Integer.parseInt(temp);
							} catch (Exception e4) {
								e4.printStackTrace();
							}
							
							break;
						}
						else
						{
							resendNum++;
							System.out.println("timeout 되었습니다. 재전송 합니다. 시도 횟수:" + resendNum);
							dSocket.send(sendPacket);
							dSocket.setSoTimeout(WAIT_TIME);
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} catch (IOException e) {
				e.printStackTrace();
				}
			}
		}
	}
	
	private boolean InitialByUFormat()
	{
		byte[] frame;
		
		frame = this.frame.MakeFrame(ID, Frame.CODE.U_REQUEST_SW, 0, 0, "");
		
		sendPacket = new DatagramPacket(frame,frame.length,sendAddress,sendPort);
		
		try {
			dSocket.send(sendPacket);
			ACKPacket = new DatagramPacket(buf, MAX_BUFFER);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		try {
			System.out.println("상대의 응답을 기다리고 있습니다.");
			dSocket.receive(ACKPacket);
			System.out.println("상대의 응답을 받았습니다.");
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		if(!this.frame.RecvCRCCheck(ACKPacket.getData()))
		{
			System.out.println("CRC 에러 검출");
			return false;
		}
			
		Frame.CODE code = this.frame.getCode(ACKPacket.getData());
		
		if(code == Frame.CODE.U_RECEIVE_READY)
		{
			System.out.println("상대측이 요청에 대한 응답을 보냈습니다. 채팅을 시작합니다.");
			return true;
		}
		else if(code == Frame.CODE.U_REJECT)
		{
			System.out.println("상대측이 요청에 대한 연결을 거부했습니다.");
			return false;
		}
		else
		{
			System.out.println("상대측이 잘못된 응답을 보냈습니다.");
			return false;
		}
	}
	
	private void SwitchACK()
	{
		if(this.NS == 0)
			this.NS = 1;
		else if(this.NS == 1)
			this.NS = 0;
	}

}
