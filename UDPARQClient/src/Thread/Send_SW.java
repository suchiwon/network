package Thread;

import Main.Frame;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class Send_SW extends SendPacket {
	
	public Send_SW(int _ID, int _port, InetAddress _sendAddress, DatagramSocket _dSocket) {
		super(_ID, _port, _sendAddress, _dSocket);
		NS = 0;
		NR = 0;
	}

	public void run()
	{
		while(true)
		{
			System.out.println("input data:");
			
			try {
				msg = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(msg.length() == 0)
				break;
			
			buf = new byte[FRAME_SIZE];
			buf = frame.MakeFrame(ID, Frame.CODE.I, NS, NR, msg);
			
			sendPacket = new DatagramPacket(buf,buf.length,sendAddress,port);
			try {
				dSocket.send(sendPacket);
				dSocket.setSoTimeout(WAIT_TIME);
				bReceive = false;
				resendNum = 0;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			ACKPacket = new DatagramPacket(buf,FRAME_SIZE);
			
			while(!bReceive)
			{
				try {
					dSocket.receive(ACKPacket);
					bReceive = true;
					
					if(!this.frame.RecvCRCCheck(ACKPacket.getData()))
					{
						throw new SocketTimeoutException();
					}
					
					code = frame.getCode(ACKPacket.getData());
					
					code = Frame.CODE.S_RR;
					
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
					else
					{
						System.out.println("잘못된 형식의 패킷 전송");
					}
					
				} catch (SocketTimeoutException e){
					try {
						if(resendNum >= RESEND_NUM)
						{
							System.out.println("재전송 시도를 초과하였습니다. 종료합니다.");
							return;
						}
						else
						{
							resendNum++;
							System.out.println("timeout 되었습니다. 재전송 합니다. 시도 횟수:" + resendNum);
							dSocket.send(sendPacket);
							dSocket.setSoTimeout(WAIT_TIME);
							bReceive = false;
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
		if(this.NS == 0)
			this.NS = 1;
		else if(this.NS == 1)
			this.NS = 0;
	}

}
