package tech.mlsql.app_runtime.ar_plugin_repo;

import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

/**
 * 31/1/2020 WilliamZhu(allwefantasy@gmail.com)
 */
public class DownloadFileUtils {
    private static final String HEADER_KEY = "Content-Disposition";
    private static final String HEADER_VALUE = "attachment; filename=";

    public static void getFileByPath(HttpServletResponse response, String pathStr) throws Exception {

        String[] fileChunk = pathStr.split("/");
        response.setContentType("application/octet-stream");
        //response.setHeader("Transfer-Encoding", "chunked");
        response.setContentLength((int)(new File(pathStr).length()));
        response.setHeader(HEADER_KEY, HEADER_VALUE + "\"" + URLEncoder.encode(fileChunk[fileChunk.length - 1], "utf-8") + "\"");
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            inputStream = new FileInputStream(pathStr);
            org.apache.commons.io.IOUtils.copyLarge(inputStream, outputStream);
        } catch (Exception e) {
            throw e;
        } finally {
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(inputStream);
        }
    }
}
