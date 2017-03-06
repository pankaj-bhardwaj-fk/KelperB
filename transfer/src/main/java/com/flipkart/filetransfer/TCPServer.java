package com.flipkart.filetransfer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * Created on 03/03/17 by dark magic.
 */
public class TCPServer {
    private final String host;
    private final int port;

    public TCPServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private SocketChannel createSocketChannel(String host, int port) throws IOException {
        SocketChannel socketChannel = null;
        socketChannel = SocketChannel.open();
        SocketAddress socketAddress = new InetSocketAddress(host, port);
        socketChannel.connect(socketAddress);
        System.out.println("Connected..Now sending the file");
        return socketChannel;
    }

    public void sendFile(String filePath, String host, int port) throws IOException, InterruptedException {
        SocketChannel socketChannel = createSocketChannel(host, port);
        File file = new File(filePath);
        RandomAccessFile aFile = new RandomAccessFile(file, "r");
        FileChannel inChannel = aFile.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (inChannel.read(buffer) > 0) {
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
        }
        Thread.sleep(1000);
        System.out.println("End of file reached..");
        socketChannel.close();
        aFile.close();
    }
}
