package checksum;

import frame.Frame;

public class ChecksumResponse extends Frame {
	private int type;
	private long offset;
	private long length;
	private String sha;
	
	public ChecksumResponse() {
		this.type = 8;
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
	public String getSha() {
		return sha;
	}
	public void setSha(String sha) {
		this.sha = sha;
	}
	

}
