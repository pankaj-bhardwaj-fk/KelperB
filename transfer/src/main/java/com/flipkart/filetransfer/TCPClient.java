package com.flipkart.filetransfer;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created on 03/03/17 by dark magic.
 * <p>
 * a simple tcp network client
 */
public class TCPClient {
    private final String host;
    private final int port;

    public TCPClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private SocketChannel createSocketChannel() throws IOException {
        ServerSocketChannel serverSocketChannel = null;
        SocketChannel socketChannel = null;
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        socketChannel = serverSocketChannel.accept();
        System.out.println("Connection established....");
        return socketChannel;
    }

    public void readFileFromSocket(String toFilePath) throws IOException, InterruptedException {
        SocketChannel socketChannel = createSocketChannel();
        RandomAccessFile aFile = null;
        aFile = new RandomAccessFile(toFilePath, "rw");
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        FileChannel fileChannel = aFile.getChannel();
        while (socketChannel.read(buffer) > 0) {
            buffer.flip();
            fileChannel.write(buffer);
            buffer.clear();
        }
        Thread.sleep(1000);
        fileChannel.close();
        System.out.println("End of file reached..Closing channel");
        socketChannel.close();
    }
}
