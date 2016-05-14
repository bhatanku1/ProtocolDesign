package pft.frames.marshallers;

import static org.junit.Assert.assertEquals;

import pft.frames.DataRequest;
import org.junit.Test;

import java.util.Random;

public class DataRequestMarshallerTest {

  @Test
  public void encodeShouldEqualDecode() {

    Random rand = new Random();

    {
      int identifier = rand.nextInt();

      DataRequest request = new DataRequest(identifier, 0, 12345);
      DataRequestMarshaller marshaller = new DataRequestMarshaller();

      assertEquals(request, marshaller.decode(identifier, marshaller.encode(request)));
    }

    {
      int identifier = rand.nextInt();

      DataRequest request = new DataRequest(identifier, 123423, 0xFFFFFFFFL);
      DataRequestMarshaller marshaller = new DataRequestMarshaller();

      assertEquals(request, marshaller.decode(identifier, marshaller.encode(request)));
    }
  }

}
