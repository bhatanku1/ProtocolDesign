package pft.frames.marshallers;

import static org.junit.Assert.assertEquals;

import pft.frames.Status;
import pft.frames.UploadResponse;
import pft.frames.marshallers.UploadResponseMarshaller;
import org.junit.Test;

import java.util.Random;

public class UploadResponseMarshallerTest {
  @Test
  public void encodeShouldEqualDecode() {

    Random rand = new Random();

    {
      int identifier = rand.nextInt();
      UploadResponse response = new UploadResponse(identifier, Status.OK, 52932);
      UploadResponseMarshaller marshaller = new UploadResponseMarshaller();
      assertEquals(response, marshaller.decode(identifier, marshaller.encode(response)));
    }

    {
      int identifier = rand.nextInt();
      UploadResponse response = new UploadResponse(identifier, Status.ERROR, 1);
      UploadResponseMarshaller marshaller = new UploadResponseMarshaller();
      assertEquals(response, marshaller.decode(identifier, marshaller.encode(response)));
    }
  }
}
