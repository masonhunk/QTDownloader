package personal.czdownloader;

import personal.qtdownloader.Download;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author aaronchu
 * @Description
 * @data 2020/11/16
 */
public class Downloader {


    private URL url;
    private int partsNum;
    private File downloadFolder;

    private ExecutorService executor;

    private AtomicLong counter;
    private AtomicBoolean started;


    public Downloader(String url, int parts, File downloadFolder) throws Exception{
        this.url = new URL(url);
        this.partsNum = parts;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.downloadFolder = downloadFolder;
        this.started = new AtomicBoolean();
        this.counter = new AtomicLong(0);

        if(!downloadFolder.exists() || !downloadFolder.isDirectory()){
            downloadFolder.mkdirs();
        }
    }

    public Downloader(String url, int parts) throws Exception{
        this(url, parts, new File(System.getProperty("user.home"), ".czdownload"));
    }

    public void download() throws DownloadException{
        if(!this.started.compareAndSet(false, true)){
            System.out.println("Already used. Exit downloader..");
        }
        long contentLength = queryContentLength();
        System.out.println("File size:"+contentLength);
        File file = computeDestinationFile();
        if(file.exists()) file.delete();
        FilePart[] parts = generateParts(contentLength);

        PartDownloadTask[] tasks = Arrays.stream(parts)
                .map(p->new PartDownloadTask(url, p, file, this))
                .toArray(PartDownloadTask[]::new);

        CompletableFuture<Long>[] futures = new CompletableFuture[tasks.length];
        int i =0 ;
        for(PartDownloadTask t: tasks){
            CompletableFuture<Long> cf = CompletableFuture.supplyAsync(t,this.executor);
            futures[i++] = cf;
        }

        CompletableFuture<Void> waitor = CompletableFuture.allOf(futures);
        try{
            waitor.get();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        if(this.counter.get() != contentLength){
            System.out.println("下载失败，仅下载"+this.counter.get()+"字节");
        }
        else{
            System.out.println("下载完成，共下载"+this.counter.get()+"字节");
        }

    }

    public File computeDestinationFile() {
        String filename = this.url.getFile();
        return new File(this.downloadFolder, filename);
    }

    public long queryContentLength() throws DownloadException {
        try{
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            int respCode = connection.getResponseCode();
            long contentLength = connection.getContentLengthLong();
            if(respCode != 200 || contentLength == -1){
                throw new DownloadException("resp code "+ respCode + " content length "+contentLength);
            }
            return contentLength;
        }
        catch (IOException ex){
            throw new DownloadException(ex);
        }
    }

    public FilePart[] generateParts(long fileSize){

        long partSize = (long) Math.ceil((double)fileSize / this.partsNum);
        long lastPart = fileSize % partSize;
        lastPart = lastPart ==0 ? partSize:lastPart;
        FilePart[] fileParts = new FilePart[partsNum];
        long offset = 0;
        for(int i = 0;i<partsNum-1 ;i++,offset+=partSize){
            fileParts[i] = new FilePart(offset, partSize);
        }

        fileParts[partsNum - 1] = new FilePart(offset, lastPart, true);
        return fileParts;
    }

    public void increment(long read){ ;
        this.counter.getAndAdd(read);
        System.out.println("已下载"+read+"字节["+Thread.currentThread().getName()+"]");
    }

}
