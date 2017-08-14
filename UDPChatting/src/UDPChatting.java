import java.io.*;
import java.net.*;

public class UDPChatting {
	
	static final int MAX_BUFFER = 1024;
	static final int DEFAULT_SEND_PORT_NUM = 3000;
	static final int DEFAULT_RECV_PORT_NUM = 4000;
	static final int RESEND_NUM = 5;
	static final int WAIT_TIME = 1000;
	
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	
	static String address = null;
	static int sendPort = DEFAULT_SEND_PORT_NUM;
	static int recvPort = DEFAULT_RECV_PORT_NUM;
	static InetAddress sendAddress;
	
	public static void main(String[] args)
	{
		String temp;
		
		if(args.length != 3)
		{
			System.out.println("서버 ip와 포트가 입력되지 않았습니다. 서버 ip를 설정해 주십시오.");
			
			System.out.println("서버 ip 설정:");
			try {
				address = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.out.println("서버 포트 설정:");
			try {
				temp = br.readLine();
				sendPort = Integer.parseInt(temp);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			System.out.println("받는 포트 설정:");
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

		Thread sendThread = new SendFrame();
		Thread recvThread = new RecvFrame();
		
		sendThread.start();
		recvThread.start();
	}
	
	static class SendFrame extends Thread{
		
		byte[] buf = new byte[MAX_BUFFER];
		byte[] buf_ACK = new byte[1];
		
		String msg;
		InetAddress recvAddress;
		int ACK_num = 0;
		DatagramPacket sendPacket, ACKPacket;
		String temp;
		
		DatagramSocket dSocket;
		
		int resendNum = 0;
		boolean bReceive = false;
		
		public void run()
		{
			try {
				dSocket = new DatagramSocket();
			} catch (SocketException e) {
				e.printStackTrace();
			}
			
			while(true)
			{
				System.out.print("input data:");
				
				try {
					msg = br.readLine();
					msg += ACK_num;
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				if(msg.length() == 0)
					break;
				
				buf = msg.getBytes();
				
				sendPacket = new DatagramPacket(buf,buf.length,sendAddress,sendPort);
				try {
					dSocket.send(sendPacket);
					dSocket.setSoTimeout(WAIT_TIME);
					bReceive = false;
					resendNum = 0;
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				ACKPacket = new DatagramPacket(buf_ACK,1);
				
				while(!bReceive)
				{
					try {
						dSocket.receive(ACKPacket);
						bReceive = true;
						System.out.println("응답 ACK number:" + (ACKPacket.getData()[0] & 0xFF));
						if(ACK_num != (ACKPacket.getData()[0] & 0xFF))
						{
							System.out.println("알맞은 ACK");
							SwitchACK();
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
		
		private void SwitchACK()
		{
			if(this.ACK_num == 0)
				this.ACK_num = 1;
			else if(this.ACK_num == 1)
				this.ACK_num = 0;
		}
	}
	
	static class RecvFrame extends Thread{

		byte[] buf = new byte[MAX_BUFFER];
		byte[] buf_ACK = new byte[1];
		
		String msg;
		InetAddress recvAddress;
		
		DatagramPacket recvPacket, ACKPacket;
		
		DatagramSocket dSocket;
		
		int ACK_recv_num = 0;
		int recv_ACK;
		
		public void run()
		{
			try {
				dSocket = new DatagramSocket(recvPort);
			} catch (SocketException e) {
				e.printStackTrace();
			}
			
			while(true)
			{
				recvPacket = new DatagramPacket(buf,buf.length);
				
				try {
					dSocket.receive(recvPacket);
					
					msg = new String(recvPacket.getData(), 0, recvPacket.getLength());
					
					recv_ACK = Integer.parseInt(msg.substring(msg.length() - 1));
					msg = msg.substring(0, msg.length() - 1);
					
					recvAddress = recvPacket.getAddress();
					
					System.out.println("client에서 메세지 전송");
					System.out.println("전송 ACK NUM:" + recv_ACK);
					
					if(ACK_recv_num == recv_ACK)
					{
						System.out.println("알맞은 ACK");
						//System.out.println("client에서 메세지 전송");
						//System.out.println("Client IP:" + recvAddress.getHostAddress());
						//System.out.println("Client port:" + recvPacket.getPort());
						System.out.println("메세지:" + msg);
						
						SwitchACK();
						
						buf_ACK = new byte[] {(byte)ACK_recv_num};
					}
					else
					{
						System.out.println("알맞지 않은 ACK");
					}
				
					ACKPacket = new DatagramPacket(buf_ACK,1,
							recvPacket.getAddress(),recvPacket.getPort());
					dSocket.send(ACKPacket);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		private void SwitchACK()
		{
			if(this.ACK_recv_num == 0)
				this.ACK_recv_num = 1;
			else if(this.ACK_recv_num == 1)
				this.ACK_recv_num = 0;
		}
		
	}

}
