package pft.frames.marshallers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import pft.frames.Status;
import pft.frames.TerminationRequest;

import java.util.Random;

public class TerminationRequestMarshallerTest {
  @Test
  public void encodeShouldEqualDecode() {

    Random rand = new Random();

    {
      int identifier = rand.nextInt();

      TerminationRequest request = new TerminationRequest(identifier, Status.OK);
      TerminationRequestMarshaller marshaller = new TerminationRequestMarshaller();

      assertEquals(request, marshaller.decode(identifier, marshaller.encode(request)));
    }
  }
}
