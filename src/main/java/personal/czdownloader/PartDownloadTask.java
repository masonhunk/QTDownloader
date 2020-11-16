package personal.czdownloader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * @author aaronchu
 * @Description
 * @data 2020/11/16
 */
public class PartDownloadTask implements Supplier<Long> {

    private URL url;
    private FilePart filePart;
    private File destinationFile;
    private Downloader downloader;

    private byte[] byteBuffer =new byte[2048];

    public PartDownloadTask(URL url, FilePart filePart, File destinationFile, Downloader downloader){
        this.url = url;
        this.filePart = filePart;
        this.destinationFile = destinationFile;
        this.downloader = downloader;
    }

    @Override
    public Long get() {

        int tries = 5;
        for(int i=0;i<tries;i++){
            Long result = doGet();
            if(result.longValue() != -1){
                return result;
            }
            System.out.println("微众防火墙作妖，重试!");
        }
        System.out.println("防火墙太牛逼了，失败");
        return 0L;
    }

    private Long doGet() {
        long totalRead = 0;
        try{
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("Connection","keep-alive");
            String rangeStart = Long.toString(filePart.getOffset());
            String rangeEnd = Long.toString(filePart.getOffset() + filePart.getSize()-1);
            connection.setRequestProperty("Range","bytes="+rangeStart+"-"+rangeEnd);
            connection.setUseCaches(false);

            try(InputStream is = new BufferedInputStream(connection.getInputStream());
                FileOutputStream fos = new FileOutputStream(this.destinationFile, true)){
                FileChannel channel = fos.getChannel();
                long position = this.filePart.getOffset();
                int n ;
                long rangeLength = connection.getContentLengthLong();
                if(rangeLength != this.filePart.getSize()){
                    readAllData(connection);
                    return -1L;
                }
                while((n = is.read(byteBuffer)) != -1){
                    ByteBuffer bb = ByteBuffer.wrap(byteBuffer, 0, n);
                    int writeNum = channel.write(bb, position);
                    if(n != writeNum){
                        throw new IOException("write failed "+ writeNum);
                    }
                    totalRead += n;
                    position += n;
                }
                channel.force(false);
                this.downloader.increment(totalRead);
                return totalRead;
            }
            catch (IOException ex){
                int responseCode = connection.getResponseCode();
                String errorInfo = null;
                try(InputStream es = connection.getErrorStream()){
                    if(es != null){
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        IOUtil.copy(es, baos);
                        errorInfo = new String(baos.toByteArray());
                    }
                }
                throw new DownloadException("Server returned code "+responseCode + " with "+errorInfo, ex);
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
            this.downloader.increment(totalRead);
            return totalRead;
        }
    }

    //Read firewall data. May reuse
    private void readAllData(HttpURLConnection httpURLConnection) throws IOException{
        try(InputStream is = httpURLConnection.getInputStream()){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtil.copy(is, baos);
        }
        catch (IOException ex){
            try(InputStream es = httpURLConnection.getErrorStream()){
                if(es != null){
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    IOUtil.copy(es, baos);
                    System.out.println(new String(baos.toByteArray()));
                }
            }
        }
        catch (Exception e){

        }
    }
}
