package com.wyh.rpc.common.compress;

public interface Compress {

    byte[] compress(byte[] bytes);

    byte[] decompress(byte[] bytes);

}
