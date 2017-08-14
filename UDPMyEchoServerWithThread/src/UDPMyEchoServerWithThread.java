import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPMyEchoServerWithThread{
	
	static final int MAX_BUFFER = 1024;
	static final int DEFAULT_PORT_NUM = 3000;
	
	static DatagramSocket dSocket;

	public static void main(String[] args) {
		
		int port = DEFAULT_PORT_NUM;
		
		if(args.length != 1)
		{
			System.out.println("������ ��Ʈ ��ȣ�� �Է��ϼ���. �⺻ ��Ʈ ��ȣ 3000���� �����մϴ�.");
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
		
		new UDPMyEchoServerWithThread().Work(port);
	}

	void Work(int port)
	{
		
		try {
			DatagramSocket dSocket = new DatagramSocket(port);
			
			System.out.println("UDPMyEchoServer ����");
			System.out.println("Server IP:" + InetAddress.getLocalHost().getHostAddress());
			System.out.println("Server port:" + dSocket.getLocalPort());

			Thread r1 = new ReceiveFrame(dSocket);
			r1.start();
			
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	class ReceiveFrame extends Thread 
	{
		byte[] buf = new byte[MAX_BUFFER];
		String msg;
		InetAddress recvAddress;
		DatagramSocket dSocket;
		
		ReceiveFrame(DatagramSocket ds)
		{
			dSocket = ds;
		}
		
		public void run()
		{
			System.out.println("ReceiveFrame ������ ����");
			while(true)
			{
				DatagramPacket recvPacket = new DatagramPacket(buf,buf.length);
				
				try {
					dSocket.receive(recvPacket);
					
					msg = new String(recvPacket.getData(), 0, recvPacket.getLength());
					recvAddress = recvPacket.getAddress();
					
					System.out.println("client���� �޼��� ����");
					System.out.println("Client IP:" + recvAddress.getHostAddress());
					System.out.println("Client port:" + recvPacket.getPort());
					System.out.println("�޼���:" + msg);
					
					DatagramPacket sendPacket = new DatagramPacket(recvPacket.getData(),recvPacket.getData().length,
							recvPacket.getAddress(),recvPacket.getPort());
					dSocket.send(sendPacket);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}



