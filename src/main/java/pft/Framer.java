package pft;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import pft.frames.ChecksumRequest;
import pft.frames.ChecksumResponse;
import pft.frames.DataRequest;
import pft.frames.DataResponse;
import pft.frames.DownloadRequest;
import pft.frames.DownloadResponse;
import pft.frames.Frame;
import pft.frames.TerminationRequest;
import pft.frames.UploadRequest;
import pft.frames.UploadResponse;
import pft.frames.marshallers.Marshaller;
import pft.frames.marshallers.Marshallers;

public class Framer {

  private static final PooledByteBufAllocator ALLOCATOR =
      PooledByteBufAllocator.DEFAULT;

  public byte[] frame(Frame frame) {

    byte[] payload = null;
    if (frame instanceof DownloadRequest) {
      payload = Marshallers.DOWNLOAD_REQUEST.encode((DownloadRequest) frame);
    } else if (frame instanceof DownloadResponse) {
      payload = Marshallers.DOWNLOAD_RESPONSE.encode((DownloadResponse) frame);
    } else if (frame instanceof UploadRequest) {
      payload = Marshallers.UPLOAD_REQUEST.encode((UploadRequest) frame);
    } else if (frame instanceof UploadResponse) {
      payload = Marshallers.UPLOAD_RESPONSE.encode((UploadResponse) frame);
    } else if (frame instanceof DataRequest) {
      payload = Marshallers.DATA_REQUEST.encode((DataRequest) frame);
    } else if (frame instanceof DataResponse) {
      payload = Marshallers.DATA_RESPONSE.encode((DataResponse) frame);
    } else if (frame instanceof TerminationRequest) {
      payload = Marshallers.TERMINATION_REQUEST.encode((TerminationRequest) frame);
    } else if (frame instanceof ChecksumRequest) {
      payload = Marshallers.CHECKSUM_REQUEST.encode((ChecksumRequest) frame);
    } else if (frame instanceof ChecksumResponse) {
      payload = Marshallers.CHECKSUM_RESPONSE.encode((ChecksumResponse) frame);
    } else {
      throw new UnsupportedOperationException("Frame type not supported");
    }

    int length = 7 + payload.length;
    int identifier = frame.identifier();
    byte type = frame.type();

    ByteBuf buffer = null;
    try {
      buffer = ALLOCATOR.heapBuffer(length, length);
      buffer.writeShort(payload.length);
      buffer.writeByte(type);
      buffer.writeInt(identifier);
      buffer.writeBytes(payload);

      byte[] data = new byte[length];
      buffer.readBytes(data);

      return data;
    } finally {
      if (buffer != null) {
        buffer.release();
      }
    }
  }
}
