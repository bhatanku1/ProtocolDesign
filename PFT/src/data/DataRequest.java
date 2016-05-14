package data;

import frame.Frame;

public class DataRequest implements Frame {
	private long offset;
	private long length;
	private int type;
	
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
	public int getType() {
		return type;
	}
	public DataRequest() {
		this.type = 5;
	}

}
