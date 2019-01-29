import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Insert something with a Long as index, a T as value.
 */
public class OrderedBinaryStream {
    public OrderedBinaryStream() {

    }

    private ConcurrentSkipListMap<Long, byte[]> storage = new ConcurrentSkipListMap<>();

    public void put(long index, byte[] value) {
        storage.put(index, value);
    }

    public InputStream getInputStream() {
        return new InputStream() {
            long lastKey = Long.MIN_VALUE;
            byte[] currentBuffer;
            int currentIndex = 0;

            @Override
            public int read() throws IOException {
                while(currentBuffer==null || currentBuffer.length <= currentIndex) {
                    try {
                        lastKey = findNextKey(lastKey);
                        currentBuffer = storage.get(lastKey);
                        currentIndex = 0;

                    } catch (NullPointerException e) {
                        throw new IOException("End of stream reached.");
                    }
                }

                return currentBuffer[currentIndex++];
            }

            private long findNextKey(long lastIndex) throws NullPointerException {
                return storage.ceilingKey(lastIndex + 1);
            }
        };
    }

}
