#include <stdio.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>
#include <arpa/inet.h>

struct packet{
  int bytes;
  char buffer[512];
};
int main(){
  int udpSocket, nBytes, wBytes;
  packet p;
  char buffer[512];
  struct sockaddr_in serverAddr, clientAddr;
  struct sockaddr_storage serverStorage;
  socklen_t addr_size, client_addr_size;
  int i;
  FILE *fptr;
int con;
//char comp[] = "OKOKOK";
  /*Create UDP socket*/
  udpSocket = socket(PF_INET, SOCK_DGRAM, 0);

  /*Configure settings in address struct*/
  serverAddr.sin_family = AF_INET;
  serverAddr.sin_port = htons(7891);
  serverAddr.sin_addr.s_addr = inet_addr("127.0.0.1");
  memset(serverAddr.sin_zero, '\0', sizeof serverAddr.sin_zero);  
  int count = 0;
  /*Bind socket with address struct*/
  bind(udpSocket, (struct sockaddr *) &serverAddr, sizeof(serverAddr));

  /*Initialize size variable to be used later on*/
  addr_size = sizeof serverStorage;
  fptr = fopen("temp.mp3", "ab");
  while(1){
    /* Try to receive any incoming UDP datagram. Address and port of 
      requesting client will be stored on serverStorage variable */
  memset(&p, 0, sizeof(struct packet));    
	memset(buffer, 0, 512);    
  nBytes = recvfrom(udpSocket,&p,sizeof (struct packet),0,(struct sockaddr *)&serverStorage, &addr_size);

	//nBytes = recvfrom(udpSocket,buffer,512,0,(struct sockaddr *)&serverStorage, &addr_size);
    printf("THe value received is %s\n",p.buffer); 
	//con = strlen(p.buffer);
	if(p.bytes == -1) {
  // && p.buffer[0] == 'O'&& p.buffer[1] == 'K'&& p.buffer[2] == 'O'&& p.buffer[3] == 'K'&& p.buffer[4] == 'O'&& p.buffer[5] == 'K'){
		printf("End Condition. Closing and saving the file\n");
		fclose(fptr);
		break;
	}
	else {
		printf("Still Continuing\n");
		count++;		
		wBytes = fwrite(p.buffer, 1, nBytes - sizeof p.bytes, fptr);
		sendto(udpSocket,"GOT",3,0,(struct sockaddr *)&serverStorage,addr_size);
		printf("Count=%d\n", count);
		printf("Bytes=%d\n", wBytes);
		//printf("Con=%d\n", con);
		//if(count == 41057) {
	//		printf("Done now\n");			
//			fclose(fptr);
//		}
	}
	
    
    
    /*Convert message received to uppercase*/
  //  for(i=0;i<nBytes-1;i++)
    //  buffer[i] = toupper(buffer[i]);

    /*Send uppercase message back to client, using serverStorage as the address*/

  }

  return 0;
}
