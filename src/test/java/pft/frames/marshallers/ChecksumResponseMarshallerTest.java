package pft.frames.marshallers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import pft.frames.ChecksumResponse;
import pft.frames.DataResponse;

import java.util.Random;

public class ChecksumResponseMarshallerTest {

  @Test
  public void encodeShouldEqualDecode() {

    Random rand = new Random();

    {
      byte[] sha1 = new byte[20];
      rand.nextBytes(sha1);
      int identifier = rand.nextInt();

      ChecksumResponse response = new ChecksumResponse(identifier, 0, 4096, sha1);
      ChecksumResponseMarshaller marshaller = new ChecksumResponseMarshaller();

      assertEquals(response, marshaller.decode(identifier, marshaller.encode(response)));
    }

    {
      byte[] sha1 = new byte[20];
      rand.nextBytes(sha1);
      int identifier = rand.nextInt();

      ChecksumResponse response = new ChecksumResponse(identifier, 70913, 65000, sha1);
      ChecksumResponseMarshaller marshaller = new ChecksumResponseMarshaller();

      assertEquals(response, marshaller.decode(identifier, marshaller.encode(response)));
    }

  }

}
