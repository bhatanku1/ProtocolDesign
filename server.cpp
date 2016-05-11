#include <stdio.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>
#include <arpa/inet.h>
#include "packet.h"
int main() {
  int rBytes; // to store the number of bytes received from the server
  int wBytes; // to store the number of Bytes written on the file when received
  int totalBytes = 0; // to store the total Bytes written

  FILE *fptr;
  /*Define socket parameters*/
  int socketfd;
  struct sockaddr_in serverAddr;
    struct sockaddr_storage serverStorage;
    socklen_t addr_size;
    socketfd = socket(PF_INET, SOCK_DGRAM, 0);
    if(socketfd == -1) {
      printf("Could not create the socket\n");
      exit(1);
    }
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = htons(7899);
    serverAddr.sin_addr.s_addr = inet_addr("127.0.0.1");
    memset(serverAddr.sin_zero, '\0', sizeof serverAddr.sin_zero);  
    bind(socketfd, (struct sockaddr *) &serverAddr, sizeof(serverAddr));
    addr_size = sizeof serverStorage;
    printf("Server started. Waiting for clients...\n");
    /*Creat a vaariable of the uploadRequest*/
    uploadRequest uploadrequest;
    uploadStatus uploadstatus;
    dataPacket datapacket;
    memset(&uploadrequest, 0, sizeof(struct uploadRequest));    
    memset(&uploadstatus, 0, sizeof(struct uploadStatus));   
    memset(&datapacket, 0, sizeof(struct dataPacket));    
 
    while(1) {
      rBytes = recvfrom(socketfd,&uploadrequest,sizeof (struct uploadRequest),0,(struct sockaddr *)&serverStorage, &addr_size);
      printf("uploadrequest received from the client\n");
      printf("Filename: %s\n", uploadrequest.filename);
      printf("Filesize: %d\n", uploadrequest.filesize);
      printf("MD5: %s\n", uploadrequest.md5sum);
      printf("type: %d\n", uploadrequest.type); 
      printf("Bytes received: %d\n", rBytes );

      /*Check if the file was uploaded before. If it was already uploaded
      check the index in the file descriptor and send it to the client.
      If the upload is not possible for any reason, send a uploadDenied.*/

      /*For now assume that the check is successful. Send the uploadStatus  
       with accept and proceed with the  download from beginning*/
      uploadstatus.type = 1;
      uploadstatus.offset = 0;
      uploadstatus.responsecode = 0;
      sendto(socketfd,&uploadstatus,sizeof (struct uploadStatus),0,(struct sockaddr *)&serverStorage,addr_size);
      break;

    }

    fptr = fopen("../pft/temp", "wb");
    int i = 0;
    while(1) {
      rBytes = recvfrom(socketfd,&datapacket,sizeof (struct dataPacket),0,(struct sockaddr *)&serverStorage, &addr_size);
      printf("Bytes received from the client is : %d\n", sizeof (datapacket.data));
      wBytes = fwrite(datapacket.data, 1, rBytes - 2*(sizeof (int)) , fptr);
      printf("Bytes written in the file are %d\n", wBytes);
      totalBytes += wBytes;
      if(datapacket.offset % 9 ==0 ) {

        sendto(socketfd,&uploadstatus,sizeof (struct uploadStatus),0,(struct sockaddr *)&serverStorage,addr_size);

      }
      if(totalBytes == uploadrequest.filesize) {
        fclose(fptr);
      }
    }


  return 0;

}