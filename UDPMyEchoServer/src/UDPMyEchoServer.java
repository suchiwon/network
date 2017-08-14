import java.io.*;
import java.net.*;
import java.util.Scanner;

public class UDPMyEchoServer {
	
	static final int MAX_BUFFER = 1024;
	static final int DEFAULT_PORT_NUM = 3000;
	
	public static void main(String[] args){
		
		int port = DEFAULT_PORT_NUM;
		
		if(args.length != 1)
		{
			System.out.println("서버의 포트 번호를 입력하세요. 기본 포트 번호 3000으로 진행합니다.");
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
		
		new UDPMyEchoServer().Work(port);
	}
	
	@SuppressWarnings({ "resource", "unused" })
	void Work(int port)
	{
		byte[] buf = new byte[MAX_BUFFER];
		DatagramSocket dSocket = null;
		String msg;
		InetAddress recvAddress;
		
		try {
			dSocket = new DatagramSocket(port);
			
			System.out.println("UDPMyEchoServer 가동");
			System.out.println("Server IP:" + InetAddress.getLocalHost().getHostAddress());
			System.out.println("Server port:" + dSocket.getLocalPort());
			
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		while(true)
		{
			DatagramPacket recvPacket = new DatagramPacket(buf,buf.length);
			
			try {
				dSocket.receive(recvPacket);
				
				msg = new String(recvPacket.getData(), 0, recvPacket.getLength());
				recvAddress = recvPacket.getAddress();
				
				System.out.println("client에서 메세지 전송");
				System.out.println("Client IP:" + recvAddress.getHostAddress());
				System.out.println("Client port:" + recvPacket.getPort());
				System.out.println("메세지:" + msg);
				
				DatagramPacket sendPacket = new DatagramPacket(recvPacket.getData(),recvPacket.getData().length,
						recvPacket.getAddress(),recvPacket.getPort());
				dSocket.send(sendPacket);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
