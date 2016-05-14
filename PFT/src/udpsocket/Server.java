package udpsocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Server {
	public static void main(String [] args) {
		DatagramSocket datagramSocket = null;
		try {
			datagramSocket = new DatagramSocket(180);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		byte[] buffer = new byte[10];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		while (true){
			try {
				datagramSocket.receive(packet);
				System.out.print(packet.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}
