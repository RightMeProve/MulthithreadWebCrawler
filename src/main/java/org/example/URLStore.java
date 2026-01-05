package org.example;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class URLStore {
    private final ConcurrentHashMap<String,Boolean> visitedUrl = new ConcurrentHashMap<>();
    private final BlockingQueue<String> urlQueue = new LinkedBlockingQueue<>();

    public boolean addUrl(String url){
        if(visitedUrl.putIfAbsent(url,true)==null){
            urlQueue.offer(url);
            return true;
        }
        return false;
    }

    public String getNextUrl() throws InterruptedException{
        return urlQueue.poll();
    }

    public boolean isQueueEmpty(){
        return urlQueue.isEmpty();
    }
}
