package com.flipkart.filetransfer;

/**
 * Created on 03/03/17 by dark magic.
 */
public class TcpClientServer {
    private final TCPClient tcpClient;
    private final TCPServer tcpServer;

    public TcpClientServer(TCPClient tcpClient, TCPServer tcpServer) {
        this.tcpClient = tcpClient;
        this.tcpServer = tcpServer;
    }

    public void sendFile(){
        //this.tcpServer.sendFile();
    }

//    public void receiveFile(){
//        this.tcpClient.readFileFromSocket();
//    }
}
