package pft;

import pft.file_operation.IFileFacade;
import pft.file_operation.PftFileManager;
import pft.frames.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.*;
import java.util.Arrays;

/**
 * Created by ankur on 15.05.2016.
 */
public class Client {
    private byte[] payload;
    private IFileFacade fileManager;
    private long fileSize;
    private byte[] sha1;
    private UploadRequest uploadRequest;
    private DownloadRequest downloadRequest;
    private String command;
    private String fileName;
    private DatagramSocket datagramSocket = null;
    private InetAddress receiverAddress = null;

    public Client(String command, String fileName) {
        this.command = command;
        this.fileName = fileName;
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
            System.out.println("UploadRequest created: " + uploadRequest.filename() + " " + uploadRequest.sha1() + " " + uploadRequest.type() + " " + uploadRequest.size() + " " + uploadRequest.identifier());
        }

        else {
            byte[] bytes = "00000000000000000000".getBytes();
            downloadRequest = new DownloadRequest(fileName,bytes );
            payload = framer.frame(downloadRequest);
            System.out.println("DownloadRequest created: " + downloadRequest.filename() + " " + downloadRequest.sha1() + " " + downloadRequest.type() + " "  + downloadRequest.identifier());
        }

        System.out.println(payload.toString());
        InitiateConnection(this.command);
    }

    public void InitiateConnection(String command){
        byte [] response = new byte[512];
        Frame frame = null;
        Deframer deframer = new Deframer();

        try {
            datagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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

            if(command.equals("upload") && ((UploadResponse) frame).status() == Status.OK) {
                //Start upload process

                System.out.println("Upload Request Accepted");
                UploadProcess(frame);
            }
            else if(command.equals("download") && ((DownloadResponse) frame).status() == Status.OK) {
                //Start Download process
                DownloadProcess();
                System.out.println("Download Request Accepted");
            }



        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public void UploadProcess(Frame frame){
        System.out.println("Starting upload...");
        DataResponse dataResponse;
        long offset;
        long lengthDataToBeSend;
        int lengthDataRequestPacket;
        long window;
        long fileLeft = fileSize;
        int readBytes = 4096;
        byte [] dataFromFile;
        byte [] dataRequest = new byte[512];
        byte [] dataPayload = new byte[5004];

        int i;
        Framer framer = new Framer();
        Frame frameDataRequest = null;
        Deframer deframer = new Deframer();
        int port = ((UploadResponse) frame).port();

        DatagramPacket dataRequestPacket = new DatagramPacket(dataRequest, dataRequest.length);
        DatagramPacket dataResponsePacket ;

        try {
            datagramSocket.setSoTimeout(10000);
        } catch (SocketException e) {
            //
            e.printStackTrace();
        }
        while(true){
            try {
                datagramSocket.receive(dataRequestPacket);
                lengthDataRequestPacket = dataRequestPacket.getLength();
                byte[] dataBufferRequest = Arrays.copyOf(dataRequestPacket.getData(), lengthDataRequestPacket);
                frameDataRequest = deframer.deframe(dataBufferRequest);
                if(frameDataRequest.type() != 5) {
                    throw new Exception("Incorrect Frame Type");
                }
                else if(frameDataRequest.type() == 9) {

                    //file uploaded successfully; This is a terminating packet
                    break;
                }
                offset = ((DataRequest) frame).offset();
                lengthDataToBeSend =  ((DataRequest) frame).length();
                window = lengthDataToBeSend / 4096;
               if(window == 0) {
                   window = 1;
                   readBytes = (int)lengthDataToBeSend;

               }
                for (i = 0; i< window; i++) {
                    dataFromFile = fileManager.readFromPosition((int)(offset),readBytes);
                    dataResponse = new DataResponse(((UploadResponse) frame).identifier(),offset, readBytes, dataFromFile);
                    dataPayload = framer.frame(dataResponse);
                    dataResponsePacket = new DatagramPacket(dataPayload, dataPayload.length, receiverAddress, ((UploadResponse)frame).port());
                    datagramSocket.send(dataResponsePacket);
                    offset += 4096;
                }

            }    catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

    }
    public void DownloadProcess() {


    }


}

