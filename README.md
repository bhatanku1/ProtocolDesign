# Instructions
Both the client and server run from the same file with different command line arguments.
Client:
      Enter the path of the folder you want to upload files from or download files from the server to the  config.properteries file.
      Usage:  
        pft 
         <server> localhost by default
        -t <port> 7000 default
        -f <filePath> Mandatory field
        -m <mode> [upload|download

Eg: pft 127.0.0.1 -m upload -f a_song.mp3
    pft 127.0.0.1 -t 7002-m upload -f a_song.mp3
    prt 127.0.0.1 -m download -f somefile.mp4 (This will try to download the file from the server. The file must be present in the path
    specified by the config.properties file in the server

Server: 
      Enter the path of the folder you want to save the uploaded files from or transfer the files for download to the client in the
      config.properteries file.
      Usage:
      pft
      -s this means server
      -t <port> 7000 default
    
  Eg: pft -s
      
