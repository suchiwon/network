import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.TooManyListenersException;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

public class HDLCSerialServer implements SerialPortEventListener{
	static Enumeration portList;
    static CommPortIdentifier portId;
    
    static final int SW = 1;
    static final int GNB = 2;
    static final int SR = 3;
    
    int ID = 1;
    boolean bConnet = false;
    boolean bReceive = false;
    
    String messageString = "tester\n";
    SerialPort serialPort;
    OutputStream outputStream;
   BufferedReader reader;
    Frame frame = new Frame();
    
    static Scanner scanner = new Scanner(System.in);

    Thread writeThread;

	public static void main(String[] args) {
		
		 portList = CommPortIdentifier.getPortIdentifiers();

	        while (portList.hasMoreElements()) {
	            portId = (CommPortIdentifier) portList.nextElement();
	            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
	                if (portId.getName().equals("COM2")) {
	                	HDLCSerialServer server = new HDLCSerialServer();
	                	server.initPort();
	                }
	            }
	        }
	}
	
	private boolean initPort()
	{
		try {
            serialPort = (SerialPort) portId.open("SimpleReadApp", 2000);

        } catch (PortInUseException e) {System.out.println(e.getStackTrace()); return false;}
        try {
            // 시리얼 포트에서 입력 스트림을 획득한다.
        	reader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
        	outputStream = serialPort.getOutputStream();
        } catch (IOException e) { return false; }
        // 시리얼 포트의 이벤트 리스너로 자신을 등록한다.
        try {
            serialPort.addEventListener(this);
        } catch (TooManyListenersException e) { return false; }
        
        /* 시리얼 포트에 데이터가 도착하면 이벤트가 한 번 발생되는데
           이 때, 자신이 리스너로 등록된 객체에게 이벤트를 전달하도록 허용. */
        serialPort.notifyOnDataAvailable(true);

        // 시리얼 통신 설정. Data Bit는 8, Stop Bit는 1, Parity Bit는 없음.
        try {    	
            serialPort.setSerialPortParams(9600, 			    		
            		SerialPort.DATABITS_8, SerialPort.STOPBITS_1,	    		
            		SerialPort.PARITY_NONE);
        } catch (UnsupportedCommOperationException e) { return false; }
        
        while(!bReceive);
        
        try {
       	 if(!frame.RecvCRCCheck(reader.readLine().getBytes()))
       	 {
       		 bConnet = false;
       	 }
       	 
       	 System.out.println("tester1");
       	 
       	 Frame.CODE code = frame.getCode(reader.readLine().getBytes());
       	 
       	 System.out.println("tester2");
       	 
       	 if(code == Frame.CODE.U_REQUEST_SW)
       	 {
       		 System.out.println("클라이언트가 Stop&Wait 방식 연결을 요청했습니다.");
       		 bConnet = true;
       	 }
       	 else if(code == Frame.CODE.U_REQUEST_GBN)
       	 {
       		 System.out.println("클라이언트가 Go-N-Back 방식 연결을 요청했습니다.");
       		 bConnet = true;
       	 }
       	 else if(code == Frame.CODE.U_REQUEST_SR)
       	 {
       		 System.out.println("클라이언트가 Selective Repeat 방식 연결을 요청했습니다.");
       		 bConnet = true;
       	 }
       	 else
       	 {
       		 System.out.println("클라이언트가 잘못된 형식의 패킷을 보냈습니다.");
       		 bConnet = false;
       	 }
       	 
       	 System.out.println("tester3");
       	 
       	 if(bConnet)
       	 {
       		 outputStream.write(frame.MakeFrame(ID, Frame.CODE.U_RECEIVE_READY, 0, 0, ""));
       	 }
       	 else
       	 {
       		 outputStream.write(frame.MakeFrame(ID, Frame.CODE.U_REJECT, 0, 0, ""));
       	 }
       	 
       	 System.out.println("tester4");
       	 
       	 bReceive = true;

        } catch (Exception e) {e.getMessage(); }
        
        if(bConnet)
        {
        	System.out.println("연결 성공");
        	return true;
        }
        else
        {
        	System.out.println("연결 실패");
        	return false;
        }     
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		
		 switch (event.getEventType()) {
         case SerialPortEvent.BI:
         case SerialPortEvent.OE:
         case SerialPortEvent.FE:
         case SerialPortEvent.PE:
         case SerialPortEvent.CD:
         case SerialPortEvent.CTS:
         case SerialPortEvent.DSR:
         case SerialPortEvent.RI:
         case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
             break;
       
             // 데이터가 도착하면
             case SerialPortEvent.DATA_AVAILABLE:
            	 bReceive = true;
                 break;
             }
         }

}
