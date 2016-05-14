package pft.frames.marshallers;

import pft.frames.ChecksumRequest;
import pft.frames.ChecksumResponse;
import pft.frames.DataRequest;
import pft.frames.DataResponse;
import pft.frames.DownloadRequest;
import pft.frames.DownloadResponse;
import pft.frames.TerminationRequest;
import pft.frames.UploadRequest;
import pft.frames.UploadResponse;

public class Marshallers {
  public static final Marshaller<DownloadRequest> DOWNLOAD_REQUEST
      = new DownloadRequestMarshaller();
  public static final Marshaller<DownloadResponse> DOWNLOAD_RESPONSE
      = new DownloadResponseMarshaller();
  public static final Marshaller<UploadRequest> UPLOAD_REQUEST
      = new UploadRequestMarshaller();
  public static final Marshaller<UploadResponse> UPLOAD_RESPONSE
      = new UploadResponseMarshaller();
  public static final Marshaller<DataRequest> DATA_REQUEST
      = new DataRequestMarshaller();
  public static final Marshaller<DataResponse> DATA_RESPONSE
      = new DataResponseMarshaller();

  public static final Marshaller<ChecksumRequest> CHECKSUM_REQUEST
      = new ChecksumRequestMarshaller();

  public static final Marshaller<ChecksumResponse> CHECKSUM_RESPONSE
      = new ChecksumResponseMarshaller();

  public static final Marshaller<TerminationRequest> TERMINATION_REQUEST
      = new TerminationRequestMarshaller();
}
