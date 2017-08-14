import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Timer;

public class UDPMyEcho {
	
	static final int MAX_BUFFER = 1024;
	static final int DEFAULT_PORT_NUM = 3000;
	static final int RESEND_NUM = 5;
	static final int WAIT_TIME = 1000;

	public static void main(String[] args) {
		
		String address = null;
		int port = DEFAULT_PORT_NUM;
		InetAddress sendAddress = null;
		String msg = null;
		int resendNum = 0;
		boolean bReceive = false;
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		if(args.length != 2)
		{
			System.out.println("���� ip�� ��Ʈ�� �Էµ��� �ʾҽ��ϴ�. ���� ip�� ������ �ֽʽÿ�. port�� �⺻ ��Ʈ 3000���� �����˴ϴ�.");
			
			System.out.println("���� ip ����:");
			try {
				address = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else
		{
			address = args[0];
			port = Integer.parseInt(args[1]);
		}
		
		try {
			sendAddress = InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	
		DatagramPacket recvPacket, sendPacket;
		DatagramSocket dSocket = null;
		
		try {
			dSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		while(true)
		{
			byte buf[] = new byte[MAX_BUFFER];
			
			System.out.println("input data:");
			try {
				msg = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(msg.length() == 0)
				break;
			
			buf = msg.getBytes();
			
			sendPacket = new DatagramPacket(buf,buf.length,sendAddress,port);
			try {
				dSocket.send(sendPacket);
				dSocket.setSoTimeout(WAIT_TIME);
				bReceive = false;
				resendNum = 0;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			recvPacket = new DatagramPacket(buf,buf.length);
			
			while(!bReceive)
			{
				try {
					dSocket.receive(recvPacket);
					bReceive = true;
				} catch (SocketTimeoutException e){
					try {
						if(resendNum >= RESEND_NUM)
						{
							System.out.println("������ �õ� ȸ���� �ʰ��Ͽ����ϴ�. ���� ip�� �ٽ� �������ֽʽÿ�.");
							
							System.out.println("���� ip ����:");
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
							try {
								dSocket = new DatagramSocket();
							} catch (SocketException e4) {
								e.printStackTrace();
							}
							break;
						}
						else
						{
							resendNum++;
							System.out.println("timeout �Ǿ����ϴ�. ������ �մϴ�. �õ� Ƚ��:" + resendNum);
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
			
			if(bReceive == true)
			{
				msg = new String(recvPacket.getData());
				System.out.println("Server IP:" + recvPacket.getAddress());
				System.out.println("Server port:" + recvPacket.getPort());
				System.out.println("Echo to Server:" + msg);
			}
		}
	}
}
