package net.fukure.ffwmv.asf;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AsfHeader {

	public final static String header_object_id 		= "30 26 B2 75 8E 66 CF 11 A6 D9 00 AA 00 62 CE 6C";
	public final static String data_object_id 			= "30 26 B2 75 8E 66 CF 11 A6 D9 00 AA 00 62 CE 6C";
	public final static String simple_index_object_id 	= "90 08 00 33 b1 e5 cf 11 89 f4 00 a0 c9 03 49 cb";
	
	public final static String ASF_STREAMING_DATA		= "24 44";
	public final static String ASF_STREAMING_END_TRANS	= "24 45";
	public final static String ASF_STREAMING_HEADER		= "24 48";

	//Header Object
	//30 26 B2 75 8E 66 CF 11 A6 D9 00 AA 00 62 CE 6C
	//Object ID: 75B22630-668E-11CF-A6D9-00AA0062CE6C
	//...
	//	Data Object
	//	Object ID: 75B22636-668E-11CF-A6D9-00AA0062CE6C

	//Data
	//82 00 00 01 5d 00 00 00 00 2e 00 82 02 01 00 00
	//82 00 00 00 5d 2e 00 00 00 00 00 81 01 25 0a 00
	//82 00 00 00 5d 2e 00 00 00 00 00 81 01 8b 16 00
	//82 00 00 00 5d 2e 00 00 00 00 00 81 01 f1 22 00
	//...
	
	//Simple Index Object
	//Object ID: 33000890-E5B1-11CF-89F4-00A0C90349CB
	//90 08 00 33 b1 e5 cf 11 89 f4 00 a0 c9 03 49 cb

	//ASF_STREAMING_END_TRANS
	//$E
	//24 45 08 00 00 00 00 00 00 00 08 00
	
	//ASF_STREAMING_CLEAR
	//$C
	//24 43 04 00 33 00 0D C0
	
	public static byte[] parseHeader(String type, byte[] data, int sequence) throws Exception{
		ByteBuffer bb = ByteBuffer.allocate(12);
		bb.order(ByteOrder.LITTLE_ENDIAN);

		if(type.equals(AsfHeader.ASF_STREAMING_HEADER)){
			bb.put((byte) 0x24);
			bb.put((byte) 0x48);
			bb.putShort((short) (data.length+8));
			bb.putInt((int) sequence);
			bb.put((byte) 0x00);
			bb.put((byte) 0x0C);
			bb.putShort((short) (data.length+8));
		}
		
		if(type.equals(AsfHeader.ASF_STREAMING_DATA)){
			bb.put((byte) 0x24);
			bb.put((byte) 0x44);
			bb.putShort((short) (data.length+8));
			bb.putInt((int) sequence);
			bb.put((byte) 0x00);
			bb.put((byte) 0x00);
			bb.putShort((short) (data.length+8));
		}

		if(type.equals(AsfHeader.ASF_STREAMING_END_TRANS)){
			//なぜかすでにパケットヘッダ$Eがついているのでなにもしない
			
			/*
			//$Cをつけてみる
			ByteBuffer bb2 = ByteBuffer.allocate(12);
			bb2.order(ByteOrder.LITTLE_ENDIAN);
			bb2.put((byte) 0x24);
			bb2.put((byte) 0x43);
			bb2.put((byte) 0x04);
			bb2.put((byte) 0x00);
			bb2.put((byte) 0x33);
			bb2.put((byte) 0x00);
			bb2.put((byte) 0x0D);
			bb2.put((byte) 0xC0);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			baos.write(data);
			baos.write(bb2.array());
			data = baos.toByteArray();
			*/
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(bb.array());
		baos.write(data);
		
		return baos.toByteArray();
	}
	
}
