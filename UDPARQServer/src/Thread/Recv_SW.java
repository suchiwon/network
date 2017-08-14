package Thread;

import Main.Frame;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Recv_SW extends RecvPacket {

	public Recv_SW(int _ID, DatagramSocket _dSocket) {
		super(_ID, _dSocket);
	}
	
	public void run()
	{
		while(true)
		{
			recvPacket = new DatagramPacket(buf,FRAME_SIZE);
			
			try {
				dSocket.receive(recvPacket);
				
				if(frame.RecvCRCCheck(recvPacket.getData()))
				{
					msg = frame.getMessage(recvPacket.getData());
					
					NS = frame.getNS(recvPacket.getData());
					msg = frame.getMessage(recvPacket.getData());
					
					NS = NR;
					
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
					
					buf = frame.MakeFrame(ID, Frame.CODE.S_RR, NS, NR, "");
				
					ACKPacket = new DatagramPacket(buf,FRAME_SIZE,
							recvPacket.getAddress(),recvPacket.getPort());
					
					dSocket.send(ACKPacket);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
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
