package pft;

/**
 * Created by rabbiddog on 5/15/16.
 */
import pft.client_manager.ClientManager;
import pft.file_operation.IFileFacade;
import pft.file_operation.PftFileManager;
import pft.frames.*;
import pft.*;
import pft.frames.Frame;

import java.awt.*;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.exit;

public class Server implements Runnable {

    private  DatagramSocket datagramSocket;
    private final ExecutorService pool;
    private final Deframer deframer;
    private final Framer framer;
    private Random rand;

    public Server(int port, int poolSize)
    {
        pool = Executors.newFixedThreadPool(poolSize);
        try {
            datagramSocket = new DatagramSocket(port);

        } catch (SocketException e) {
            e.printStackTrace();
            exit(1);
        }


        deframer = new Deframer();
        framer = new Framer();
        rand = new Random();
    }

    public void run() {
    System.out.println("Start Server");
        byte[] packetbuffer = new byte[512]; //check what happens if datagram is larger tha 512
        DatagramPacket packet = new DatagramPacket(packetbuffer, packetbuffer.length);
            for (;;) {
                try {

                    datagramSocket.receive(packet);
                    System.out.println("Received packet");
                    int length = packet.getLength();
                    byte[] data = Arrays.copyOf(packet.getData(), length);
                    Frame f = deframer.deframe(data);
                    if(f instanceof DownloadRequest || f instanceof UploadRequest)
                    {
                        int identifier = rand.nextInt();
                        ClientManager manager ;
                        Frame response;
                        byte[] responseBuffer;
                        if(f instanceof DownloadRequest)
                        {
                            manager = new ClientManager(packet.getAddress(), packet.getPort(), identifier, f, true);
                            response = manager.createDownloadResponse((DownloadRequest)f);
                            if(((DownloadResponse)response).status() == Status.OK || ((DownloadResponse)response).status() == Status.HASH_NOT_EQUAL)
                                pool.execute(manager);
                        /*else
                            manager.dispose;*/
                            responseBuffer = framer.frame(response);
                        }
                        else
                        {
                            manager = new ClientManager(packet.getAddress(), packet.getPort(), identifier, f, false);
                            response = manager.createUploadResponse((UploadRequest) f);
                            if(((UploadResponse)response).status() == Status.OK)
                                pool.execute(manager);
                        /*
                        * else
                        * manager.dispose*/
                            responseBuffer = framer.frame(response);
                        }
                    packet = new DatagramPacket(responseBuffer, responseBuffer.length, packet.getAddress(), packet.getPort());
                    datagramSocket.send(packet);

                    }
                } catch (IOException ex) {
                    System.out.println("Exception occured while receiving client request. Stack : "+ex.getStackTrace()+" Message: " + ex.getMessage());
                }
            }

    }
}
