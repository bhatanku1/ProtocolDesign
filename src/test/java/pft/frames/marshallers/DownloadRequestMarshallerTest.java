package pft.frames.marshallers;

import static org.junit.Assert.assertEquals;

import pft.frames.DownloadRequest;
import pft.frames.marshallers.DownloadRequestMarshaller;
import org.junit.Test;

import java.util.Random;

public class DownloadRequestMarshallerTest {

  @Test
  public void encodeShouldEqualDecode() {

    Random rand = new Random();
    byte[] sha1 = new byte[20];

    {
      rand.nextBytes(sha1);
      DownloadRequest request = new DownloadRequest("test.txt", sha1);
      DownloadRequestMarshaller marshaller = new DownloadRequestMarshaller();
      assertEquals(request, marshaller.decode(0, marshaller.encode(request)));
    }

    {
      rand.nextBytes(sha1);
      DownloadRequest request = new DownloadRequest("hello_world_bla_bla_foo_bar.doc", sha1);
      DownloadRequestMarshaller marshaller = new DownloadRequestMarshaller();
      assertEquals(request, marshaller.decode(0, marshaller.encode(request)));
    }
  }

}
