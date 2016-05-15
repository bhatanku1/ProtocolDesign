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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.exit;

public class Server implements Runnable {

    private  DatagramSocket datagramSocket;
    private final Deframer deframer;
    private final Framer framer;
    private Random rand;

    public Server(int port, int poolSize)
    {
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
        try {
            for (;;) {
                byte[] packetbuffer = new byte[8192];
                DatagramPacket packet = new DatagramPacket(packetbuffer, packetbuffer.length);
                datagramSocket.receive(packet);
                byte[] data = packet.getData();
                Frame f = deframer.deframe(data);
                if(f instanceof DownloadRequest || f instanceof UploadRequest)
                {
                    int identifier = rand.nextInt();
                    ClientManager manager = new ClientManager(packet.getAddress(), packet.getPort(), identifier);
                }
            }
        } catch (IOException ex) {

        }
    }

    private static boolean isByteArrayAllZero(byte[] data)
    {
        for (byte b : data) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }
}
