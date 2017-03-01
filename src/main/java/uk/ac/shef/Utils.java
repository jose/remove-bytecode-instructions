package uk.ac.shef;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utils {

  public static byte[] getByteArray(long length, InputStream in) throws IOException {
    if (length < Integer.MIN_VALUE || length > Integer.MAX_VALUE) {
      throw new IllegalArgumentException(
          length + " cannot be cast to int without changing its value!");
    }

    byte[] buffer = new byte[(int) length];
    ByteArrayOutputStream out = new ByteArrayOutputStream(buffer.length);

    int nbytes = 0;
    while ((nbytes = in.read(buffer)) != -1) {
      out.write(buffer, 0, nbytes);
    }

    in.close();
    out.close();

    return out.toByteArray();
  }
}
