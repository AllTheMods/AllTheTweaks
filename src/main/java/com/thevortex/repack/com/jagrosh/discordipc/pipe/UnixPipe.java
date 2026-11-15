package com.thevortex.repack.com.jagrosh.discordipc.pipe;
/*
 * Copyright 2017 John Grosh (john.a.grosh@gmail.com). Licensed under the Apache License, Version 2.0.
 * Unix domain socket implementation for Linux/macOS.
 */


import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;

/**
 * Unix domain socket implementation using Java NIO SocketChannel.
 */
public class UnixPipe implements Pipe {

    private final SocketChannel channel;
    private int peeked = -1;

    public UnixPipe(String path) throws IOException {
        UnixDomainSocketAddress address = UnixDomainSocketAddress.of(Path.of(path));
        this.channel = SocketChannel.open(StandardProtocolFamily.UNIX);
        this.channel.connect(address);
        this.channel.configureBlocking(true);
    }

    @Override
    public void write(byte[] data) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    @Override
    public int readInt() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.BIG_ENDIAN);
        int totalRead = takePeeked(buffer);
        while (totalRead < 4) {
            int read = channel.read(buffer);
            if (read == -1) {
                throw new IOException("End of stream reached");
            }
            totalRead += read;
        }
        buffer.flip();
        return buffer.getInt();
    }

    @Override
    public void readFully(byte[] data) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int totalRead = takePeeked(buffer);
        while (totalRead < data.length) {
            int read = channel.read(buffer);
            if (read == -1) {
                throw new IOException("End of stream reached");
            }
            totalRead += read;
        }
    }

    @Override
    public boolean hasData() throws IOException {
        if (peeked != -1) {
            return true;
        }
        channel.configureBlocking(false);
        ByteBuffer peekBuffer = ByteBuffer.allocate(1);
        int read = channel.read(peekBuffer);
        channel.configureBlocking(true);
        if (read > 0) {
            // the peeked byte belongs to the next packet, so the next read starts with it
            peeked = peekBuffer.get(0) & 0xFF;
            return true;
        }
        return read != -1;
    }

    private int takePeeked(ByteBuffer buffer) {
        if (peeked == -1 || !buffer.hasRemaining()) {
            return 0;
        }
        buffer.put((byte) peeked);
        peeked = -1;
        return 1;
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}