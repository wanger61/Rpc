package com.wyh.rpc.common.compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipCompress implements Compress {

    private static final int BUFFER_SIZE = 1024*8;

    @Override
    public byte[] compress(byte[] bytes) {
        if (bytes == null){
            throw new NullPointerException("bytes is null");
        }
        try(ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(bytes);
            gzip.flush();
            gzip.finish();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("gzip compress error", e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        if (bytes == null){
            throw new NullPointerException("bytes is null");
        }
        try(ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            GZIPInputStream gzip = new GZIPInputStream(in)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            while ((len = gzip.read(buffer)) != -1){
                out.write(buffer, 0, len);
                out.flush();
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("gzip decompress error", e);
        }
    }
}
