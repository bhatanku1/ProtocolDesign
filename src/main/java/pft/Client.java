package pft;

import pft.frames.DownloadRequest;
import pft.frames.Frame;
import pft.frames.UploadRequest;

/**
 * Created by ankur on 15.05.2016.
 */
public class Client {
    byte[] payload;
    //IFileFacade fileManager;

    public Client(String command, String fileName) {
       //fileManager = new PftFileManager();
        Framer framer = new Framer();
        Frame frame = null;
        Deframer deframer = new Deframer();
        if (command.equals("upload")) {
            //Get the Size and SHA1 of the filename and create constructor using it

            byte[] bytes = "SHAAAAAAAAAAAAAAAAAA".getBytes();
            UploadRequest uploadRequest = new UploadRequest(fileName, 10, bytes );
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

            DownloadRequest downloadRequest = new DownloadRequest(fileName,bytes );
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
    }
    public void ClientUploadDecode(){

    }
   /* ctor()
   creTE csocket

      */
    //fucntion upload(file name)

    //function download(file name)
}
