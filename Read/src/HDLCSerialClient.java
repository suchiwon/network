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

public class HDLCSerialClient implements SerialPortEventListener{
	
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
	                if (portId.getName().equals("COM1")) {
	                	HDLCSerialClient client = new HDLCSerialClient();
	                	client.initPort();
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
            // �ø��� ��Ʈ���� �Է� ��Ʈ���� ȹ���Ѵ�.
        	reader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
        	outputStream = serialPort.getOutputStream();
        } catch (IOException e) { return false; }
        // �ø��� ��Ʈ�� �̺�Ʈ �����ʷ� �ڽ��� ����Ѵ�.
        try {
            serialPort.addEventListener(this);
        } catch (TooManyListenersException e) { return false; }
        
        /* �ø��� ��Ʈ�� �����Ͱ� �����ϸ� �̺�Ʈ�� �� �� �߻��Ǵµ�
           �� ��, �ڽ��� �����ʷ� ��ϵ� ��ü���� �̺�Ʈ�� �����ϵ��� ���. */
        serialPort.notifyOnDataAvailable(true);

        // �ø��� ��� ����. Data Bit�� 8, Stop Bit�� 1, Parity Bit�� ����.
        try {    	
            serialPort.setSerialPortParams(9600, 			    		
            		SerialPort.DATABITS_8, SerialPort.STOPBITS_1,	    		
            		SerialPort.PARITY_NONE);
        } catch (UnsupportedCommOperationException e) { return false; }
        
        System.out.println("��� ��带 �����Ͻʽÿ�. 1. Stop&Wait   2. Go-N-Back   3.Selective Repeat");
        
        int mode = scanner.nextInt();
        
        try {
        	if(mode == SW)
        	{
        		outputStream.write(frame.MakeFrame(ID, Frame.CODE.U_REQUEST_SW, 0, 0, ""));
        	}
        	else if(mode == GNB)
        	{
        		outputStream.write(frame.MakeFrame(ID, Frame.CODE.U_REQUEST_GBN, 0, 0, ""));
        	}
        	else if(mode == SR)
        	{
        		outputStream.write(frame.MakeFrame(ID, Frame.CODE.U_REQUEST_SR, 0, 0, ""));
        	}
        } catch (IOException e){
        	
        }
        
        System.out.println("��û ��Ŷ�� ���½��ϴ�.");
        
        while(!bReceive);
        
        try {
       	 
       	 if(!frame.RecvCRCCheck(reader.readLine().getBytes()))
       	 {
       		 bConnet = false;
       	 }
       	 
       	 Frame.CODE code = frame.getCode(reader.readLine().getBytes());
       	 
       	 if(code == Frame.CODE.U_RECEIVE_READY)
       	 {
       		 System.out.println("������ ������ ����߽��ϴ�.");
       		 bConnet = true;
       	 }
       	 else if(code == Frame.CODE.U_REJECT)
       	 {
       		 System.out.println("������ ������ �ź��߽��ϴ�.");
       		 bConnet = false;
       	 }
       	 else
       	 {
       		 System.out.println("������ �߸��� ���� ��Ŷ�� ���½��ϴ�.");
       		 bConnet = false;
       	 }
       	 
       	 bReceive = true;

        } catch (IOException e) {e.getMessage(); }
        
        if(bConnet)
        {
        	System.out.println("���� ����");
        	return true;
        }
        else
        {
        	System.out.println("���� ����");
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
       
             // �����Ͱ� �����ϸ�
             case SerialPortEvent.DATA_AVAILABLE:
            	 
            	 System.out.println("������ �Խ��ϴ�.");
            	 bReceive = true;
                 // �Է� ��Ʈ���� ��밡���ϸ�, ���۷� �о� ���� ��
                 break;
             }
         }
		
	}

