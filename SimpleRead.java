import java.io.*;
import java.util.*;
//import javax.comm.*;//32��Ʈ
import gnu.io.*;

public class SimpleRead implements Runnable, SerialPortEventListener {
    static CommPortIdentifier portId;
    static Enumeration portList;

    Scanner scanner = new Scanner(System.in);

    SerialPort serialPort;
    Thread readThread;
    BufferedReader reader;
    OutputStream outputStream;
    
    String messageString = "Hello, world!\n";
    
    public static void main(String[] args) {
        // �ý��ۿ� �ִ� ������ ����̹��� ����� �޾ƿ´�.
        portList = CommPortIdentifier.getPortIdentifiers();

        // enumeration type �� portList �� ��� ��ü�� ���Ͽ�
        while (portList.hasMoreElements()) {
        
            // enumeration ���� ��ü�� �ϳ� �����´�.
            portId = (CommPortIdentifier) portList.nextElement();
            // ������ ��ü�� port type �� serial port �̸�
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                
            	if (portId.getName().equals("COM2")) {

                    // Linux �� ���
                    //if (portId.getName().equals("/dev/term/a"))
                        // ��ü ����
                        SimpleRead read = new SimpleRead();
                    }
                }
            }
        }
 // SimpleRead ������
    public SimpleRead() {
        try {
            /* ��� �޼ҵ� : 
               public CommPort open(java.lang.String appname, int timeout)
               ��� : 
               ���ø����̼� �̸��� Ÿ�Ӿƿ� �ð� ��� */
            serialPort = (SerialPort) portId.open("SimpleReadApp", 2000);

        } catch (PortInUseException e) {e.printStackTrace();}
        try {
            // �ø��� ��Ʈ���� �Է� ��Ʈ���� ȹ���Ѵ�.
        	reader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
        	outputStream = serialPort.getOutputStream();
        } catch (IOException e) { }
        // �ø��� ��Ʈ�� �̺�Ʈ �����ʷ� �ڽ��� ����Ѵ�.
        try {
            serialPort.addEventListener(this);
        } catch (TooManyListenersException e) { }
        
        /* �ø��� ��Ʈ�� �����Ͱ� �����ϸ� �̺�Ʈ�� �� �� �߻��Ǵµ�
           �� ��, �ڽ��� �����ʷ� ��ϵ� ��ü���� �̺�Ʈ�� �����ϵ��� ���. */
        serialPort.notifyOnDataAvailable(true);

        // �ø��� ��� ����. Data Bit�� 8, Stop Bit�� 1, Parity Bit�� ����.
        try {
            serialPort.setSerialPortParams(9600, 			    		SerialPort.DATABITS_8, SerialPort.STOPBITS_1,	    		SerialPort.PARITY_NONE);
        } catch (UnsupportedCommOperationException e) { }
        
     // ������ ��ü ����
        readThread = new Thread(this);

        // ������ ����
        readThread.start();
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
        // �̺�Ʈ�� Ÿ�Կ� ���� switch ������ ����.
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


                // �Է� ��Ʈ���� ��밡���ϸ�, ���۷� �о� ���� ��
                // String ��ü�� ��ȯ�Ͽ� ���
                try {
                	while (true) {
    					String str = reader.readLine();
    					System.out.println(str);			        
    				}

                } catch (Exception e){
					// TODO Auto-generated catch block
					e.getMessage();
				}
                break;
            }
        }
    }

