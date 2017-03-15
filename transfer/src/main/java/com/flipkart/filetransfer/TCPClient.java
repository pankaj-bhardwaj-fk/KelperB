package com.flipkart.filetransfer;


import com.flipkart.Worker;
import com.flipkart.dto.NodeData;
import com.flipkart.dto.ResultType;
import com.flipkart.dto.ServiceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class TCPClient implements Worker {
    private static final Logger logger = LoggerFactory.getLogger(TCPClient.class);

    private ServiceNode<NodeData> serverNode;
    private ServiceNode<NodeData> nodeData;

    private SocketChannel createSocketChannel() throws IOException {
        ServerSocketChannel serverSocketChannel = null;
        SocketChannel socketChannel = null;
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(nodeData.getHost(), nodeData.getPort()));
        socketChannel = serverSocketChannel.accept();
        logger.info("Connection established....");
        return socketChannel;
    }

    public void readFileFromSocket(String toFilePath) throws IOException, InterruptedException {
        SocketChannel socketChannel = null;
        FileChannel fileChannel = null;
        try {

            socketChannel = createSocketChannel();
            RandomAccessFile aFile = null;
            aFile = new RandomAccessFile(toFilePath, "rw");
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            fileChannel = aFile.getChannel();
            while (socketChannel.read(buffer) > 0) {
                buffer.flip();
                fileChannel.write(buffer);
                buffer.clear();
            }
            Thread.sleep(1000);
        } finally {
            logger.info("End of file reached..Closing channel");

            if (fileChannel != null)
                fileChannel.close();
            if (socketChannel != null)
                socketChannel.close();
        }

    }

    @Override
    public void setData(Object... data) {
        //nodeData = (ServiceNode<NodeData>) data[0];
        serverNode = (ServiceNode<NodeData>) data[0];
    }

    @Override
    public ResultType doWork() {

        String toFilePath = null;
        try {
            readFileFromSocket(toFilePath);
        } catch (IOException e) {
            logger.error("IOException while transfering data at tcp layer ", e);
            e.printStackTrace();
            return ResultType.FAIL;
        } catch (InterruptedException e) {
            logger.error("IOException while transfering data at tcp layer ", e);
            e.printStackTrace();
            return ResultType.FAIL;
        }
        logger.info("File transfer successful!!!");
        return ResultType.SUCCESSFUL;
    }

    public void releaseResources() {

    }


}
