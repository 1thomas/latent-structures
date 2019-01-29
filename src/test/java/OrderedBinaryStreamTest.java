import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.*;

public class OrderedBinaryStreamTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void simpleTest() throws IOException {
        OrderedBinaryStream stream = new OrderedBinaryStream();
        stream.put(3, new byte[] {1,2,3});
        stream.put(5, new byte[] {});
        stream.put(8, new byte[] {6,7});
        stream.put(7, new byte[] {4,5});

        InputStream inputStream = stream.getInputStream();

        assertArrayEquals(new byte[] {1,2,3,4,5,6,7}, inputStream.readNBytes(7));
    }


    @Test(expected = IOException.class)
    public void throwsOnEOF() throws IOException {
        OrderedBinaryStream stream = new OrderedBinaryStream();
        stream.put(3, new byte[] {1,2,3});
        stream.put(5, new byte[] {});
        stream.put(8, new byte[] {6,7});
        stream.put(7, new byte[] {4,5});

        InputStream inputStream = stream.getInputStream();

        inputStream.readNBytes(8);
    }
}