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
import java.nio.ByteBuffer;
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
    private volatile int currentOffset;
    private int fileSize;

    public ClientManager(InetAddress address, int clientPort, int identifier, Frame requestFrame, boolean isDownload) throws java.net.SocketException
    {
        this.clinetAddress = address;
        this.clientPort = clientPort;
        this.framer = new Framer();
        this.deframer = new Deframer();

        this.sock = new DatagramSocket();
        System.out.println("Client Manager socket creation"+ sock.getLocalPort());
        this.identifier = identifier;
        this.isDownloadOperation = isDownload;

        String fileName = getFileName(requestFrame);
        fileSize = getFileSize(requestFrame);
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
            incomingFrames= new LinkedBlockingQueue<Frame>(8);

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
                            {
                                System.out.println("Reveiced packet with correct identifier. Putting into incoming frames");
                                System.out.println("Incoming frame count before"+incomingFrames.size());
                                incomingFrames.offer(f, 20, TimeUnit.MILLISECONDS);
                                System.out.println("Incoming frame count after"+incomingFrames.size());
                            }
                            else
                            {
                                System.out.println("Reveiced packet with incorrect identifier. Putting into incoming frames");
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
                }
            });

            Future sendRequestFuture = executor.submit(new Runnable() {
                public void run() {
                    System.out.println("Send data request Thread started");
                    /*check if partial file exist*/
                    DatagramPacket packet;
                    byte[] responseBuffer;
                    if(pftFileManager.fileExits())
                    {
                        currentOffset = readOffsetFromPftFile(); //find the actual offset
                    }
                    else {
                        currentOffset = 0;
                    }
                    for(;;) //this forloop keep sending requests till termination is received for file size is reached
                    {
                        if(currentOffset< fileSize)
                        {
                            if(pendingPackets.size() == 0)//change this to allow to send multiple requests even when previous requests were not fulfilled
                            {
                                try
                                {
                                    boolean isLessThanDefaultSize = ((fileSize - currentOffset) / defaultPacketSize) == 0 ? true:false;
                                    if(isLessThanDefaultSize)
                                    {
                                        DataRequest request = new DataRequest(identifier, currentOffset, (fileSize - currentOffset));

                                        System.out.println("Send Request for last packet");
                                        responseBuffer = framer.frame(request);
                                        packet = new DatagramPacket(responseBuffer, responseBuffer.length, clinetAddress, clientPort);
                                        sock.send(packet);
                                        currentOffset = fileSize;
                                        //add to pending packet dqueue
                                        pendingPackets.add(request);
                                    }
                                    else
                                    {
                                        int remainingWindow = 4 - pendingPackets.size();
                                        long remainingPackets = ((fileSize - currentOffset) / defaultPacketSize) >=4 ? 4 : ((fileSize - currentOffset) / defaultPacketSize);

                                        long packetsToSend = remainingWindow>remainingPackets ? remainingPackets:remainingWindow;
                                        DataRequest request = new DataRequest(identifier, currentOffset, defaultPacketSize*packetsToSend);
                                        responseBuffer = framer.frame(request);
                                        packet = new DatagramPacket(responseBuffer, responseBuffer.length, clinetAddress, clientPort);
                                        sock.send(packet);
                                        //create required number of packet request for later request
                                        for (int i = 0; i < packetsToSend; i++) {
                                            DataRequest request1 = new DataRequest(identifier, currentOffset, defaultPacketSize);
                                            pendingPackets.add(request1);
                                            currentOffset += defaultPacketSize;
                                        }
                                    }
                                }
                                catch (IOException iex)
                                {
                                    iex.printStackTrace();
                                }

                            }
                            else
                            {
                                /*wait till few of the pending requests have been serviced*/
                                try
                                {
                                    Thread.sleep(100);
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
                        System.out.println(" will process packet now");
                        try
                        {
                            Frame f = incomingFrames.poll();
                            if(null == f)
                            {
                                System.out.println("incomingFrames poll returned null");
                                if(((System.currentTimeMillis() - packetReceivedAt)) > 10000)
                                {
                                    System.out.print("No packets received in 100sec. Stopping socket");
                                    executor.shutdownNow();
                                    break;
                                }
                                else
                                {
                                    Thread.sleep(200);
                                    continue;
                                }
                            }
                            System.out.println("incomingFrames poll returned non null");
                            packetReceivedAt = System.currentTimeMillis();
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

                                        //remove Data request from resend
                                        Iterator<DataRequest> i = pendingPackets.iterator();
                                        while (i.hasNext())
                                        {
                                            DataRequest req = i.next();
                                            if(req.offset() == response.offset())
                                            {System.out.println("Removing packet " + req.offset());
                                                pendingPackets.remove(req);
                                                if(pendingPackets.size() == 0) //since new requests are not sent untill pending packets are acknowledged
                                                {
                                                    byte[] offset = ByteBuffer.allocate(4).putInt(currentOffset).array();
                                                    writeOffsetInPftFile(offset);
                                                    if(currentOffset == fileSize)
                                                    {
                                                        /*send termination request*/
                                                        byte[] terminationBuffer = framer.frame(new TerminationRequest(identifier, Status.OK));
                                                        DatagramPacket pack = new DatagramPacket(terminationBuffer, terminationBuffer.length, clinetAddress, clientPort);
                                                        sock.send(pack);
                                                        /*delete pft file*/
                                                        boolean status = pftFileManager.deleteFile();
                                                        executor.shutdownNow();
                                                    }

                                                }
                                                break;
                                            }

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
                            else
                            {
                                System.out.println("Received packet is neither dataresonse not terminaton");
                            }
                        }
                        catch (Exception iex)
                        {
                            iex.printStackTrace();
                        }
                    }
                }
            });

            Future resendRequestFuture = executor.submit(new Runnable() {
                public void run() {
                    System.out.println("Resend packet Thread started");
                    DatagramPacket packet;
                    byte[] responseBuffer;
                    for(;;)
                    {
                        try
                        {
                            Thread.sleep(1000);
                        }
                        catch (InterruptedException iex)
                        {
                            iex.printStackTrace();
                        }
                        System.out.println("Will Check If packets need to be resent");
                        if(pendingPackets.size() == 0 && (currentOffset == fileSize))
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
        System.out.println("Closing socket . Identifier : "+identifier);
        sock.close();
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
        System.out.println("Identifier"+identifier);
        //check disk space
        //check if file exists by same name. Delete old file and its pft for now
        //check pft
        if(pftFileManager.fileExits())
        {
            byte[] hash = readHashFromPftFile();
            if(!Arrays.equals(request.sha1(), hash))
            {
                System.out.println("Hash from previous partial upload does not match. Deleting old files");
                fileManager.deleteFile();
                byte[] offset = ByteBuffer.allocate(4).putInt(0).array();
                writeOffsetInPftFile(offset);
                writeHashInPftFile(request.sha1());
                byte[] size = ByteBuffer.allocate(8).putLong(request.size()).array();
                writeSizeInPftFile(size);
            }
            UploadResponse response = new UploadResponse(identifier, Status.OK, sock.getLocalPort());
            return response;
        }
        else
        {
            /*if file by same name exists, delete old file for now*/
            if(fileManager.fileExits())
            {
                System.out.println("Delete old file by same name");
                fileManager.deleteFile();
            }
            byte[] offset = ByteBuffer.allocate(4).putInt(0).array();
            writeOffsetInPftFile(offset);
            writeHashInPftFile(request.sha1());
            byte[] size = ByteBuffer.allocate(8).putLong(request.size()).array();
            writeSizeInPftFile(size);

            UploadResponse response = new UploadResponse(identifier, Status.OK, sock.getLocalPort());
            return response;
        }
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

    private int getFileSize(Frame frame)
    {
        if(frame instanceof UploadRequest)
            return (int)((UploadRequest)frame).size();
        else return 0;
    }

    //Assume always sha-1. so hash is 4 bytes
    private void writeHashInPftFile(byte[] hash)
    {
        pftFileManager.writeFromPosition(5, 20, hash);
    }
    private byte[] readHashFromPftFile()
    {
        return pftFileManager.readFromPosition(5, 20);
    }
    private void writeOffsetInPftFile(byte[] offset)
    {
        pftFileManager.writeFromPosition(0, 4, offset);
    }
    private int readOffsetFromPftFile()
    {
        ByteBuffer bb = ByteBuffer.wrap(pftFileManager.readFromPosition(0, 4));
        return bb.getInt();
    }
    private void writeSizeInPftFile(byte[] size)
    {
        pftFileManager.writeFromPosition(26, 8, size);
    }
    private long readSizeFromPftFile()
    {
        ByteBuffer bb = ByteBuffer.wrap(pftFileManager.readFromPosition(26, 8));
        return bb.getLong();
    }

}
