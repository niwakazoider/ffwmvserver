package net.fukure.ffwmv;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import net.fukure.ffwmv.asf.AsfHeader;
import net.fukure.ffwmv.asf.AsfStreamData;

public class FFWmvServer extends Thread{

	final private static int defaultport = 47711;
	final boolean debug = true;
	
	private ServerSocket server = null;
	private int port = defaultport;
	private Thread seqThread;
	private AsfStreamData asfData = new AsfStreamData();
	private Socket EncodeSocket = null;
	
	public FFWmvServer(int port) {
		this.port = port;
	}

	public static void main(String[] args) {
		
		int port = defaultport;
		if(args.length>0){
			try{
				port = Integer.parseInt(args[0]);
			}catch(Exception e){}
		}
		FFWmvServer server = new FFWmvServer(port);
		server.start();
		
	}
	
	public void kill(){
		try {
			server.close();
		} catch (Exception e) {}
		try {
			if(seqThread!=null){
				seqThread.interrupt();
			}
		} catch (Exception e) {}
	}

	public void run() {

		try {
			System.out.println("start port:"+port);
			server = new ServerSocket(port);

			while(true){
				Socket socket = server.accept();
				ClientThread rd = new ClientThread(this, socket);
				rd.start();
			}
		} catch (IOException ie) {
			ie.printStackTrace();
			System.out.println("stop");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	class ClientThread extends Thread {
		FFWmvServer server;
		InputStream in;
	    OutputStream os;
		Socket socket;

	    public ClientThread(FFWmvServer server, Socket socket) throws Exception {
	    	this.socket = socket;
	    	this.server = server;
	    	this.in = socket.getInputStream();
	        this.os = socket.getOutputStream();
	    }

	    private String readHttpHeader(ChunkedInputStream cin) throws IOException{
	    	StringBuilder sb = new StringBuilder();
	    	String header = "dummy";
	    	while(header.length()!=0){
	    		header = cin.readline();
	    		sb.append(header);
	    		sb.append("\r\n");
	    	}
			
			return sb.toString();
	    }
	    
	    private byte[] readHttpChunkedData(ChunkedInputStream cin) throws IOException{
			return cin.readChunk();
	    }
	    
	    private void hexOutput(byte[] data){
	    	boolean debug = true;
			if(hexString(data, 2).equals("82 00")){
				int i = new Random().nextInt(30);
				if(i>1) debug = false;
			}
			if(debug){
				System.out.println(data.length+" "+hexString(data, 16));
			}
	    }
	    
	    private void onAsfPacket(byte[] data) throws Exception{

			if(server.debug){
				hexOutput(data);
			}

	    	//$H
	    	if(hexString(data, 16).toUpperCase().equals(AsfHeader.header_object_id)){
	    		addAsfChunk(AsfHeader.ASF_STREAMING_HEADER, data);
	    	}

	    	//$D
	    	if(hexString(data, 2).equals("82 00")){
	    		addAsfChunk(AsfHeader.ASF_STREAMING_DATA, data);
	    	}
	    	
	    	//$E
	    	if(hexString(data, 2).equals("24 45")){
	    		addAsfChunk(AsfHeader.ASF_STREAMING_END_TRANS, data);
	    	}
	    }
	    

		private void addAsfChunk(String type, byte[] data) throws Exception{
			byte[] chunk = AsfHeader.parseHeader(type, data, server.asfData.getSequence());
			if(type.equals(AsfHeader.ASF_STREAMING_HEADER)){
				if(server.debug){
					System.out.println("new streaming header");
				}
				server.asfData.addHeader(chunk);
			}else{
				server.asfData.addData(chunk);
			}
		}
		
		@Override
		public void run() {
	    	
	    	try {
	        	ChunkedInputStream cin = new ChunkedInputStream(in);
	        	
	        	String httpHeader = readHttpHeader(cin);

	        	//ffmpeg
	    		if(httpHeader.indexOf("POST")==0){
	    			//同時に２つの送信接続が来たら切断する
	    			if(EncodeSocket!=null){
	    				throw new Exception();
	    			}
	    			EncodeSocket = socket;
	    			try{
		    			if(server.debug){
		    		    	System.out.println("post from:"+socket.getInetAddress());
		    			}
		    			server.asfData.reset();
		        	
			        	byte[] chunkedData = new byte[1];//dummy
			        	while(chunkedData.length>0){
			        		chunkedData = readHttpChunkedData(cin);
			        		onAsfPacket(chunkedData);
			        	}
		    			//server.asfData.reset();
	    			}catch(Exception e){
	    				
	    			}
	    			EncodeSocket = null;
	    		}
	    		
	    		//peercast, wmp
	    		if(httpHeader.indexOf("GET")==0){
	    			if(server.debug){
	    		    	System.out.println("get from:"+socket.getInetAddress());
	    			}

	    			byte[] streamingHeader = server.asfData.getHeader();
	    			
	    			if(streamingHeader==null){
	    				throw new Exception();
	    				//プレーヤーが固まるので送信しない
	        			//String header = getCloseHttpHeader();
	        			//os.write(header.getBytes());
	    			}
	    			
	    			if(httpHeader.indexOf("xPlayStrm=1")<0){
	    				String header = HttpHeaderTemplate.getStreamingRequest(streamingHeader);
	        			os.write(header.getBytes());
	        			os.write(streamingHeader);
	    				throw new Exception();
	    			}
	    			
	    			String header = HttpHeaderTemplate.getStreaming();
	    			os.write(header.getBytes());
	    			os.write(streamingHeader);
	    			os.flush();
	    			
	    			int wait = 0;
	    			int nowait = 0;
	    			int seq = server.asfData.getSequence()-20;
	    			byte[] chunk;
	    			while(true){
	    				chunk = server.asfData.getData(seq);
	    				if(chunk!=null){
	        				os.write(chunk);
	        				os.flush();
	        				seq++;
	        				wait = 0;
	        				nowait++;
	        				if(nowait>100){
	        					Thread.sleep(100);
	        					nowait = 0;
	        				}
	    				}else{
	    					int latestseq = server.asfData.getSequence();
		    				if(seq>=latestseq && latestseq>0){
		    					Thread.sleep(100);
		    					wait++;
		    					nowait = 0;
		    				}else{
		    					break;
		    				}
	    					if(wait>50) break;
	    				}
						
	    			}
	    		}
	    	} catch(IOException ie){
	    		System.out.println("connection abort");
	        } catch(Exception e) {
			}
	        
	        try {
	            in.close();
	            os.close();
	        } catch(IOException e) {}
	        
	    }
	    
	    private String hexString(byte[] by, int len){
	    	StringBuilder sb = new StringBuilder();
	    	int i=0;
	    	for (int b : by) {
	    		int b2 = b & 0xff;
	    		if (b2 < 16) sb.append("0");
	    		sb.append(Integer.toHexString(b2));
	    		sb.append(" ");
	    		i++;
	    		if(i>=len) break;
	    	}
	    	return sb.toString().trim();
	    }
	}
	
}

