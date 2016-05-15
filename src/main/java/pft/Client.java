package pft;

import pft.file_operation.IFileFacade;
import pft.file_operation.PftFileManager;
import pft.frames.DownloadRequest;
import pft.frames.Frame;
import pft.frames.UploadRequest;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

/**
 * Created by ankur on 15.05.2016.
 */
public class Client {
    byte[] payload;
    IFileFacade fileManager;
    long fileSize;
    byte[] sha1;
    UploadRequest uploadRequest;
    DownloadRequest downloadRequest;
    String command;

    public Client(String command, String fileName) {
        this.command = command;
       fileManager = new PftFileManager(fileName);
        if(fileManager.fileExits() == false) {
            System.out.println("File does not exists");
            return;
        }
        fileSize = fileManager.getSize();
        sha1 = fileManager.getHash("SHA-1", 0, fileSize );

        System.out.println("Size of the file: " + fileSize);
        System.out.println("Hash of the file: " + sha1);

        Framer framer = new Framer();
        Frame frame = null;
        Deframer deframer = new Deframer();
        if (command.equals("upload")) {
            //Get the Size and SHA1 of the filename and create constructor using it

            uploadRequest = new UploadRequest(fileName, fileSize, sha1 );
            payload = framer.frame(uploadRequest);
            if(frame instanceof DownloadRequest) {
                System.out.println("True" + ((DownloadRequest) frame).filename());

            }
            else {
                System.out.println("false");
            }
            System.out.println("UploadRequest created: " + uploadRequest.filename() + " " + uploadRequest.sha1() + " " + uploadRequest.type() + " " + uploadRequest.size() + " " + uploadRequest.identifier());
        }

        else {
            byte[] bytes = "00000000000000000000".getBytes();

            downloadRequest = new DownloadRequest(fileName,bytes );
            payload = framer.frame(downloadRequest);
            System.out.println("DownloadRequest created: " + downloadRequest.filename() + " " + downloadRequest.sha1() + " " + downloadRequest.type() + " "  + downloadRequest.identifier());
            frame = deframer.deframe(payload);
            if(frame instanceof DownloadRequest) {
                System.out.println("True" + ((DownloadRequest) frame).filename());

            }
            else {
                System.out.println("false");
            }
        }

        System.out.println(payload.toString());
        InitiateConnection();
    }

    public void InitiateConnection(){
        byte [] response = new byte[512];
        Frame frame = null;
        Deframer deframer = new Deframer();

        DatagramSocket datagramSocket = null;
        try {
            datagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        InetAddress receiverAddress = null;
        try {
            receiverAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        DatagramPacket packet = new DatagramPacket(payload, payload.length, receiverAddress, 180);
        DatagramPacket packetResponse = new DatagramPacket(response, response.length);

        try {
            datagramSocket.send(packet);
            datagramSocket.receive(packetResponse);
            int length = packetResponse.getLength();
            byte[] dataBuffer = Arrays.copyOf(packetResponse.getData(), length);
            frame = deframer.deframe(dataBuffer);
            System.out.println("Response from the server: " + frame.type());



        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
   /* ctor()
   creTE csocket

      */
    //fucntion upload(file name)

    //function download(file name)

