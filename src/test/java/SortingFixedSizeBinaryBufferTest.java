import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class SortingFixedSizeBinaryBufferTest {

    @Test
    public void putAndReadBack() throws IOException, InterruptedException {
        SortingFixedSizeBinaryBuffer buffer = new SortingFixedSizeBinaryBuffer(5, 5);

        buffer.put(3, new byte[] {6,6,6,6,6});
        buffer.put(1, new byte[] {1,2,3,4,5});

        SortingFixedSizeBinaryBuffer.IndexBufferPair result = buffer.readNextTimeout(50l);
        byte[] testRes = new byte[5];
        result.buffer.get(testRes);
        assertArrayEquals(testRes, new byte[] {1,2,3,4,5});

        result = buffer.readNextTimeout(50l);
        result.buffer.get(testRes);
        assertArrayEquals(testRes, new byte[] {6,6,6,6,6});
    }
}