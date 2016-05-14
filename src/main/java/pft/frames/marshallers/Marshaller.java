package pft.frames.marshallers;

public interface Marshaller<FrameT> {

  FrameT decode(int identifier, byte[] data);

  byte[] encode(FrameT frame);

}
