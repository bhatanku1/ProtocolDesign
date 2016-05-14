package pft.frames.marshallers;

import com.google.common.base.Preconditions;

import java.nio.charset.Charset;

public class Utils {

  public static String filenameToString(byte[] filename) {
    int length = 0;
    while (length < filename.length && filename[length] != 0) {
      length++;
    }

    Preconditions.checkState(length <= 256);

    return new String(filename, 0, length, Charset.forName("ASCII"));
  }
}
