package pft.frames.marshallers;

import static org.junit.Assert.assertEquals;

import pft.frames.DownloadResponse;
import pft.frames.Status;
import pft.frames.marshallers.DownloadResponseMarshaller;
import org.junit.Test;

import java.util.Random;

public class DownloadResponseMarshallerTest {

  @Test
  public void encodeShouldEqualDecode() {

    Random rand = new Random();
    byte[] sha1 = new byte[20];

    {
      rand.nextBytes(sha1);
      int identifier = rand.nextInt();

      DownloadResponse response =
          new DownloadResponse(identifier, Status.OK, 7891, 0xFFFFFFFFL, sha1);
      DownloadResponseMarshaller marshaller = new DownloadResponseMarshaller();
      assertEquals(response, marshaller.decode(identifier, marshaller.encode(response)));
    }

    {
      rand.nextBytes(sha1);
      int identifier = rand.nextInt();

      DownloadResponse response =
          new DownloadResponse(identifier, Status.ERROR, 65535, 24308234, sha1);
      DownloadResponseMarshaller marshaller = new DownloadResponseMarshaller();
      assertEquals(response, marshaller.decode(identifier, marshaller.encode(response)));
    }
  }

}
