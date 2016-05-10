#include <stdio.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <arpa/inet.h>
#include <stdlib.h>
struct packet{
  int bytes;
  char buffer[512];
};
int main(int argc, char *argv[]){
  if ( argc != 2 ) /* argc should be 2 for correct execution */
    {
        /* We print argv[0] assuming it is the program name */
        printf( "usage: %s filename", argv[0] );
        exit(1);
    }
  int clientSocket, portNum, nBytes, rBytes;
  char buffer[512];
  char buffer1[512];
  struct sockaddr_in serverAddr;
  packet p;
  int count = 1;
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
  fptr = fopen(argv[1], "rb");
  if(fptr == 0) {
      printf("Couldnot open the file\n");
      exit(1);
  }
  while(!feof(fptr)){
//    printf("Type a sentence to send to server:\n");
 //   fgets(buffer,1024,stdin);
  //  printf("You typed: %s",buffer);
    //memset(buffer, 0, 512);
      memset(&p, 0, sizeof(struct packet));    
      p.bytes = ++count;
//    nBytes = strlen(buffer) + 1;
    nBytes = fread(p.buffer, 1, sizeof p.buffer, fptr);  
    printf("Bytes read in the buffer is: %d\n", nBytes);
    /*Send message to server*/
    sendto(clientSocket,&p ,nBytes + sizeof p.bytes,0,(struct sockaddr *)&serverAddr,addr_size);

    //sendto(clientSocket,buffer,nBytes,0,(struct sockaddr *)&serverAddr,addr_size);
	//sendto(clientSocket,"OKOKOK",6,0,(struct sockaddr *)&serverAddr,addr_size);
    /*Receive message from server*/
     rBytes = recvfrom(clientSocket,buffer1,512,0,NULL, NULL);

    printf("Received from server: %s\n",buffer1);

  }
  //Send the EOF indication to the server
	printf("File sent; Sending EOF\n");
  
  memset(&p, 0, sizeof(struct packet));    
  p.bytes = -1;
  strcpy(p.buffer, "OKOKOK");
  sendto(clientSocket,&p ,6 + sizeof p.bytes,0,(struct sockaddr *)&serverAddr,addr_size);
  return 0;
}
