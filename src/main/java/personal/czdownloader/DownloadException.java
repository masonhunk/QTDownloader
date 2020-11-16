package personal.czdownloader;


/**
 * @author aaronchu
 * @Description
 * @data 2020/11/16
 */
public class DownloadException extends Exception {

    public DownloadException(String message){
        super(message);
    }

    public DownloadException(Throwable ex){
        super(ex);
    }

    public DownloadException(String message, Throwable ex){
        super(message, ex);
    }
}
