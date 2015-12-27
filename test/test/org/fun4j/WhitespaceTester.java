package test.org.fun4j;

import org.junit.Test;

import java.util.Arrays;
import java.util.BitSet;

/**
 * Created by thomas on 02.10.2014.
 */
public class WhitespaceTester {

  private static final Character ONE = '1';
  private static final Character ZERO = '0';

  private static final String secretMsg = "0100100001100001011011000110110001101111001000000101011101100101011011000111010000100000011010010110111000100000010101111110010001101100011001000010000011100100011011100110010000100000010001100110110001110101011100100010000000100001";

  @Test
  public void testEncoding() throws Exception {
    String msg = "Hallo Welt in Wäld änd Flur !";
    String result = "";

    char[] chars = new char[msg.length()];
    msg.getChars(0, msg.length(), chars, 0);

    for (char c : chars) {
      String encodedByte = Integer.toString((int)c, 2);
      if (encodedByte.length() < 8) {
        encodedByte = fillStringWithLeadingZeros(encodedByte, 8-encodedByte.length());
      }
      result += encodedByte;
    }

    System.out.println(result);
    System.out.println(result.length());
  }

  private String fillStringWithLeadingZeros(String encodedByte, int i) {
    char[] zeros = new char[i];
    Arrays.fill(zeros, ZERO);
    return new String(zeros) + encodedByte;
  }

  @Test
  public void testDecoding() throws Exception {
    String result = "";

   for (int i=0; i<secretMsg.length(); i+=8) {
      String str = secretMsg.substring(i,i+8);

      char c = (char) Integer.valueOf(str,2).intValue();
      result += c;
    }
    System.out.println("result: " + result);

  }


}
