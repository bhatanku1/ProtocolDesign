package pft.frames.marshallers;

import static org.junit.Assert.assertEquals;

import pft.frames.DataResponse;
import pft.frames.marshallers.DataResponseMarshaller;
import org.junit.Test;

import java.util.Random;

public class DataResponseMarshallerTest {
  @Test
  public void encodeShouldEqualDecode() {

    Random rand = new Random();

    {
      int identifier = rand.nextInt();
      byte[] data = new byte[4096];
      rand.nextBytes(data);
      DataResponse response = new DataResponse(identifier, 0, 4096, data);
      DataResponseMarshaller marshaller = new DataResponseMarshaller();

      assertEquals(response, marshaller.decode(identifier, marshaller.encode(response)));
    }

    {
      int identifier = rand.nextInt();
      byte[] data = new byte[127];
      rand.nextBytes(data);
      DataResponse response = new DataResponse(identifier, 809123, 127, data);
      DataResponseMarshaller marshaller = new DataResponseMarshaller();

      assertEquals(response, marshaller.decode(identifier, marshaller.encode(response)));
    }
  }
}
