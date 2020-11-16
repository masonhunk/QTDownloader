package personal.downloadmanager;

import org.junit.Test;
import personal.czdownloader.Downloader;
import personal.czdownloader.FilePart;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.CyclicBarrier;

/**
 * @author aaronchu
 * @Description
 * @data 2020/11/16
 */
public class DownloaderTest {

    @Test
    public void testGetFileLength() throws Exception{
        System.out.println("Hello");
        Downloader downloader = new Downloader("http://106.12.193.68:5299/0.binlog", 2);
        long length = downloader.queryContentLength();
        System.out.println(length);
    }

    @Test
    public void testFilename() throws Exception{
        Downloader downloader = new Downloader("http://106.12.193.68:5299/0.binlog", 2);
        File f = downloader.computeDestinationFile();
        System.out.println(f.getAbsolutePath());
    }

    @Test
    public void testGenerateParts() throws Exception{
        Downloader downloader = new Downloader("http://106.12.193.68:5299/0.binlog", 3);
        FilePart[] fileParts = downloader.generateParts(10);
        for(int i=0;i<fileParts.length;i++){
            FilePart filePart = fileParts[i];
            System.out.println(filePart.getOffset()+"-"+(filePart.getOffset() + filePart.getSize()-1));
        }

    }

    @Test
    public void testCeilingDiv(){
        long fileSize = 12;
        long part = 3;
        long val = (long)Math.ceil((double)fileSize / part);
        System.out.println(val);

        long remaining = fileSize % val;
        remaining = remaining ==0 ? val:remaining;
        System.out.println("val "+ val + ": "+remaining);
    }

    @Test
    public void testWriteConcurrentSeek() throws Exception{
        File f = new File("C:\\Users\\aaronchu\\Desktop\\aaa.txt");
        long fileSize = f.length();

        int partNumber = 10;
        CyclicBarrier cb = new CyclicBarrier(partNumber);
        for(int i=0;i<partNumber;i++){
            int finalI = i;
            Thread t = new Thread(()->{
                try{
                    Thread.sleep(3000);
                    cb.await();
                    System.out.println("线程启动:"+Thread.currentThread().getName());
                    FileOutputStream fos = new FileOutputStream(f, true);
                    FileChannel channel = fos.getChannel();
                    channel.write(ByteBuffer.wrap(new byte[]{98}), finalI);
                    System.out.println("线程结束:"+Thread.currentThread().getName());
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
            });
            t.start();

        }

        System.in.read();
    }

    @Test
    public void testDownload() throws Exception{
        Downloader downloader = new Downloader("http://106.12.193.68:5299/0.binlog", 3);
        downloader.download();

    }

    @Test
    public void testEqual(){
        Long i = new Long(0xFFFFFFFFFFFFL);
        System.out.println(i == 0xFFFFFFFFFFFFL);
    }
}
