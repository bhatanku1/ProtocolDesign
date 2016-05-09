#include <string.h>

int fileSize(char *file) {
	FILE *fptr;
	char buffer[1024];
	int size = 0;
	fptr = fopen(file, "rb");
	if(fptr == 0){ // File doesnot exists or could not open
		return -1;
	}
	while(!feof(fptr)) {
		memset(buffer,0,1024);
		size += fread(buffer, 1, sizeof buffer, fptr);
	}	
	fclose(fptr);
	return size;
}

