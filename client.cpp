/*
Ankur Bhatia
Msc Student
Technical University of Munich
bhatia.ankur8@gmail.com */

#include <stdio.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include "helper.h"
#include "packet.h"



int main(int argc, char *argv[]){
  if ( argc != 2 ) /* argc should be 2 for correct execution */
    {
        /* We print argv[0] assuming it is the program name */
        printf( "usage: %s filename\n", argv[0] );
        exit(1);
    }
    int rBytes; //Bytes received from the server
    int fBytes; //Bztes read from file
    int index = 0;
    /*Initialize the socket Parameters and create the UDP socket*/
  int socketfd;
  struct sockaddr_in serverAddr;
    socklen_t addr_size;
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = htons(7899);
    serverAddr.sin_addr.s_addr = inet_addr("127.0.0.1");
    memset(serverAddr.sin_zero, '\0', sizeof serverAddr.sin_zero); 
    addr_size = sizeof serverAddr;
    socketfd = socket(PF_INET, SOCK_DGRAM, 0);
    if(socketfd == -1) {
      printf("Error creating socket");
      exit(1);
    }

    
    /*Check if the file to be uploaded is present in the client. Else throw an error and exit.*/
    FILE *fptr;
    fptr = fopen(argv[1], "rb");
    if(fptr == 0) {
      printf("Couldnot open the file\n");
      exit(1);
    }
    /*create a variable for uploadStatus to get reply from the server*/
    uploadStatus uploadstatus;
    /* Create the updateRequest Packet*/
    uploadRequest uploadrequest;
    uploadrequest.type = 1;
    uploadrequest.filesize = fileSize(argv[1]);
    strcpy(uploadrequest.filename, argv[1]);
    strcpy(uploadrequest.md5sum, "AAAAAAAAAAAAAAAAAA");

    printf("Sending UploadRequest to the server. Waiting for reply...\n");
    printf("Filename: %s\n", uploadrequest.filename);
    printf("Filesize: %d\n", uploadrequest.filesize);

    /*Send an UpdateRequest Packet.This contains the filename, filesize and the MD5 sum of the
    file. The client then waits for n seconds for the reply from the server.*/

    sendto(socketfd,&uploadrequest ,sizeof (struct uploadRequest),0,(struct sockaddr *)&serverAddr,addr_size);
    /*Receive the uploadStatus from the server.
    If the upload status is uploadAccept, check the offset and start the upload*/

    rBytes = recvfrom(socketfd,&uploadstatus,sizeof (struct uploadStatus),0,NULL, NULL);
    printf("Received packets from Server");
    printf("MessageType: %d\n", uploadstatus.type);
    printf("Messageoffset: %d\n", uploadstatus.offset);
    printf("Responsecode: %d\n", uploadstatus.responsecode);
    printf("Bytes received: %d\n", rBytes);

    /*If the uploadstatus is 1 start the upload depending upon the offset.
    The offset 0 means starts from the beginning*/

    dataPacket datapacket;
    memset(&datapacket, 0, sizeof(struct dataPacket));    
    while(!feof(fptr)){
        memset(&datapacket, 0, sizeof(struct dataPacket));    
        fBytes = fread(datapacket.data, 1, sizeof datapacket.data, fptr);  
        printf("Number of bytes> %d\n", fBytes);
        datapacket.size = fBytes;
        datapacket.offset = index;
        sendto(socketfd,&datapacket ,fBytes + 8,0,(struct sockaddr *)&serverAddr,addr_size);
        index++;
        if(index % 9 == 0) {
            rBytes = recvfrom(socketfd,&uploadstatus,sizeof (struct uploadStatus),0,NULL, NULL);
        }
        
    }




    return 0;
 }
