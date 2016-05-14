package checksum;

import frame.Frame;

public class ChecksumRequest implements Frame {
	private int type;
	private long offset;
	private long length;
	public ChecksumRequest() {
		this.type = 7;
	}
	public int getType() {
		return type;
	}
	
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

}
