package pft.client_manager;

/**
 * Created by anjum parvez ali on 5/15/16.
 */
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import io.netty.util.concurrent.*;
import io.netty.util.internal.SystemPropertyUtil;
import pft.Deframer;
import pft.Framer;
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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.Future;

public class ClientManager implements Runnable {

    public final DatagramSocket sock;
    public final InetAddress clinetAddress;
    public final int clientPort;
    public final int identifier;
    public final boolean isDownloadOperation; //if false then allow upload

    private final IFileFacade fileManager;
    private final IFileFacade pftFileManager;

    private final Framer framer; /*will having a static framer and deframer be a bottleneck??*/
    private final Deframer deframer;
    private final int defaultPacketSize = 4096;
    private volatile List<DataRequest> pendingPackets;
    private volatile LinkedBlockingQueue<Frame> incomingFrames;
    private volatile long currentOffset;

    public ClientManager(InetAddress address, int clientPort, int identifier, Frame requestFrame, boolean isDownload) throws java.net.SocketException
    {
        this.clinetAddress = address;
        this.clientPort = clientPort;
        this.framer = new Framer();
        this.deframer = new Deframer();

        this.sock = new DatagramSocket();
        this.identifier = identifier;
        this.isDownloadOperation = isDownload;

        String fileName = getFileName(requestFrame);
        fileManager = new PftFileManager(fileName);
        pftFileManager = new PftFileManager(fileName + ".pft");

    }
    public void run()
    {
        System.out.println("Executed thread for listening to client IP: "+ clinetAddress.toString()+ " Port: "+clientPort + "Request for "+ fileManager.getFileName());
        ExecutorService pool =  Executors.newFixedThreadPool(20);
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


            final ExecutorService executor = Executors.newFixedThreadPool(4);
            pendingPackets = Collections.synchronizedList(new ArrayList<DataRequest>());
            incomingFrames= new LinkedBlockingQueue<Frame>(64);

            Future receivePacketFuture = executor.submit(new Runnable() {
                public void run() {
                    byte[] packetbuffer = new byte[8196]; //check what happens if datagram is larger than 8196
                    DatagramPacket packet = new DatagramPacket(packetbuffer, packetbuffer.length);
                    for(;;)
                    {
                        try
                        {
                            sock.receive(packet);
                            int length = packet.getLength();
                            byte[] data = Arrays.copyOf(packet.getData(), length);
                            Frame f = deframer.deframe(data);
                            if(f.identifier() == identifier)
                                incomingFrames.offer(f, 20, TimeUnit.MILLISECONDS);

                        }catch (IOException ex)
                        {
                            ex.printStackTrace();
                        }
                        catch (InterruptedException iex)
                        {
                            iex.printStackTrace();
                        }
                    }
                }
            });

            Future sendRequestFuture = executor.submit(new Runnable() {
                public void run() {
                    /*check if partial file exist*/
                    if(pftFileManager.fileExits())
                    {
                        //TODO
                        currentOffset = 0; //find the actual offset
                    }
                    else {
                        currentOffset = 0;
                    }
                    long filesize = fileManager.getSize();
                    for(;;) //this forloop keep sending requests till termination is received for file size is reached
                    {
                        if(currentOffset< filesize)
                        {
                            if(pendingPackets.size() < 32)
                            {
                                boolean isLessThanDefaultSize = ((filesize - currentOffset) / defaultPacketSize) == 0 ? true:false;
                                if(isLessThanDefaultSize)
                                {
                                    DataRequest request = new DataRequest(identifier, currentOffset, (filesize - currentOffset));
                                    currentOffset = filesize;
                                    System.out.println("Send Request for last packet");
                                    //add to pending packet dqueue
                                    pendingPackets.add(request);
                                }
                                else
                                {
                                    int remainingWindow = 32 - pendingPackets.size();
                                    long remainingPackets = ((filesize - currentOffset) / defaultPacketSize) >=4 ? 4 : ((filesize - currentOffset) / defaultPacketSize);

                                    long packetsToSend = remainingWindow>remainingPackets ? remainingPackets:remainingWindow;
                                    DataRequest request = new DataRequest(identifier, currentOffset, 4*packetsToSend);
                                    //create required number of packet request for later request
                                    for (int i = 0; i < packetsToSend; i++) {
                                        DataRequest request1 = new DataRequest(identifier, currentOffset, defaultPacketSize);
                                        pendingPackets.add(request1);
                                        currentOffset += defaultPacketSize;
                                    }
                                }
                            }
                            else
                            {
                                /*wait till few of the pending requests have been serviced*/
                                try
                                {
                                    Thread.sleep(20);
                                }
                                catch (InterruptedException inex) {
                                    inex.printStackTrace();
                                }
                            }
                        }
                        else {
                            /*last packet was sent*/
                            break;
                        }
                    }
                }
            });

            Future processPacketFuture = executor.submit(new Runnable() {
                public void run() {
                    System.out.println("Start Thread to process incoming packets");
                    long packetReceivedAt = System.currentTimeMillis();
                    byte[] packetbuffer = new byte[8196]; //check what happens if datagram is larger tha 512
                    DatagramPacket packet = new DatagramPacket(packetbuffer, packetbuffer.length);
                    for(;;)
                    {
                        try
                        {
                            Frame f = incomingFrames.poll();
                            if(null == f)
                            {
                                if(((System.currentTimeMillis() - packetReceivedAt)) > 10000)
                                {
                                    System.out.print("No packets received in 10sec. Stopping packet processing thread");
                                    break;
                                }
                                else
                                {
                                    Thread.sleep(20);
                                    continue;
                                }
                            }
                    /*packet must be termination or Data Response*/
                            if(f instanceof DataResponse)
                            {
                                DataResponse response = (DataResponse)f;
                                if(response.identifier() == identifier)
                                {
                                    //write to file
                                    long writePosition = fileManager.writeFromPosition(response.offset(), response.length(), response.data());
                                    System.out.println("Identifier : "+identifier+" Data Response received for "+response.offset());
                                    if(writePosition == (response.offset() + response.data().length)) /*data was successfully written*/
                                    {
                                        System.out.println("Identifier : "+identifier+" bytes were written from offset "+response.offset());

                                        //update pft
                                        //TODO
                                        //remove Data request from resend
                                        Iterator<DataRequest> i = pendingPackets.iterator();
                                        while (i.hasNext())
                                        {
                                            DataRequest req = i.next();
                                            if(req.offset() == response.offset())
                                                pendingPackets.remove(req);
                                        }
                                    }
                                }
                                else
                                {
                                    System.out.println("Data Response with incorrect identifier was received. "+"Expected "+identifier+". "+"Received "+ response.identifier());
                                }
                            }
                            else if(f instanceof TerminationRequest)
                            {
                                //write remaining packets to the file system and close this thread
                                TerminationRequest terminationRequest = (TerminationRequest) f;
                                if(terminationRequest.identifier() == identifier)
                                {
                                    //Notify other threads to shutdown
                                    //TODO
                                    executor.shutdownNow();
                                }
                                else
                                {
                                    System.out.println("Termination Request with incorrect identifier was received. "+"Expected "+identifier+". "+"Received "+ terminationRequest.identifier());
                                }
                            }
                        }
                        catch (InterruptedException iex)
                        {
                            iex.printStackTrace();
                        }
                    }
                }
            });

            Future resendRequestFuture = executor.submit(new Runnable() {
                public void run() {
                    DatagramPacket packet;
                    byte[] responseBuffer;
                    long filesize = fileManager.getSize();
                    for(;;)
                    {
                        if(pendingPackets.size() == 0 && (currentOffset == filesize))
                        {
                            System.out.println("No more packets to resend. Will shut down thread");
                            break;
                        }
                        else
                        {
                            for (DataRequest resendPack: pendingPackets) {
                                try
                                {
                                    responseBuffer = framer.frame(resendPack);
                                    packet = new DatagramPacket(responseBuffer, responseBuffer.length, clinetAddress, clientPort);
                                    sock.send(packet);
                                    System.out.println("Data Request resend for offset: "+resendPack.offset());
                                }
                                catch (IOException ex)
                                {
                                    ex.printStackTrace();
                                }
                            }

                        }

                        try
                        {
                            Thread.sleep(60);
                        }
                        catch (InterruptedException iex)
                        {
                            iex.printStackTrace();
                        }
                    }
                }
            });

            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS); //enough time
            }catch(InterruptedException iex)
            {
                System.out.println("Exception Awaiting termination of task. Identifier : "+ identifier +". Process will end now");
            }
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

    /*private void Send()
    {
        int currentOffSet= 0;
        int filesize = 204800;
        List<Object> pendingPackets = Collections.synchronizedList(new ArrayList<Object>());
        for(;;) //this forloop keep sending requests till termination is received for file size is reached
        {
            if(currentOffSet< filesize)
            {
                if(pendingPackets.size() < 32)
                {
                    boolean isLessThanDefaultSize = ((filesize - currentOffSet) / defaultPacketSize) == 0 ? true:false;
                    if(isLessThanDefaultSize)
                    {
                        DataRequest request = new DataRequest(this.identifier, currentOffSet, (filesize - currentOffSet));

                        //add to pending packet dqueue
                        pendingPackets.add(request);
                    }
                    else
                    {
                        int remainingWindow = 32 - pendingPackets.size();
                        int remainingPackets = ((filesize - currentOffSet) / defaultPacketSize) >=4 ? 4 : ((filesize - currentOffSet) / defaultPacketSize);

                        int packetsToSend = remainingWindow>remainingPackets ? remainingPackets:remainingWindow;
                        DataRequest request = new DataRequest(this.identifier, currentOffSet, 4*packetsToSend);
                        //create required number of packet request for later request
                        for (int i = 0; i < packetsToSend; i++) {
                            DataRequest request1 = new DataRequest(this.identifier, currentOffSet, defaultPacketSize);
                            pendingPackets.add(request1);
                            currentOffSet += defaultPacketSize;
                        }
                    }
                }
                else
                {
                    /*wait till few of the pending requests have been serviced*/
                    /*try
                    {
                        Thread.sleep(20);
                    }
                    catch (InterruptedException inex) {
                        inex.printStackTrace();
                    }
                }
            }
            else {
                /*last packet was sent*/
                /*break;
            }
        }
    }*/

    /**private void Resend()
    {
        int currentOffSet= 0;
        int filesize = 204800;
        DatagramPacket packet;
        byte[] responseBuffer;
        List<DataRequest> pendingPackets = Collections.synchronizedList(new ArrayList<DataRequest>());
        for(;;)
        {
            if(pendingPackets.size() == 0 && (currentOffSet == filesize))
            {
                break;
            }
            else
            {
                for (DataRequest resendPack: pendingPackets) {
                    try
                    {
                        responseBuffer = framer.frame(resendPack);
                        packet = new DatagramPacket(responseBuffer, responseBuffer.length, this.clinetAddress, this.clientPort);
                        this.sock.send(packet);
                    }
                    catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                }

            }

            try
            {
                Thread.sleep(60);
            }
            catch (InterruptedException iex)
            {
                iex.printStackTrace();
            }
        }
    }*/

    /*private void ReceivePackets()
    {
        LinkedBlockingQueue<Frame> incomingFrames= new LinkedBlockingQueue<Frame>(64);
        byte[] packetbuffer = new byte[8196]; //check what happens if datagram is larger tha 512
        DatagramPacket packet = new DatagramPacket(packetbuffer, packetbuffer.length);
        for(;;)
        {
            try
            {
                this.sock.receive(packet);
                int length = packet.getLength();
                byte[] data = Arrays.copyOf(packet.getData(), length);
                Frame f = this.deframer.deframe(data);
                if(f.identifier() == this.identifier)
                    incomingFrames.offer(f, 20, TimeUnit.MILLISECONDS);

            }catch (IOException ex)
            {
                ex.printStackTrace();
            }
            catch (InterruptedException iex)
            {
                iex.printStackTrace();
            }
        }
    }*/
    /*private void ProcessPackets()
    {
        List<DataRequest> pendingPackets = Collections.synchronizedList(new ArrayList<DataRequest>());
        LinkedBlockingQueue<Frame> incomingFrames= new LinkedBlockingQueue<Frame>(64);
        byte[] packetbuffer = new byte[8196]; //check what happens if datagram is larger tha 512
        DatagramPacket packet = new DatagramPacket(packetbuffer, packetbuffer.length);
        for(;;)
        {
            try
            {
                Frame f = incomingFrames.poll();
                if(null == f)
                    Thread.sleep(10);
                    /*packet must be termination or Data Response*/
                /*if(f instanceof DataResponse)
                {
                    DataResponse response = (DataResponse)f;
                    if(response.identifier() == this.identifier)
                    {
                        //write to file
                        long writePosition = fileManager.writeFromPosition(response.offset(), response.length(), response.data());
                        System.out.println("Identifier : "+this.identifier+" Data Response received for "+response.offset());
                        if(writePosition == (response.offset() + response.data().length)) /*data was successfully written*/
                       /* {
                            System.out.println("Identifier : "+this.identifier+" bytes were written from offset "+response.offset());

                            //update pft
                            //TODO
                            //remove Data request from resend
                            Iterator<DataRequest> i = pendingPackets.iterator();
                            while (i.hasNext())
                            {
                                DataRequest req = i.next();
                                if(req.offset() == response.offset())
                                    pendingPackets.remove(req);
                            }
                        }
                    }
                    else
                    {
                        System.out.println("Data Response with incorrect identifier was received. "+"Expected "+this.identifier+". "+"Received "+ response.identifier());
                    }
                }
                else if(f instanceof TerminationRequest)
                {
                    //write remaining packets to the file system and close this thread
                    TerminationRequest terminationRequest = (TerminationRequest) f;
                    if(terminationRequest.identifier() == this.identifier)
                    {
                        //Notify other threads to shutdown
                        //TODO
                    }
                    else
                    {
                        System.out.println("Termination Request with incorrect identifier was received. "+"Expected "+this.identifier+". "+"Received "+ response.identifier());
                    }
                }
            }catch (IOException ex)
            {
                ex.printStackTrace();
            }
            catch (InterruptedException iex)
            {
                iex.printStackTrace();
            }
        }
    }*/
}
