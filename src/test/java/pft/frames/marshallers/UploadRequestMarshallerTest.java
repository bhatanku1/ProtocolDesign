package pft.frames.marshallers;

import static org.junit.Assert.assertEquals;

import pft.frames.UploadRequest;
import pft.frames.marshallers.UploadRequestMarshaller;
import org.junit.Test;

import java.util.Random;

public class UploadRequestMarshallerTest {
  @Test
  public void encodeShouldEqualDecode() {

    Random rand = new Random();
    byte[] sha1 = new byte[20];

    {
      rand.nextBytes(sha1);

      UploadRequest request =
          new UploadRequest("hello_world.pdf", 23424123, sha1);
      UploadRequestMarshaller marshaller = new UploadRequestMarshaller();
      assertEquals(request, marshaller.decode(0, marshaller.encode(request)));
    }

    {
      rand.nextBytes(sha1);

      UploadRequest request =
          new UploadRequest("hello_world_asdfadsf_123123--asdfa-&&.cpp", 1, sha1);
      UploadRequestMarshaller marshaller = new UploadRequestMarshaller();
      assertEquals(request, marshaller.decode(0, marshaller.encode(request)));
    }
  }
}
