#include <stdio.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <arpa/inet.h>
int main(){
  int clientSocket, portNum, nBytes, rBytes;
  char buffer[512];
  char buffer1[512];
  struct sockaddr_in serverAddr;
  socklen_t addr_size;
  FILE *fptr;
  /*Create UDP socket*/
  clientSocket = socket(PF_INET, SOCK_DGRAM, 0);

  /*Configure settings in address struct*/
  serverAddr.sin_family = AF_INET;
  serverAddr.sin_port = htons(7891);
  serverAddr.sin_addr.s_addr = inet_addr("127.0.0.1");
  memset(serverAddr.sin_zero, '\0', sizeof serverAddr.sin_zero);  

  /*Initialize size variable to be used later on*/
  addr_size = sizeof serverAddr;
  fptr = fopen("ipl.mp4", "rb");
  while(!feof(fptr)){
//    printf("Type a sentence to send to server:\n");
 //   fgets(buffer,1024,stdin);
  //  printf("You typed: %s",buffer);
    memset(buffer, 0, 512);
//    nBytes = strlen(buffer) + 1;
    nBytes = fread(buffer, 1, sizeof buffer, fptr);  
    /*Send message to server*/
    sendto(clientSocket,buffer,nBytes,0,(struct sockaddr *)&serverAddr,addr_size);
	//sendto(clientSocket,"OKOKOK",6,0,(struct sockaddr *)&serverAddr,addr_size);
    /*Receive message from server*/
     rBytes = recvfrom(clientSocket,buffer1,512,0,NULL, NULL);

    printf("Received from server: %s\n",buffer1);

  }
  //Send the EOF indication to the server
	printf("File sent; Sending EOF\n");
   sendto(clientSocket,"OKOKOK",6,0,(struct sockaddr *)&serverAddr,addr_size);
  return 0;
}
