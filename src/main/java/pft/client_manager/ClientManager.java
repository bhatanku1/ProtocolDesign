package pft.client_manager;

/**
 * Created by anjum parvez ali on 5/15/16.
 */
import pft.Server;
import pft.file_operation.IFileFacade;
import pft.file_operation.OpenFileOperationStatus;
import pft.file_operation.PftFileManager;
import pft.frames.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Random;

public class ClientManager implements Runnable {

    public final DatagramSocket sock;
    public final InetAddress clinetAddress;
    public final int clientPort;
    public final int identifier;
    public final boolean isDownloadOperation; //if false then allow upload

    private final IFileFacade fileManager;


    public ClientManager(InetAddress address, int clientPort, int identifier, Frame requestFrame, boolean isDownload) throws java.net.SocketException
    {
        this.clinetAddress = address;
        this.clientPort = clientPort;

        this.sock = new DatagramSocket();
        this.identifier = identifier;
        this.isDownloadOperation = isDownload;

        fileManager = new PftFileManager(getFileName(requestFrame));
    }
    public void run()
    {
        System.out.println("Executed thread for listening to client IP: "+ clinetAddress.toString()+ " Port: "+clientPort + "Request for "+ fileManager.getFileName());

        if(isDownloadOperation)
        {
            for(;;) {
                byte[] packetbuffer = new byte[32]; //check what happens if datagram is larger tha 512
                DatagramPacket packet = new DatagramPacket(packetbuffer, packetbuffer.length);
                try
                {
                    this.sock.receive(packet);
                }
                catch (IOException ex)
                {
                    System.out.println("Exception occured in client manager");
                }
            }
        }
        else
        {

        }
    }

    public DownloadResponse createDownloadResponse(DownloadRequest request)
    {
        byte[] empty = new byte[20];
        DownloadResponse response;

        if(fileManager.fileExits())
        {
            long fileSize = fileManager.getSize();
            if(fileSize < Math.pow(2, 31))
            {
                if(Arrays.equals(empty, request.sha1()))
                {
                    response = new DownloadResponse(this.identifier, Status.ERROR, 0, 0, empty);
                }
                else if(fileManager.fileMatchDescription(request.sha1(), "SHA-1") == OpenFileOperationStatus.HASH_MATCH)
                {
                    response = new DownloadResponse(this.identifier, Status.OK, this.sock.getPort(), fileSize, fileManager.getHash("SHA-1", 0, (int)fileSize));
                }
                else
                {
                    response = new DownloadResponse(this.identifier, Status.HASH_NOT_EQUAL, 0, 0, empty);
                }
            }
            else {
                response = new DownloadResponse(this.identifier, Status.ERROR, 0, 0, empty);
            }
        }
        else
        {
            response = new DownloadResponse(this.identifier, Status.ERROR, 0, 0, empty);
        }

        return response;
    }

    public UploadResponse createUploadResponse(UploadRequest request)
    {
        return null;
    }

    private String getFileName(Frame frame)
    {
        if(frame instanceof DownloadRequest)
            return ((DownloadRequest)frame).filename();
        else if(frame instanceof  UploadRequest)
            return ((UploadRequest)frame).filename();
        else
            return  null;
    }
}
