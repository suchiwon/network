package Main;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;

import Thread.SendPacket;
import Thread.Send_GNB;
import Thread.Send_SR;
import Thread.Send_SW;

public class UDPARQClient {
	
	static final int DEFAULT_PORT_NUM = 5566;
	static final int FRAME_SIZE = 508;
	static final int WAIT_TIME = 10000;
	
	static final int SW = 1;
	static final int GNB = 2;
	static final int SR = 3;
	
	static String address = null;

	public static void main(String[] args) {
		
		String address = null;
		int port = DEFAULT_PORT_NUM;
		InetAddress sendAddress = null;
		int ID = 1;
		int mode;
		
		Frame frame = new Frame();
		
		Scanner scanner = new Scanner(System.in);
		
		if(args.length != 2)
		{
			System.out.println("서버 ip와 포트가 입력되지 않았습니다. 서버 ip를 설정해 주십시오. port는 기본 포트 3000으로 지정됩니다.");
			
			System.out.println("서버 ip 설정:");
			try {
				address = scanner.nextLine();
			} catch (Exception e) {
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
	
		DatagramPacket controlPacket, sendPacket;
		DatagramSocket dSocket = null;
		
		try {
			dSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		SendPacket sp = new Send_SW(ID, port, sendAddress, dSocket);
		sp.start();
		
		/*
		System.out.println("서버와 통신할 ARQ 방식을 선택하십시오.");
		System.out.println("1.Stop&Wait   2.Go-N-Back   3.Selective Repeat");
		
		try {
			mode = scanner.nextInt();
			
			if(mode == SW)
			{
				sendPacket = new DatagramPacket(frame.MakeFrame(ID, Frame.CODE.U_REQUEST_SW,
						0, 0, ""),FRAME_SIZE,sendAddress,port);
			}
			else if(mode == GNB)
			{
				sendPacket = new DatagramPacket(frame.MakeFrame(ID, Frame.CODE.U_REQUEST_GBN,
						0, 0, ""),FRAME_SIZE,sendAddress,port);
			}
			else if(mode == SR)
			{
				sendPacket = new DatagramPacket(frame.MakeFrame(ID, Frame.CODE.U_REQUEST_SR,
						0, 0, ""),FRAME_SIZE,sendAddress,port);
			}
			else
			{
				System.out.println("잘못 입력하셨습니다. 종료합니다.");
				return;
			}
		} catch(Exception e)
		{
			return;
		}
		
		try {
			dSocket.setSoTimeout(WAIT_TIME);
			dSocket.send(sendPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		byte[] buf = new byte[FRAME_SIZE];
		
		controlPacket = new DatagramPacket(buf,buf.length);
		try {
			dSocket.receive(controlPacket);
			
			if(!frame.RecvCRCCheck(controlPacket.getData()))
			{
				System.out.println("서버의 응답 패킷에 에러가 검출되었습니다.");
				return;
			}
			else
			{
				Frame.CODE code = frame.getCode(controlPacket.getData());
				
				code = Frame.CODE.U_RECEIVE_READY;
				
				if(code == Frame.CODE.U_RECEIVE_READY)
				{
					System.out.println("서버가 요청을 받아들였습니다.");
					
					if(mode == SW)
					{
						SendPacket sp = new Send_SW(ID, port, sendAddress, dSocket);
						sp.start();
					}
					else if(mode == GNB)
					{
						SendPacket sp = new Send_GNB(ID, port, sendAddress, dSocket);
						sp.start();
					}
					else if(mode == SR)
					{
						SendPacket sp = new Send_SR(ID, port, sendAddress, dSocket);
						sp.start();
					}
				}
				else if(code == Frame.CODE.U_REJECT)
				{
					System.out.println("서버가 요청을 거절했습니다.");
				}
				else
				{
					System.out.println("서버가 잘못된 응답 패킷을 보냈습니다.");
				}
			}
			
		} catch (SocketTimeoutException e) {
			System.out.println("서버로부터 응답이 없습니다. 종료합니다.");
			return;
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		*/
	}

}
