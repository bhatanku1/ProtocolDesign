package download;

import frame.Frame;

public class DownloadRequest implements Frame {
	private int type;
	private String fileName;
	private String sha;
	private String identifier;
	private long size;
	public DownloadRequest() {
		this.type = 1;
	}
	public int getType() {
		return type;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getSha() {
		return sha;
	}
	public void setSha(String sha) {
		this.sha = sha;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	
	
}
