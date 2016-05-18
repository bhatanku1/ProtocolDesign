package pft;

import pft.file_operation.IFileFacade;
import pft.file_operation.PftFileManager;
import pft.frames.*;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.System.exit;

/**
 * Created by ankur on 15.05.2016.
 */
public class Client {
    private byte[] payload;
    private IFileFacade fileManager;
    private IFileFacade pftFileManager;
    private byte[] sha1;
    private UploadRequest uploadRequest;
    private DownloadRequest downloadRequest;
    private String command;
    private String fileName;
    private DatagramSocket datagramSocket = null;
    private InetAddress serverAddress = null;
    private String client_path;
    private String serverIp;
    private Deframer deframer;
    private Framer framer;
    private volatile int currentOffset;
    private int fileSize;
    private int identifier;
    private int serverPort;
    private int dataPortServer;

    private final int defaultPacketSize = 4096;
    private volatile List<DataRequest> pendingPackets;
    private volatile LinkedBlockingQueue<Frame> incomingFrames;

    /*Assume client has a dedicated folder specified in config.properties*/
    private  void loadClientRoot()
    {
        File configFile = new File("config.properties");

        try {
            FileReader reader = new FileReader(configFile);
            Properties props = new Properties();
            props.load(reader);
            client_path= props.getProperty("pathClient");
            reader.close();

        } catch (FileNotFoundException ex) {
            System.out.print("Server root folder not found");
        } catch (IOException ex) {
            System.out.print("Server root folder not found");
        }
    }
    public Client(String command, String fileName, String server_ip, int port) {
        serverIp = server_ip;
        serverPort = port;
        this.command = command;
        loadClientRoot();
        this.fileName = client_path+fileName;
        fileManager = new PftFileManager(this.fileName);
        pftFileManager = new PftFileManager(this.fileName+".pft");

        framer = new Framer();
        Frame frame = null;
        deframer = new Deframer();

        if (command.equals("upload")) {
            if(fileManager.fileExits() == false) {
                System.out.println("File does not exists");
                return;
            }
            fileSize = (int)fileManager.getSize();
            sha1 = fileManager.getHash("SHA-1", 0, (int)fileSize );

            System.out.println("Size of the file: " + fileSize);
            System.out.println("Hash of the file: " + sha1);
            //Get the Size and SHA1 of the filename and create constructor using it
            uploadRequest = new UploadRequest(fileName, fileSize, sha1 );
            payload = framer.frame(uploadRequest);
            System.out.println("UploadRequest created: " + uploadRequest.filename() + " " + uploadRequest.sha1() + " " + uploadRequest.type() + " " + uploadRequest.size() + " " + uploadRequest.identifier());
        }

        else {
            /*check if file.pft exists and then if file exists*/
            byte[] initialRequestSha;
            if(pftFileManager.fileExits() && !fileManager.fileExits())
            {
                pftFileManager.deleteFile();
                initialRequestSha = new byte[20];
            }
            else if(pftFileManager.fileExits() )
            {
                initialRequestSha = readHashFromPftFile();
            }
            else
            {
                initialRequestSha = new byte[20];
            }

            //byte[] bytes = "00000000000000000000".getBytes();

            downloadRequest = new DownloadRequest(fileName,initialRequestSha );
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
            e.printStackTrace();
        }

        try {
            serverAddress = InetAddress.getByName(serverIp);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        DatagramPacket packet = new DatagramPacket(payload, payload.length, serverAddress, serverPort);
        DatagramPacket packetResponse = new DatagramPacket(response, response.length);
        try {
            datagramSocket.send(packet);
            System.out.println("Length of the packet: "+ packet.getLength());
            datagramSocket.receive(packetResponse);
            int length = packetResponse.getLength();
            byte[] dataBuffer = Arrays.copyOf(packetResponse.getData(), length);
            frame = deframer.deframe(dataBuffer);
            System.out.println("Response from the server: " + frame.type());

            if(command.equals("upload")) {
                //Start upload process
                if(frame.type() == 4 )
                {
                    if(((UploadResponse) frame).status() == Status.OK)
                    {
                        System.out.println("Upload Request Accepted");
                        dataPortServer = ((UploadResponse) frame).port();
                        identifier = frame.identifier();
                        UploadProcess(frame);
                    }
                    else
                    {
                        System.out.println("Upload rejected");
                        exit(1);
                    }
                }
                else
                {
                    System.out.println("Expected Upload response. But got another type");
                    exit(1);
                }

            }
            else if(command.equals("download") && frame.type() == 2) {
                if(((DownloadResponse) frame).status() == Status.HASH_NOT_EQUAL)
                {
                    dataPortServer = ((DownloadResponse) frame).port();
                    currentOffset = 0;
                    fileManager.deleteFile();

                }else  if(((DownloadResponse) frame).status() == Status.OK)
                {
                    dataPortServer = ((DownloadResponse) frame).port();
                    if(pftFileManager.fileExits())
                    {
                        currentOffset = readOffsetFromPftFile();
                    }
                    else
                    {
                        currentOffset = 0;
                    }

                }
                else
                {
                    System.out.print("Server rejected download");
                    exit(1);
                }
                byte[] offset = ByteBuffer.allocate(4).putInt(currentOffset).array();
                writeOffsetInPftFile(offset);
                writeHashInPftFile(((DownloadResponse) frame).sha1());
                byte[] size = ByteBuffer.allocate(8).putLong(((DownloadResponse) frame).size()).array();
                writeSizeInPftFile(size);
                //Start Download process
                identifier = frame.identifier();
                fileSize = (int)((DownloadResponse) frame).size();
                System.out.println("Download Request Accepted");
                DownloadProcess();
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

        /*int port = ((UploadResponse) frame).port();
        System.out.println("Port received"+port);
        int identifier = ((UploadResponse)frame).identifier();
        System.out.println("Identifier "+identifier);*/

        DatagramPacket dataRequestPacket = new DatagramPacket(dataRequest, dataRequest.length);
        DatagramPacket dataResponsePacket ;
        System.out.println("Server port for data connection "+dataPortServer);
        long lastValidPacketReceivedAt = System.currentTimeMillis();
        while(true){
            try {
                datagramSocket.setSoTimeout(10000);
                datagramSocket.receive(dataRequestPacket);
                lengthDataRequestPacket = dataRequestPacket.getLength();
                dataRequestPacket.getPort();
                byte[] dataBufferRequest = Arrays.copyOf(dataRequestPacket.getData(), lengthDataRequestPacket);
                frameDataRequest = deframer.deframe(dataBufferRequest);
                if(frameDataRequest.type() == 9) {

                    /*gracefully stop connection*/
                    break;
                }
                else if(frameDataRequest.type() != 5) {
                    if((System.currentTimeMillis() - lastValidPacketReceivedAt) > 10000)
                    {
                        System.out.println("No Valid packets received for 10sec. Closing client");
                        break;
                    }
                }
                lastValidPacketReceivedAt = System.currentTimeMillis();
                offset = ((DataRequest) frameDataRequest).offset();
                lengthDataToBeSend =  ((DataRequest) frameDataRequest).length();
                System.out.println("Offset received: " + offset);
                System.out.println("Length  received: " + lengthDataToBeSend);
                window = lengthDataToBeSend / 4096;
                if(window == 0) {
                    window = 1;
                    readBytes = (int)lengthDataToBeSend;

                }
                for (i = 0; i< window; i++) {
                    dataFromFile = fileManager.readFromPosition((int)(offset),readBytes);
                    dataResponse = new DataResponse(identifier,offset, readBytes, dataFromFile);
                    dataPayload = framer.frame(dataResponse);
                    dataResponsePacket = new DatagramPacket(dataPayload, dataPayload.length, serverAddress, dataPortServer);
                    datagramSocket.send(dataResponsePacket);
                    System.out.println("Packet sent for offset: " + offset+ " To  address "+serverAddress.toString()+"at port "+dataPortServer);
                    offset += 4096;
                }

            }catch (SocketTimeoutException ex)
            {
                ex.printStackTrace();
                break;
            }
            catch (SocketException e) {

                e.printStackTrace();
                break;
            } catch (IOException e) {

                e.printStackTrace();
                break;
            }


        }
        datagramSocket.close();
    }
    public void DownloadProcess() {
        System.out.println("Executed thread for listening to client IP: "+ serverAddress.toString()+ " Port: "+serverPort + "Download at "+ fileManager.getFileName());
        final ExecutorService executor = Executors.newFixedThreadPool(4);
        pendingPackets = Collections.synchronizedList(new ArrayList<DataRequest>());
        incomingFrames= new LinkedBlockingQueue<Frame>(8);

        Future receivePacketFuture = executor.submit(new Runnable() {
            public void run() {
                System.out.println("ReceivePacket thread started");
                byte[] packetbuffer = new byte[8196]; //check what happens if datagram is larger than 8196
                DatagramPacket packet = new DatagramPacket(packetbuffer, packetbuffer.length);
                for(;;)
                {
                    /*if (Thread.interrupted()) {
                        System.out.print("Executor ShutdownNow requested. Shutting down receivePacketFuture");
                        break;
                    }*/
                    try
                    {
                        System.out.println("Waiting for receiveing packet at "+datagramSocket.getLocalPort());
                        datagramSocket.receive(packet);
                        System.out.println("Recevied packet");
                        int length = packet.getLength();
                        byte[] data = Arrays.copyOf(packet.getData(), length);
                        Frame f = deframer.deframe(data);
                        System.out.println("deframe succeded");
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
                        break;
                    }
                }
                System.out.println("For loop ended for receivePacketFuture");
            }
        });

        Future sendRequestFuture = executor.submit(new Runnable() {
            public void run() {
                System.out.println("Send data request Thread started");
                    /*check if partial file exist*/
                DatagramPacket packet;
                byte[] responseBuffer;
                System.out.println("Download request will start at offset "+currentOffset);
                for(;;) //this forloop keep sending requests till termination is received for file size is reached
                {
                    /*if (Thread.interrupted()) {
                        System.out.print("Executor ShutdownNow requested. Shutting down sendRequestFuture");
                        break;
                    }*/
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
                                    packet = new DatagramPacket(responseBuffer, responseBuffer.length, serverAddress, dataPortServer);
                                    datagramSocket.send(packet);
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
                                    packet = new DatagramPacket(responseBuffer, responseBuffer.length, serverAddress, dataPortServer);
                                    datagramSocket.send(packet);
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
                                break;
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
                    /*if (Thread.interrupted()) {
                        System.out.print("Executor ShutdownNow requested. Shutting down processPacketFuture");
                        break;
                    }*/
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
                                                    System.out.print("Current offset is file size. Will stop precess");
                                                        /*send termination request*/
                                                    byte[] terminationBuffer = framer.frame(new TerminationRequest(identifier, Status.OK));
                                                    DatagramPacket pack = new DatagramPacket(terminationBuffer, terminationBuffer.length, serverAddress, dataPortServer);
                                                    datagramSocket.send(pack);
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
                                System.out.println("Received Termination request. Will stop process");
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
                    catch (InterruptedException iex)
                    {
                        iex.printStackTrace();
                        break;
                    }
                    catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                }
                System.out.println("For loop ended for processPAcketFuture");
            }
        });

        Future resendRequestFuture = executor.submit(new Runnable() {
            public void run() {
                System.out.println("Resend packet Thread started");
                DatagramPacket packet;
                byte[] responseBuffer;
                for(;;)
                {
                    /*if (Thread.interrupted()) {
                        System.out.print("Executor ShutdownNow requested. Shutting down resendRequestFuture");
                        break;
                    }*/
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException iex)
                    {
                        iex.printStackTrace();
                        break;
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
                                packet = new DatagramPacket(responseBuffer, responseBuffer.length, serverAddress, dataPortServer);
                                datagramSocket.send(packet);
                                System.out.println("Data Request resend for offset: "+resendPack.offset());
                            }
                            catch (IOException ex)
                            {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
                System.out.println("For loop ended for resendPacketFuture");
            }
        });

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS); //enough time
        }catch(InterruptedException iex)
        {
            System.out.println("Exception Awaiting termination of task. Identifier : "+ identifier +". Process will end now");
        }
        System.out.println("Closing socket . Identifier : "+identifier);
        datagramSocket.close();
        exit(0);
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

