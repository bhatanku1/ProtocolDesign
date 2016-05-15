package pft.client_manager;

/**
 * Created by anjum parvez ali on 5/15/16.
 */
import pft.Server;
import pft.file_operation.IFileFacade;
import pft.file_operation.PftFileManager;
import pft.frames.DownloadRequest;
import pft.frames.DownloadResponse;
import pft.frames.Frame;
import pft.frames.UploadRequest;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;

public class ClientManager implements Runnable {

    public final DatagramSocket sock;
    public final InetAddress clinetAddress;
    public final int clientPort;
    public final int identifier;

    //private final IFileFacade fileManager;

    public ClientManager(InetAddress address, int clientPort, int identifier) throws java.net.SocketException
    {
        this.clinetAddress = address;
        this.clientPort = clientPort;

        this.sock = new DatagramSocket();
        this.identifier = identifier;
    }
    public void run()
    {
       /* byte[] data = packet.getData();
        Frame f = deframer.deframe(data);

        if(f instanceof DownloadRequest)
        {
            DownloadResponse response;
            //create
            packet = new DatagramPacket(buf, buf.length, address, port);
            IFileFacade fileManager = new PftFileManager(((DownloadRequest) f).filename());
            if(Server.isByteArrayAllZero(((DownloadRequest) f).sha1()))
            {
                if(fileManager.fileExits())
                {
                    response = new DownloadResponse()
                }
            }
            else
            {

            }
            //create clientmanager and a socket for revceiving packets

            datagramSocket.send(packet);
        }
        if (f instanceof UploadRequest)
        {

        }*/
        //else drop packet
    }

    public DownloadResponse createDownloadResponse(DownloadRequest request)
    {
        return null;
    }
}
