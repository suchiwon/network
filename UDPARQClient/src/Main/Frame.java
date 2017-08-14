package Main;

import java.util.zip.Adler32;

public class Frame {
	
	public enum CODE {I, S_RR, S_REJECT, S_RNR, S_SREJ,
		U_REQUEST_SW, U_REQUEST_GBN, U_REQUEST_SR, 
		U_RECEIVE_READY, U_REJECT};
		
	static final int MAX_BUFFER = 508;
	static final int MAX_INFORMATION_BUFFER = 500;
	
	static final int FLAG_START_IDX = 2;
	static final int FLAG_END_IDX = 2;
	static final int ID_IDX = 0;
	static final int CONTROL_IDX = 1;
	static final int INFORMATION_IDX = 8;
	static final int FCS_IDX = 4;
	
	static final byte FLAG = (byte)0b01111110;
		
	private byte NS;
	private byte NR;
	
	private byte[] frame;
	
	private Adler32 adler32 = new Adler32();
	
	private byte[] crcByte = new byte[4];
		
	public byte[] MakeFrame(int ID, CODE code, int _NS, int _NR, String _message)
	{
		frame = new byte[MAX_BUFFER];
		
		frame[FLAG_START_IDX] = FLAG;
		frame[FLAG_END_IDX] = FLAG;
		frame[ID_IDX] = (byte)ID;
		
		byte codeByte = 0b00000000;
		
		setNSByte(_NS);
		setNRByte(_NR);
		
		/*
		switch(code)
		{
			case I:
				codeByte = (byte) (codeByte | this.NS);
				codeByte = (byte) (codeByte | this.NR);
				
				System.arraycopy(_message.getBytes(), 0, frame, INFORMATION_IDX, _message.getBytes().length);
				break;
			case S_RR:
				codeByte = (byte)0b10000000;
				codeByte = (byte) (codeByte | this.NR);
				break;
			case S_REJECT:
				codeByte = (byte)0b10010000;
				codeByte = (byte) (codeByte | this.NR);
				break;
			case S_RNR:
				codeByte = (byte)0b10100000;
				codeByte = (byte) (codeByte | this.NR);
				break;
			case S_SREJ:
				codeByte = (byte)0b10110000;
				codeByte = (byte) (codeByte | this.NR);
				break;
			case U_REQUEST_SW:
				codeByte = (byte)0b11000001;
				break;
			case U_REQUEST_GBN:
				codeByte = (byte)0b11000010;
				break;
			case U_REQUEST_SR:
				codeByte = (byte)0b11000011;
				break;
			case U_RECEIVE_READY:
				codeByte = (byte)0b11010001;
				break;
			case U_REJECT:
				codeByte = (byte)0b11010010;
				break;
		}
		*/
		
		codeByte = (byte) (codeByte | this.NR);
		System.arraycopy(_message.getBytes(), 0, frame, INFORMATION_IDX, _message.getBytes().length);
		frame[3] = (byte)_message.getBytes().length;
		
		frame[CONTROL_IDX] = codeByte;
		
		adler32.reset();
		adler32.update(frame);
		
		int temp = (int)adler32.getValue();
		
		crcByte = intToByte(temp);
		
		System.arraycopy(crcByte, 0, frame, FCS_IDX, 4);
		
		return frame;
	};
	
	public boolean RecvCRCCheck(byte[] frame)
	{
		byte[] temp = new byte[]{
				frame[FCS_IDX],
				frame[FCS_IDX+1],
				frame[FCS_IDX+2],
				frame[FCS_IDX+3]
		};
		
		int sendCRC = byteToInt(temp);
		
		for(int i = 0; i < 4; i++)
			frame[FCS_IDX + i] = 0;
		
		adler32.reset();
		adler32.update(frame);
		
		int recvCRC = (int)adler32.getValue();
		
		if(sendCRC == recvCRC)
			return true;
		else
			return true;
	}
	
	public CODE getCode(byte[] frame)
	{
		byte codeByte = frame[CONTROL_IDX];

		if((codeByte & 0b10000000) == 0b00000000)
		{
			return CODE.I;
		}
		else if((codeByte & 0b11000000) == 0b10000000)
		{
			if((codeByte & 0b00110000) == 0b00000000)
				return CODE.S_RR;
			else if((codeByte & 0b00110000) == 0b00010000)
				return CODE.S_REJECT;
			else if((codeByte & 0b00110000) == 0b00100000)
				return CODE.S_RNR;
			else if((codeByte & 0b00110000) == 0b00110000)
				return CODE.S_SREJ;
		}
		else if((codeByte & 0b11000000) == 0b11000000)
		{
			if((codeByte & 0b00110111) == 0b00000001)
				return CODE.U_REQUEST_SW;
			else if((codeByte & 0b00110111) == 0b00000010)
				return CODE.U_REQUEST_GBN;
			else if((codeByte & 0b00110111) == 0b00000011)
				return CODE.U_REQUEST_SR;
			else if((codeByte & 0b00110111) == 0b00010001)
				return CODE.U_RECEIVE_READY;
			else if((codeByte & 0b00110111) == 0b00010010)
				return CODE.U_REJECT;
		}

		return CODE.I;

	}
	
	public String getMessage(byte[] frame)
	{
		byte[] msgBuf = new byte[MAX_INFORMATION_BUFFER];
		
		System.arraycopy(frame, INFORMATION_IDX, msgBuf, 0, MAX_INFORMATION_BUFFER);
		String msg = new String(msgBuf, 0, msgBuf.length);
		
		return msg;
	}
	
	public int getNS(byte[] frame)
	{
		byte NSByte = frame[CONTROL_IDX];
		NSByte = (byte) (NSByte & 0b01110000);
		NSByte = (byte) (NSByte >> 4);
		return (int)NSByte;
	}
	
	public int getNR(byte[] frame)
	{
		byte NRByte = frame[CONTROL_IDX];
		NRByte = (byte) (NRByte & 0b00000111);
		return (int)NRByte;
	}
	
	private void setNRByte(int _NR) {
		
		if(_NR <= 7)
		{
			this.NR = (byte)_NR;
		}
		else
		{
			this.NR = 0b00000000;
		}
	}

	private void setNSByte(int _NS) {
		
		if(_NS <= 7)
		{
			this.NS = (byte) (_NS << 4);
		}
		else
		{
			this.NS = 0b0000000;
		}
	}
	
	private byte[] intToByte(int n) {

		 byte[] bytes = new byte[] {
				  (byte) (n >> 24)
				 , (byte) (n >> 16)
				 , (byte) (n >> 8)
				 , (byte) (n)
		 };
		 
		 return bytes;
	 }
	
	private int byteToInt(byte[] bytes) {
		 int n = (bytes[0] << 24 & 0xffffffff)
				 | (bytes[1] << 16 & 0xffffff)
				 | (bytes[2] << 8 & 0xffff)
				 | (bytes[3] & 0xff);

		 return n;
	 }

}
