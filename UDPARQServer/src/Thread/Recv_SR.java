package Thread;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import Main.Frame;

public class Recv_SR extends RecvPacket{
	
	static final int SEQUENCE_NUMBER_SIZE = 8;
	static final int WINDOW_SIZE = 4;
	
	int SF, SL;
	
	boolean[] bReceive;

	public Recv_SR(int _ID, DatagramSocket _dSocket) {
		super(_ID, _dSocket);
		bReceive = new boolean[SEQUENCE_NUMBER_SIZE];
		SF = 0;
		SL = SF + WINDOW_SIZE;
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
						
						if(checkInWindow(NS))
						{
							setReceive(NS);

							if(checkAllReceive())
							{
								switchWindow();
								
								ACKPacket = new DatagramPacket(frame.MakeFrame(ID, Frame.CODE.S_RR, NS, SF, "")
										,FRAME_SIZE,recvPacket.getAddress(),recvPacket.getPort());						
								
								dSocket.send(ACKPacket);
								
								allResetReceive();
							}
							else
							{
								for(int i = SF; i < NS; i++)
								{
									if(!getReceive(i))
									{
										System.out.println("NAK 패킷 전송    NAK:" + i);
										
										ACKPacket = new DatagramPacket(frame.MakeFrame(ID, Frame.CODE.S_REJECT, NS, i, "")
												,FRAME_SIZE,recvPacket.getAddress(),recvPacket.getPort());
										
										dSocket.send(ACKPacket);
									}
								}
							}
						}
						else
						{
							System.out.println("알맞지 않는 NS");
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void allResetReceive()
	{
		for(int i = 0; i < SEQUENCE_NUMBER_SIZE; i++)
			bReceive[i] = false;
	}
	
	private void setReceive(int sNum)
	{
		bReceive[sNum] = true;
	}
	
	private boolean checkAllReceive()
	{
		if(SF < SL)
		{
			for(int i = SF; i < SL; i++)
			{
				if(bReceive[i] == false)
					return false;
			}
		}
		else
		{
			int temp = SF;
			for(int i = 0; i < WINDOW_SIZE; i++)
			{
				if(bReceive[temp] == false)
					return false;
				
				temp++;
				if(temp >= SEQUENCE_NUMBER_SIZE)
					temp = 0;
			}
		}
		
		return true;
	}
	
	private void switchWindow()
	{
		for(int i = 0; i < WINDOW_SIZE; i++)
		{
			SF++;
			SL++;
			if(SF >= SEQUENCE_NUMBER_SIZE)
				SF = 0;
			if(SL >= SEQUENCE_NUMBER_SIZE)
				SL = 0;
		}
		
		System.out.println("Sliding window 변경  SF:" + SF + "  SL:" + SL);
	}
	
	private boolean checkInWindow(int NS)
	{
		if(SF < SL)
		{
			if(NS >= SF && NS < SL)
				return true;
			else
				return false;
		}
		else
		{
			if(NS >= SF || NS < SL)
				return true;
			else
				return false;
		}
	}
	
	private boolean getReceive(int sNum)
	{
		return bReceive[sNum];
	}
}
