package shutdown;

import frame.Frame;

public class ShutdownRequest extends Frame{
	private int type;
	public ShutdownRequest() {
		this.type = 9;
	}
	public int getType() {
		return type;
	}
	
}
