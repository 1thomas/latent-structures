import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 */
public class SortingFixedSizeBinaryBuffer {
    private final Object newDataNotifier = new Object();
    private final int packetSize;
    private final int packetCount;
    private final ByteBuffer storage;
    private final ByteBuffer header;
    private final ByteBuffer content;

    private int readIndex = 0;


    public SortingFixedSizeBinaryBuffer(int packetSize, int packetCount) {
        if (((long) packetSize) * packetCount > Integer.MAX_VALUE)
            throw new IllegalArgumentException("packetSize * packetCount > Integer.MAX_VALUE");

        int headerSize = packetCount;

        this.packetSize = packetSize;
        this.packetCount = packetCount;
        storage = ByteBuffer.allocate(headerSize + packetSize * packetCount);

        storage.position(headerSize);
        content = storage.slice();

        storage.position(0);
        storage.limit(headerSize);
        header = storage.slice();

        storage.position(0);
        storage.limit(storage.capacity());
    }

    public void put(int index, byte[] value) {
        if (value.length != packetSize)
            throw new IllegalArgumentException("Invalid size");

        if (index >= packetCount || index < 0)
            throw new IllegalArgumentException("Invalid index");

        content.position(index * packetSize);
        content.put(value);

        commitIndex(index);
    }

    public void put(int index, ByteBuffer value) {
        if (index >= packetCount || index < 0)
            throw new IllegalArgumentException("Invalid index");

        content.position(index * packetSize);
        content.put(value);

        commitIndex(index);
    }

    public ByteBuffer getBuffer(int index) {
        if (index >= packetCount || index < 0)
            throw new IllegalArgumentException("Invalid index");

        content.limit(index * packetSize + packetSize);
        content.position(index * packetSize);
        return content.slice();
    }

    public void commitIndex(int index) {
        if (index >= packetCount || index < 0)
            throw new IllegalArgumentException("Invalid index");

        header.position(index);
        header.put((byte) 1);

        newDataNotifier.notifyAll();
    }

    public boolean isCommitted(int index) {
        return header.get(index) == 1;
    }
    public static class IndexBufferPair {
        long index;
        ByteBuffer buffer;

    }

    public IndexBufferPair read() throws IOException {
        while (!isCommitted(readIndex) && readIndex < packetCount)
            readIndex++;

        if (readIndex >= packetCount)
            throw new IOException("End of buffer reached");

        content.limit(readIndex * packetSize + packetSize);
        content.position(readIndex * packetSize);

        IndexBufferPair result = new IndexBufferPair();
        result.index = readIndex;
        result.buffer = content.slice();

        readIndex++;

        return result;
    }

    public IndexBufferPair readNextTimeout(long maxWait) throws IOException, InterruptedException {
        if (!isCommitted(readIndex)) {
            synchronized (newDataNotifier) {
                newDataNotifier.wait(maxWait);
            }
            // bug: this is also notified if the inserted packet is not the one we're looking for, so
            // turn the if into a loop, and wait for the remaining time.
        }

        return read();
    }
}
