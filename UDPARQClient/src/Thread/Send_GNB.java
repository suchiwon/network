package Thread;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import Main.Frame;

public class Send_GNB extends SendPacket{
	
	static final int WINDOW_SIZE = 3;
	static final int SEQUENCE_NUMBER_SIZE = 8;
	static final int RECV_LOOP_NUM = 40;
	
	int SF, SL, S, SP;
	int recvLoopNum;
	DatagramPacket[] packetSaver;
	
	public Send_GNB(int _ID, int _port, InetAddress _sendAddress, DatagramSocket _dSocket) {
		super(_ID, _port, _sendAddress, _dSocket);
		this.SF = 0;
		this.SL = this.SF + WINDOW_SIZE;
		this.S = 0;
		this.SP = 0;
		this.recvLoopNum = 0;
		packetSaver = new DatagramPacket[SEQUENCE_NUMBER_SIZE];
	}
	
	public void run()
	{
		while(true)
		{	
			if(SF == SP)
			{
				System.out.println("������ �޼����� ������ �Է��Ͻÿ�.(1~7)");
				
				try {
					int msgNum = Integer.parseInt(br.readLine());
					
					if(msgNum >=1 && msgNum <= 7)
					{
						for(int i = 0; i < msgNum; i++)
						{
							System.out.println((i+1) + "��° �޼���:");
							
							msg = br.readLine();
							
							sendPacket = new DatagramPacket(frame.MakeFrame(ID, Frame.CODE.I, 
									SP, NR, msg),FRAME_SIZE,sendAddress,port);
							
							savePacket(SP, sendPacket);
							
							switchSP();
							setTimer();
						}
						
						returnS();
						System.out.println("�޼��� ���� �Ϸ�  SP:" + SP);
					}
					
				} catch (NumberFormatException e) {
					System.out.println("�߸� �Է��Ͽ����ϴ�.");
					e.printStackTrace();
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
						
						System.out.println("�޼��� ����    S:" + S);
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
				
				resetResend();
				
				if(frame.RecvCRCCheck(ACKPacket.getData()))
				{
					code = frame.getCode(ACKPacket.getData());
					
					if(code == Frame.CODE.S_RR)
					{
						NR = frame.getNR(ACKPacket.getData());
						
						System.out.println("������ ���� ��Ŷ NR:" + NR);
						
						if(checkNR(NR))
						{
							while(SF != NR)
								switchWindow();
							
							System.out.println("Sliding window ����  SF:" + SF + "  SL:" + SL);
						}
					}
					else
					{
						System.out.println("�߸��� ������ ��Ŷ ����");
					}
				}
				
			} catch (SocketTimeoutException e) {
				
				recvLoopNum++;
				
				if(recvLoopNum >= RECV_LOOP_NUM)
				{
					resendNum++;
					
					if(resendNum >= RESEND_NUM)
					{
						System.out.println("������ �õ��� �ʰ��Ͽ����ϴ�. �����մϴ�.");
						return;
					}
					System.out.println("timeout �Ǿ����ϴ�. �������մϴ�. ������ Ƚ��:" + resendNum);
					
					returnS();
					setTimer();
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
	
	private void setTimer()
	{
		recvLoopNum = 0;
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
	
	private void resetResend()
	{
		resendNum = 0;
	}
	
	private void switchSP()
	{
		SP++;
		if(SP >= SEQUENCE_NUMBER_SIZE)
			SP = 0;
	}
	
	private boolean checkNR(int NR)
	{
		if(SF < SL)
		{
			if(NR >= SF && NR < SL)
				return true;
			else
				return false;
		}
		else
		{
			if(NR >= SF || NR < SL)
				return true;
			else
				return false;
		}
	}

}
