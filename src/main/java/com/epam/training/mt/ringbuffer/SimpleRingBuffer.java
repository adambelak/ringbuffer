package com.epam.training.mt.ringbuffer;

public class SimpleRingBuffer implements RingBuffer {

    private final int[] buffer;
    private long readCount;
    private long writeCount;
    private static final Object LOCK = new Object();

    public SimpleRingBuffer(int size) {
        buffer = new int[size];
        readCount = 0;
        writeCount = 0;
    }

    @Override
    public int get() throws InterruptedException {
        int value = 0;
        synchronized (LOCK) {
            while (!canRead()) {
                // wait until writes catch up; all data that had been written
                // has already been read
                LOCK.notifyAll();
                LOCK.wait();
            }
            value = readValue();
            incrementReadCount();
            LOCK.notifyAll();
        }
        return value;
    }

    @Override
    public void put(int value) throws InterruptedException {
        synchronized (LOCK) {
            while (!canStore()) {
                // wait until reads catch up to avoid overwriting data
                LOCK.notifyAll();
                LOCK.wait();
            }
            storeValue(value);
            incrementWriteCount();
            LOCK.notifyAll();
        }
    }

    private boolean canRead() {
        return readCount < writeCount;
    }

    private boolean canStore() {
        return writeCount < readCount + buffer.length;
    }

    private int readValue() {
        int readIndex = (int) (readCount % buffer.length);
        return buffer[readIndex];
    }

    private void storeValue(int value) {
        int writeIndex = (int) (writeCount % buffer.length);
        buffer[writeIndex] = value;
    }

    private void incrementReadCount() {
        readCount++;
    }

    private void incrementWriteCount() {
        writeCount++;
    }

}
