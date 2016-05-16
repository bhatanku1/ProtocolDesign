package pft;

/**
 * Created by ankur on 15.05.2016.
 */
import pft.file_operation.IFileFacade;
import pft.file_operation.PftFileManager;
import pft.frames.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
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
        DataResponse dataResponse;
        if(CheckUploadConditions(frame) == false) {
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
    public boolean CheckUploadConditions(Frame frame){
        //Check duplicate file with diff SHA-1
        //Check space in disk
        //if condition fullfilled, create pft file if not created before, write sha and offset=0
        IFileFacade fileManager;
        fileManager = new PftFileManager(((UploadRequest) frame).filename() + ".pft");
        if(fileManager.fileExits() == false){
            //Create a file
            //write sha
            //offset 0

            try {
                RandomAccessFile raf = new RandomAccessFile(((UploadRequest) frame).filename() + ".pft", "rw");
                String originalString = ((UploadRequest) frame).sha1() + " \n0 \n";
                String updatedString = originalString.replace("\n","\r\n");
                raf.writeBytes(updatedString);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }



        return true;
    }
    public void HandleTermination(){}
    public int GetPort(){
        return 10255;
    }
    public int GetIdentifier(){
        return 12;
    }
    public void PrepareUpload(Frame frame) {
        byte[] dataResponse;
        String line;
        //Read the .pft file and get the offset
        IFileFacade fileManager;
        try {
            RandomAccessFile raf = new RandomAccessFile(((UploadRequest) frame).filename() + ".pft", "rw");
            line = raf.readLine();
            line = raf.readLine();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


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
                    //Start a new thread, create a new socket for the upload and send the dataRequest
                    break;

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

        //The following process is to be started in a new thread
        byte[] datareq = new byte[512];
        DatagramPacket dataReqPacket = new DatagramPacket(buffer, buffer.length);
        while (true){
            try{


            }
            catch (Exception e){
                e.printStackTrace();
            }
        }


    }
}
