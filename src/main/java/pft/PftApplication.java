package pft;

import pft.frames.UploadRequest;

import java.io.IOException;

import static java.lang.System.exit;

/**
 * Created by ankur on 15.05.2016.
 */
public class PftApplication {
    private int port = 7000;
    private String mode = null;
    private String filepath = null;
    private String server = "localhost";
    private String command = null;
    public void setCommand(String command){
        this.command = command;
    }

    public int port(){
        return port;
    }
    public String mode() {
        return mode;
    }
    public String filepath() {
        return filepath;
    }
    public String server(){
        return server;
    }
    public String command(){
        return command;
    }
    public String toString(){
        return "Command: " + command() + " port: " + port() + " mode: " +mode() + " Filepath:" + filepath() + " server:" + server();

    }
    public void printUsage() {
        System.out.println("Client:  <server> localhost by default");
        System.out.println("      :  -t <port> 7000 default");
        System.out.println("      :  -f <filePath> Mandatory field");
        System.out.println("      :  -m <mode> [upload|download]");
        System.out.println("Server:  -t <port> 7000 by default");
        System.out.println("      :  -s [Mandatory field]");



    }
    int ParseCommand(String parameter, int index, String [] args){
        switch(parameter) {
            case "-t":
                try{
                    //System.out.println("Paramater: " + parameter + " Index: " + index);
                    port = Integer.parseInt(args[index+1]);
                    return 2;


                }catch(Exception e){
                    //System.out.println("Error in port number");
                    //e.printStackTrace();
                    return 0;
                }
            case "-m" :
                try{
                    if(args[index+1].equals("upload")) {
                        mode = "upload";
                        break;
                    }
                    else if(args[index+1].equals("download")) {
                        mode = "download";
                        break;
                    }
                    else {
                        //System.out.println("Error in -m mode");

                        return 0;
                    }
                }catch(Exception e){
                    return 0;
                }
                //System.out.println("Paramater: " + parameter + " Index: " + index);


            case "-f" :
                //System.out.println("Paramater: " + parameter + " Index: " + index);

                try{
                    filepath = args[index + 1];
                    break;
                }catch(Exception e){
                    //System.out.println("Error in filepath");

                    e.printStackTrace();
                    return 0;
                }
            case "-s" :
                //System.out.println("Paramater: " + parameter + " Index: " + index);

                if(command == null){
                    command = "server";
                    return 1;

                }
                else{
                    //System.out.println("Error in port number");
                    return 0;
                }
            default:
                //System.out.println("Paramater: " + parameter + " Index: " + index);

                if (index != 1 ) {
                    return 0;
                }
                else {
                    command = "client";
                    server = parameter;
                    return 1;
                }
        }
        return 2;
    }

    public static void main(String [] args) {
        PftApplication c = new PftApplication();
        int length = args.length;
        int status;
        boolean isValid = true;
        Client client;
        Server server;
        //System.out.println(length);
        //for(int i =0; i<length; i++) System.out.println(args[i]);
        //Condition 1: Check  the number of arguments
        if(length>9 | length < 2 | !args[0].equals("pft")) {
            c.printUsage();
            //System.out.println("Check the usage: Error in length of the parameters");
            return;

        }
        int parsed = 1;
        while(parsed<length) {
            //System.out.println(args[parsed] + " " + parsed + " " + args[parsed]);
            status = c.ParseCommand(args[parsed], parsed, args);
            if (status == 0) {
                //System.out.println("Check the usage");
                //c.printUsage();
                isValid = false;
                break;
            }
            parsed = parsed + status;
            //System.out.println("Parsed" + parsed);

        }
        if(c.command()== null){
            c.setCommand("client");
        }
        if(c.command().equals("client")) {
            if(c.filepath() == null | c.mode() == null){
                isValid = false;

                //c.printUsage();

                //System.out.println("Check the usage");
            }
        }

        if(isValid == false) {
            c.printUsage();
            exit(1);
        }

        if(c.command().equals("client")){
            //Start the client
            client = new Client(c.mode(), c.filepath(), c.server(), c.port());

        }
        else{
            //Start the Server
            Thread th = new Thread(new Server(c.port()));
            th.start();
            try
            {
                th.join();
            }catch (InterruptedException ex)
            {
                System.out.print("Error in starting server thread");
                exit(1);
            }

        }
        System.out.println(c.toString());

    }
}
