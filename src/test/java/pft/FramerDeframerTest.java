package pft;


import static org.junit.Assert.assertEquals;

import org.junit.Test;
import pft.frames.ChecksumRequest;
import pft.frames.ChecksumResponse;
import pft.frames.DataRequest;
import pft.frames.DataResponse;
import pft.frames.DownloadRequest;
import pft.frames.DownloadResponse;
import pft.frames.Status;
import pft.frames.TerminationRequest;
import pft.frames.UploadRequest;
import pft.frames.UploadResponse;

import java.util.Random;

public class FramerDeframerTest {

  @Test
  public void frameDeframe() {

    Framer framer = new Framer();
    Deframer deframer = new Deframer();

    Random rand = new Random();
    byte[] sha1 = new byte[20];

    {
      rand.nextBytes(sha1);
      DownloadRequest request = new DownloadRequest("my_file.docx", sha1);
      assertEquals(request, deframer.deframe(framer.frame(request)));
    }

    {
      rand.nextBytes(sha1);
      DownloadResponse response = new DownloadResponse(rand.nextInt(), Status.OK, 8913, 123123, sha1);
      assertEquals(response, deframer.deframe(framer.frame(response)));
    }

    {
      rand.nextBytes(sha1);
      UploadRequest request = new UploadRequest("hello_world.jpeg", 4096, sha1);
      assertEquals(request, deframer.deframe(framer.frame(request)));
    }

    {
      rand.nextBytes(sha1);
      UploadResponse response = new UploadResponse(rand.nextInt(), Status.ERROR, 9781);
      assertEquals(response, deframer.deframe(framer.frame(response)));
    }

    {
      rand.nextBytes(sha1);
      DataRequest request = new DataRequest(rand.nextInt(), 19121, 4096);
      assertEquals(request, deframer.deframe(framer.frame(request)));
    }

    {
      rand.nextBytes(sha1);
      ChecksumResponse response = new ChecksumResponse(rand.nextInt(), 1230, 3901, sha1);
      assertEquals(response, deframer.deframe(framer.frame(response)));
    }

    {
      ChecksumRequest response = new ChecksumRequest(rand.nextInt(), 0, 32212301);
      assertEquals(response, deframer.deframe(framer.frame(response)));
    }

    {
      rand.nextBytes(sha1);
      byte[] data = new byte[3901];
      rand.nextBytes(data);
      DataResponse response = new DataResponse(rand.nextInt(), 1230, 3901, data);
      assertEquals(response, deframer.deframe(framer.frame(response)));
    }

    {
      TerminationRequest request = new TerminationRequest(rand.nextInt(), Status.OK);
      assertEquals(request, deframer.deframe(framer.frame(request)));
    }
  }
}
