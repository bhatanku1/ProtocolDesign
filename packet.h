/*uploadRequest is the packet send when the client wants to initiate an Upload
The client sends the filename, size and the md5sum of the file to be uploaded*/
struct uploadRequest {
	int type;
	char filename[30];
	int filesize;
	char md5sum[32];
};
struct uploadStatus {
	int type;                      //For uploadAccept:
	                               //For uploadDenied:
	int offset;                    //0 if upload is new
	int responsecode;              //incase of a upload Denied
};
 /*Datapackets are sent by the client from the server after it receives the 
 uploadAccept packet from the client*/
struct dataPacket {
	int size;
	int offset;
	char data[512];
};