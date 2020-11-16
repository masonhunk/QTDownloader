package personal.czdownloader;

import java.net.URL;
import java.util.concurrent.Callable;

/**
 * @author aaronchu
 * @Description
 * @data 2020/11/16
 */
public class FilePart{

    private long offset;
    private long size;
    private boolean lastPart;

    public FilePart(long offset, long size, boolean lastPart){
        this.offset = offset;
        this.size = size;
        this.lastPart = lastPart;
    }

    public FilePart(long offset, long size){
        this.offset = offset;
        this.size = size;
        this.lastPart = false;
    }

    public long getOffset() {
        return offset;
    }


    public long getSize() {
        return size;
    }

    public boolean isLastPart() {
        return lastPart;
    }
}
