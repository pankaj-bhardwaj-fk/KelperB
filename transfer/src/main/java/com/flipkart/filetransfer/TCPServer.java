package com.flipkart.filetransfer;

import com.flipkart.Worker;
import com.flipkart.dto.NodeData;
import com.flipkart.dto.ResultType;
import com.flipkart.dto.ServiceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class TCPServer implements Worker {
    private static final Logger logger = LoggerFactory.getLogger(TCPServer.class);
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
        socketChannel.finishConnect();
        logger.info("Connected..Now sending the file");
        return socketChannel;
    }

    public void sendFile(String filePath, String host, int port) throws IOException, InterruptedException {
        SocketChannel socketChannel = null;
        FileChannel inChannel = null;
        RandomAccessFile aFile = null;
        try {
            socketChannel = createSocketChannel(host, port);
            File file = new File(filePath);
            aFile = new RandomAccessFile(file, "r");
            inChannel = aFile.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (inChannel.read(buffer) > 0) {
                buffer.flip();
                socketChannel.write(buffer);
                buffer.clear();
            }
            Thread.sleep(1000);
            logger.info("End of file reached..");
        } finally {
            if (socketChannel != null)
                socketChannel.close();
            if (aFile != null)
                aFile.close();
        }
    }

    @Override
    public void setData(Object... data) {
        ServiceNode<NodeData> nodeData = (ServiceNode<NodeData>) data[0];
    }

    @Override
    public ResultType doWork() {
        try {
            String filePath = "";
            sendFile(filePath, "dummyHost", 10010);
        } catch (InterruptedException e) {
            logger.error("Error occurred in tcp Server ", e);
            return ResultType.FAIL;
        } catch (IOException e) {
            logger.error("Error occurred in tcp Server ", e);
            return ResultType.FAIL;
        }
        logger.info("Successful transfer!!!");
        return ResultType.SUCCESSFUL;
    }

    @Override
    public void releaseResources() {

    }
}
