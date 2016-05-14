package main;

import java.io.IOException;

import download.DownloadRequest;
import download.DownloadRequestMarshaller;

public class Client {
	public static void main(String [] args) {
		byte[] bytes = null;
		DownloadRequest downloadRequest = new DownloadRequest();
		DownloadRequestMarshaller downloadRequestMarshaller = new DownloadRequestMarshaller();
		downloadRequest.setFileName("ankur.mp3");
		downloadRequest.setIdentifier("ankurbhatia");
		downloadRequest.setSha("adfahfadhfjahdfj");
		downloadRequest.setSize(5);
		try {
			bytes = downloadRequestMarshaller.encode(downloadRequest);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.print(bytes);
	}

}
