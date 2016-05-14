package upload;

import frame.Frame;

public class UploadResponse extends Frame{
	private int type;
	private int port;
	private int status;
	
	public UploadResponse() {
		this.type = 4;
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

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
