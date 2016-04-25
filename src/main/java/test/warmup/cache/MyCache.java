package test.warmup.cache;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * Created by vladimir on 4/21/16.
 */

public class MyCache extends Thread {
    public static final byte[] NO_BYTES = new byte[0];

    private final int maxSize;
    private final Map<Integer, byte[]> dataMap;
    private final Queue<Integer> lruQueue;
    private int putCount;
    private int cacheSize;

    /*
    In fact, every operation is mutable due lru support operations. So plain Object + 'synchronized' section
    is suitable here (hurts performance).
    */
    private final Object locker;

    //DISK IO class members
    private final Map<Integer, byte[]> offloadInProgressItems;
    private final BlockingQueue<Integer> offloaderWorkQueue;
    private volatile boolean canRun;

    private MyCache (int maxSize) {
        this.maxSize      = maxSize;
        this.dataMap      = new HashMap<>();
        this.lruQueue     = new LinkedList<>();
        this.locker       = new Object();


        this.offloaderWorkQueue     = new LinkedBlockingQueue();
        this.offloadInProgressItems = new ConcurrentHashMap<>();
        this.canRun                 = true;
    }


    public byte[] get(Integer key) {
        byte[] cachedBytes;
        synchronized (locker) {
            cachedBytes = dataMap.get(key);
            if (cachedBytes == null) {
                cachedBytes = loadFromFile(key);
                offloadToDiskIfNeeded(cachedBytes.length);
                dataMap.put(key, cachedBytes);
                cacheSize += cachedBytes.length;
            }
            lruQueue.remove(key);
            lruQueue.add(key);
        }
        return cachedBytes;
    }

    public Integer put(byte[] value) {
        Integer key;
        synchronized (locker) {
            key = ++putCount;
            offloadToDiskIfNeeded(value.length);
            lruQueue.add(key);
            dataMap.put(key, value);
        }
        return key;
    }

    private void offloadToDiskIfNeeded(int freeSizeRequired) {
        while (maxSize - cacheSize <= freeSizeRequired) {
            Integer lruKey = lruQueue.poll();
            if(lruKey != null) {
                byte[] data = dataMap.remove(lruKey);
                if (data != null) {
                    cacheSize -= data.length;
                }
            }
        }
    }


    public int putToCache(byte[] data){
        return put(data);
    }

    public byte[] getFromCache(int id){
        return get(id);
    }

    public void run() {
        try {
            while (canRun && !Thread.currentThread().isInterrupted()) {
                Integer itemIdToOffload = offloaderWorkQueue.take();
                byte[] data = offloadInProgressItems.get(itemIdToOffload);
                this.saveToFile(itemIdToOffload.toString(), data);
                offloadInProgressItems.remove(itemIdToOffload);
            }
        }catch(InterruptedException ex) {
            System.err.println("Offloader thread finished");
        }
    }

    private byte[] loadFromFile(Integer id) {
        byte[] result = offloadInProgressItems.get(id);
        if (result == null) {
            result = loadFromFile(id.toString());
        }
        return result;
    }

    public byte[] loadFromFile(String filename) {
        return NO_BYTES;
    }

    public void saveToFile(String filename, byte[] data){
    }


    public static MyCache newInstance(int maxSize) {
        MyCache result = new MyCache(maxSize);
        result.start();
        return  result;
    }

    public static MyCache newInstance() {
        return  newInstance(100 * 1024 * 1024);
    }
}