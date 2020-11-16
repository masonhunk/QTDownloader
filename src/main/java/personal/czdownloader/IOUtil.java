package personal.czdownloader;

import java.io.*;

public class IOUtil {

    private static final int BUFFER_SIZE = 2048;

    public static void copy(InputStream from, OutputStream to) throws IOException {
        byte[] buffer = new byte[2048];
        int n;
        try(BufferedInputStream bis = new BufferedInputStream(from);
            BufferedOutputStream bos = new BufferedOutputStream(to)){
            while ((n = bis.read(buffer)) != -1){
                bos.write(buffer, 0, n);
            }
            bos.flush();
        }
    }

}