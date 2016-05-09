#include <stdio.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>
#include <arpa/inet.h>


int main(){
  int udpSocket, nBytes, wBytes;
  char buffer[512];
  struct sockaddr_in serverAddr, clientAddr;
  struct sockaddr_storage serverStorage;
  socklen_t addr_size, client_addr_size;
  int i;
  FILE *fptr;
int con;
char comp[] = "OKOKOK";
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
  fptr = fopen("temp.mp4", "ab");
  while(1){
    /* Try to receive any incoming UDP datagram. Address and port of 
      requesting client will be stored on serverStorage variable */
		memset(buffer, 0, 512);    
	nBytes = recvfrom(udpSocket,buffer,512,0,(struct sockaddr *)&serverStorage, &addr_size);
    printf("THe value received is %s\n",buffer); 
	con = strlen(buffer);
	if((con == 6) && buffer[0] == 'O'&& buffer[1] == 'K'&& buffer[2] == 'O'&& buffer[3] == 'K'&& buffer[4] == 'O'&& buffer[5] == 'K'){
		printf("End Condition. Closing and saving the file\n");
		fclose(fptr);
		break;
	}
	else {
		printf("Still Continuing\n");
		count++;		
		wBytes = fwrite(buffer, 1, nBytes, fptr);
		sendto(udpSocket,"GOT",3,0,(struct sockaddr *)&serverStorage,addr_size);
		printf("Count=%d\n", count);
		printf("Bytes=%d\n", wBytes);
		printf("Con=%d\n", con);
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
