package Thread;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import Main.Frame;

public class Send_SR extends SendPacket{
	
	static final int WINDOW_SIZE = 4;
	static final int SEQUENCE_NUMBER_SIZE = 8;
	static final int RECV_LOOP_NUM = 40;
	
	int SF, SL, S, SP;
	int[] recvLoopNum;
	DatagramPacket[] packetSaver;
	
	public Send_SR(int _ID, int _port, InetAddress _sendAddress, DatagramSocket _dSocket) {
		super(_ID, _port, _sendAddress, _dSocket);
		this.SF = 0;
		this.SL = this.SF + WINDOW_SIZE;
		this.S = 0;
		this.SP = 0;
		this.recvLoopNum = new int[SEQUENCE_NUMBER_SIZE];
		allResetTimer();
		packetSaver = new DatagramPacket[SEQUENCE_NUMBER_SIZE];
	}
	
	public void run()
	{
		while(true)
		{	
			if(SF == SP)
			{
				System.out.println("전달할 메세지 4개를 입력하십시오,");
				try {
					
						for(int i = 0; i < WINDOW_SIZE; i++)
						{
							System.out.println((i+1) + "번째 메세지:");
							
							msg = br.readLine();
							
							sendPacket = new DatagramPacket(frame.MakeFrame(ID, Frame.CODE.I, 
									SP, NR, msg),FRAME_SIZE,sendAddress,port);
							
							savePacket(SP, sendPacket);
							switchSP();
						}
						
						returnS();
						allResetTimer();
						System.out.println("메세지 세팅 완료  SP:" + SP);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else
			{
				if(S == SL)
				{
					
				}
				else if(S != SP)
				{
					try {
						dSocket.send(packetSaver[S]);
						
						System.out.println("메세지 전송    S:" + S);
						setTimer(S);
						switchS();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			try {
				dSocket.setSoTimeout(RECV_TIME_SLICE);
			} catch (SocketException e) {
				e.printStackTrace();
			}
			
			try {
				dSocket.receive(ACKPacket);
				
				if(frame.RecvCRCCheck(ACKPacket.getData()))
				{
					code = frame.getCode(ACKPacket.getData());
					
					if(code == Frame.CODE.S_RR)
					{
						NR = frame.getNR(ACKPacket.getData());
						
						System.out.println("서버의 ACK 패킷 NR:" + NR);
						
						if(checkInWindow(NR))
						{
							while(SF != NR)
							{
								switchWindow();
							}
							
							System.out.println("Sliding window 변경  SF:" + SF + "  SL:" + SL);
						}
					}
					else if(code == Frame.CODE.S_REJECT)
					{
						NR = frame.getNR(ACKPacket.getData());
						
						System.out.println("서버의 NAK 패킷  NR:" + NR);
						
						if(checkInWindow(NR))
						{
							dSocket.send(packetSaver[NR]);
							System.out.println("패킷 재전송  S:" + NR);
							setTimer(NR);
						}
						
					}
					else
					{
						System.out.println("잘못된 서버의 패킷 전송");
					}
				}
				
			} catch (SocketTimeoutException e) {
				
				allIncreaseTimer();
				
				for(int i = SF; i < SL; i++)
				{
					if(recvLoopNum[i] >= RECV_LOOP_NUM)
					{
						System.out.println("Packet" + i + "  timeout 되었습니다. 재전송합니다.");
						
						try {
							dSocket.send(packetSaver[i]);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						setTimer(i);
					}
					
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void savePacket(int sIdx, DatagramPacket desc)
	{
		this.packetSaver[sIdx] = desc;
	}
	
	private void setTimer(int sNum)
	{
		recvLoopNum[sNum] = 0;
	}
	
	private void allResetTimer()
	{
		for(int i = 0; i < SEQUENCE_NUMBER_SIZE; i++)
		{
			recvLoopNum[i] = 0;
		}
	}
	
	private void allIncreaseTimer()
	{
		for(int i = 0; i < SEQUENCE_NUMBER_SIZE; i++)
		{
			this.recvLoopNum[i]++;
		}
	}
	
	private void switchS()
	{
		S++;	
		if(S >= SEQUENCE_NUMBER_SIZE)
			S = 0;
	}
	
	private void returnS()
	{
		S = SF;
	}
	
	private void switchWindow()
	{
		SF++;
		SL++;
		if(SF >= SEQUENCE_NUMBER_SIZE)
			SF = 0;
		if(SL >= SEQUENCE_NUMBER_SIZE)
			SL = 0;
	}
	
	private void switchSP()
	{
		SP++;
		if(SP >= SEQUENCE_NUMBER_SIZE)
			SP = 0;
	}
	
	private boolean checkInWindow(int NR)
	{
		if(SF < SL)
		{
			if(NR >= SF && NR <= SL)
				return true;
			else
				return false;
		}
		else
		{
			if(NR >= SF || NR <= SL)
				return true;
			else
				return false;
		}
	}

}
