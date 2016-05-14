package upload;

import frame.Frame;

public class UploadRequest implements Frame {
	private int type;
	private String identifier;
	private String fileName;
	private long size;
	private String sha;
	
	public UploadRequest() {
		this.type = 3;
	}

	public int getType() {
		return type;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getSha() {
		return sha;
	}

	public void setSha(String sha) {
		this.sha = sha;
	}
	
	

}
