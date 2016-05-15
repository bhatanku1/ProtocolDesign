package pft;

import pft.frames.UploadRequest;

/**
 * Created by ankur on 15.05.2016.
 */
public class PftApplication {
    public static void main(String [] args){
        //If server Start Server
        // If client start client.
        String command = "upload";
        String Filename;
        Client client = new Client(command, "C:\\Users\\ankur\\test.txt");

    }
}
