package data;

import frame.Frame;

public class DataResponse extends Frame {
	private long offset;
	private long length;
	private String payload;
	int type;
	public long getOffset() {
		return offset;
	}
	public void setOffset(long offset) {
		this.offset = offset;
	}
	public long getLength() {
		return length;
	}
	public void setLength(long length) {
		this.length = length;
	}
	public String getPayload() {
		return payload;
	}
	public void setPayload(String payload) {
		this.payload = payload;
	}
	public int getType() {
		return type;
	}
	public DataResponse(){
		this.type = 6;
	}
	
}
