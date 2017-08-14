import java.io.*;
import java.util.*;
import gnu.io.*;

public class SimpleWrite implements Runnable, SerialPortEventListener{
	static Enumeration portList;
    static CommPortIdentifier portId;
    
    String messageString = "tester\n";
    SerialPort serialPort;
    OutputStream outputStream;
    BufferedReader reader;
    
    static Scanner scanner = new Scanner(System.in);

    Thread writeThread;
    
    public static void main(String[] args) {
        portList = CommPortIdentifier.getPortIdentifiers();

        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                if (portId.getName().equals("COM1")) {
                	
                	SimpleWrite write = new SimpleWrite();
                }
            }
        }
    }
    
    public SimpleWrite()
    {
    	try {
            /* 사용 메소드 : 
               public CommPort open(java.lang.String appname, int timeout)
               기능 : 
               어플리케이션 이름과 타임아웃 시간 명시 */
            serialPort = (SerialPort) portId.open("SimpleReadApp", 2000);

        } catch (PortInUseException e) {System.out.println(e.getStackTrace());}
        try {
            // 시리얼 포트에서 입력 스트림을 획득한다.
        	reader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
        	outputStream = serialPort.getOutputStream();
        } catch (IOException e) { }
        // 시리얼 포트의 이벤트 리스너로 자신을 등록한다.
        try {
            serialPort.addEventListener(this);
        } catch (TooManyListenersException e) { }
        
        /* 시리얼 포트에 데이터가 도착하면 이벤트가 한 번 발생되는데
           이 때, 자신이 리스너로 등록된 객체에게 이벤트를 전달하도록 허용. */
        serialPort.notifyOnDataAvailable(true);

        // 시리얼 통신 설정. Data Bit는 8, Stop Bit는 1, Parity Bit는 없음.
        try {
        	
            serialPort.setSerialPortParams(9600, 			    		SerialPort.DATABITS_8, SerialPort.STOPBITS_1,	    		SerialPort.PARITY_NONE);
        } catch (UnsupportedCommOperationException e) { }
        
     // 쓰레드 객체 생성
        writeThread = new Thread(this);

        // 쓰레드 동작
        writeThread.start();
    }
    
    public void run() {
        try {
            Thread.sleep(2000);
            while(true)
            {
            	if(scanner.hasNextLine())
            	{
            		messageString = scanner.nextLine();
            		messageString += "\n";
            		outputStream.write(messageString.getBytes());
            	}
            }
        } catch (Exception e) { }
    }

        public void serialEvent(SerialPortEvent event) {
            // 이벤트의 타입에 따라 switch 문으로 제어.
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


                    // 입력 스트림이 사용가능하면, 버퍼로 읽어 들인 후
                    // String 객체로 변환하여 출력
                    try {
                    	while (true) {
        					String str = reader.readLine();        					
        					System.out.println(str);
        				}

                    } catch (IOException e) {e.getMessage(); }
                    break;
                }
            }
}

