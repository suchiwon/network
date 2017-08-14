package Thread;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import Main.Frame;

public class Recv_GNB extends RecvPacket{
	
	static final int SEQUENCE_NUMBER_SIZE = 8;

	public Recv_GNB(int _ID, DatagramSocket _dSocket) {
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
					code = frame.getCode(recvPacket.getData());
					
					if(code == Frame.CODE.I)
					{
						System.out.println("클라이언트의 데이터 패킷 전송");
						
						msg = frame.getMessage(recvPacket.getData());
						
						System.out.println("메세지:" + msg);
						
						NS = frame.getNS(recvPacket.getData());
						
						System.out.println("클라이언트 Sequence num:" + NS);
						
						if(NS == NR)
						{
							System.out.println("알맞은 ACK");
							switchACK();
							
							ACKPacket = new DatagramPacket(frame.MakeFrame(ID, Frame.CODE.S_RR, NS, NR, "")
									,FRAME_SIZE,recvPacket.getAddress(),recvPacket.getPort());
							
							dSocket.send(ACKPacket);
						}
						else
						{
							System.out.println("알맞지 않는 ACK");
						}
					}
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void switchACK()
	{
		NR++;
		
		if(NR >= SEQUENCE_NUMBER_SIZE)
			NR = 0;
	}
	

}
