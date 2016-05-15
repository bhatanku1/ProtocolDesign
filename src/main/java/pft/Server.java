package pft;

/**
 * Created by ankur on 15.05.2016.
 */
import pft.frames.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class Server {
    public byte[] HandleUpload(Frame frame){
        int identifier;
        int port;
        byte[] payload;
        Framer framer = new Framer();

        UploadResponse uploadResponse;

        if(CheckUploadConditions() == false) {
            //Upload not possible, send a termination
            uploadResponse = new UploadResponse(0,Status.ERROR,-1);
            payload = framer.frame(uploadResponse);
            return payload;
        }
        identifier = GetIdentifier();
        port = GetPort();
        uploadResponse = new UploadResponse(identifier, Status.OK, port);
        payload = framer.frame(uploadResponse);
        return payload;



    }
    public void HandleDownload() {}
    public boolean CheckUploadConditions(){
        return true;
    }
    public void HandleTermination(){}
    public int GetPort(){
        return 10255;
    }
    public int GetIdentifier(){
        return 12;
    }

    public static void main(String [] args) {
        Server server = new Server();
        Framer framer = new Framer();
        Frame frame = null;
        Deframer deframer = new Deframer();
        byte[] response;
        DatagramSocket datagramSocket = null;
        try {
            datagramSocket = new DatagramSocket(180);
        } catch (SocketException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        byte[] buffer = new byte[512];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        while (true){
            try {
                datagramSocket.receive(packet);
                int length = packet.getLength();
                byte[] dataBuffer = Arrays.copyOf(packet.getData(), length);
                frame = deframer.deframe(dataBuffer);
                System.out.println(frame.type());
                if(frame.type() == 3) {
                    //uploadResponse
                    response = server.HandleUpload(frame);
                    packet = new DatagramPacket(response, response.length, packet.getAddress(), packet.getPort());
                    datagramSocket.send(packet);

                }
                else if(frame.type() == 1){
                    //DownloadResponse
                    server.HandleDownload();
                }
                else {
                    //Error: Packet not allowed on this port
                }
                System.out.println(((UploadRequest)frame).filename());
                System.out.print("packet " + buffer.toString());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
}
