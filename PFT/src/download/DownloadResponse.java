package download;

import frame.Frame;

public class DownloadResponse implements Frame {
	private int status;
	private int type;
	private int port;
	private long size;
	public DownloadResponse() {
		this.type = 2;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getType() {
		return type;
	}
	
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	

}
