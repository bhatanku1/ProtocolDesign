package pft.frames.marshallers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import pft.frames.ChecksumRequest;

import java.util.Random;

public class ChecksumRequestMarshallerTest {

  @Test
  public void encodeShouldEqualDecode() {

    Random rand = new Random();

    {
      int identifier = rand.nextInt();

      ChecksumRequest request = new ChecksumRequest(identifier, 0, 12345);
      ChecksumRequestMarshaller marshaller = new ChecksumRequestMarshaller();

      assertEquals(request, marshaller.decode(identifier, marshaller.encode(request)));
    }

    {
      int identifier = rand.nextInt();

      ChecksumRequest request = new ChecksumRequest(identifier, 123423, 0xFFFFFFFFL);
      ChecksumRequestMarshaller marshaller = new ChecksumRequestMarshaller();

      assertEquals(request, marshaller.decode(identifier, marshaller.encode(request)));
    }
  }
}
