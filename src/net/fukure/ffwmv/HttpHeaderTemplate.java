package net.fukure.ffwmv;

import java.io.IOException;

public class HttpHeaderTemplate {

	/*これをするとプレーヤーがフリーズする
	public static String getCloseHttpHeader() throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP/1.0 400 Bad Request\r\n");
		sb.append("Server: Rex/11.0.5721.5275\r\n");
		sb.append("Cache-Control: no-cache\r\n");
		sb.append("Pragma: no-cache\r\n");
		sb.append("Pragma: client-id=1977717453\r\n");
		sb.append("\r\n");
		return sb.toString();
	}
	*/

	public static String getStreaming() throws IOException{
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP/1.0 200 OK\r\n");
		sb.append("Server: Rex/11.0.5721.5275\r\n");
		sb.append("Cache-Control: no-cache\r\n");
		sb.append("Pragma: no-cache\r\n");
		sb.append("Pragma: client-id=1977717453\r\n");
		sb.append("Pragma: features=\"broadcast,playlist\"\r\n");
		sb.append("Content-Type: application/x-mms-framed\r\n");
		sb.append("\r\n");
		return sb.toString();
	}
	
	public static String getStreamingRequest(byte[] header) throws IOException{
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP/1.0 200 OK\r\n");
		sb.append("Server: Rex/11.0.5721.5275\r\n");
		sb.append("Cache-Control: no-cache\r\n");
		sb.append("Pragma: no-cache\r\n");
		sb.append("Pragma: client-id=1977717453\r\n");
		sb.append("Pragma: features=\"broadcast,playlist\"\r\n");
		sb.append("Content-Type: application/vnd.ms.wms-hdr.asfv1\r\n");
		sb.append("Content-Length: "+header.length+"\r\n");
		sb.append("Connection: Keep-Alive\r\n");
		sb.append("\r\n");
		return sb.toString();
	}
	
}
