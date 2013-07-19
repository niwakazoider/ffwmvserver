package net.fukure.ffwmv.asf;
import java.util.TreeMap;


public class AsfStreamData {
	private byte[] header = null;
	private TreeMap<Integer, byte[]> dataList = new TreeMap<Integer, byte[]>();
	private int sequence = 0;
	private Object syncObj = new Object();
	
	public int getSequence(){
		synchronized (syncObj) {
			return sequence;
		}
	}
	
	public void reset(){
		synchronized (syncObj) {
			//再接続のときヘッダを送ってこないことがあるのでヘッダはリセットしない
			//header = null;
			dataList.clear();
			sequence = 0;
		}
	}
	
	public void addHeader(byte[] data){
		header = data;
	}
	
	public void addData(byte[] data){
		synchronized (syncObj) {
			dataList.put(sequence, data);
			if(dataList.size()>=500){
				dataList.remove(dataList.firstKey());
			}
			sequence++;
		}
	}
	
	public byte[] getHeader(){
		return header;
	}
	
	public byte[] getData(int seq){
		synchronized (syncObj) {
			return dataList.get(seq);
		}
	}
}
