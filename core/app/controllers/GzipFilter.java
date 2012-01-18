package controllers;

import play.mvc.Controller;
import play.mvc.Finally;

import java.io.*;
import java.util.zip.GZIPOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/18/12
 * Time: 6:26 PM
 */
public class GzipFilter extends Controller {
    @Finally
    public static void compress() throws IOException {

        String text = response.out.toString();
        if(text.getBytes().length < 2048) return; // 2kb 以上的才进行 gzip 压缩

        final ByteArrayOutputStream gzip = gzip(text);
        response.setHeader("Content-Encoding", "gzip");
        response.setHeader("Content-Length", gzip.size() + "");
        response.out = gzip;
    }

    public static ByteArrayOutputStream gzip(final String input)
            throws IOException {
        final InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        final ByteArrayOutputStream stringOutputStream = new ByteArrayOutputStream((int) (input.length() * 0.75));
        final OutputStream gzipOutputStream = new GZIPOutputStream(stringOutputStream);

        final byte[] buf = new byte[5000];
        int len;
        while((len = inputStream.read(buf)) > 0) {
            gzipOutputStream.write(buf, 0, len);
        }

        inputStream.close();
        gzipOutputStream.close();

        return stringOutputStream;
    }

}
