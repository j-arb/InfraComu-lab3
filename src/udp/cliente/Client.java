package udp.cliente;

import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;

import utils.Converter;
import utils.Logger;

public class Client extends Thread {

    private static final String CLIENT_FILES_DIR = "files/client";
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 6869;
    private static final int FILE_DATAGRAM_SIZE = 10*1024;

    private DatagramSocket socket;
    private Logger logger;
    private int numConnections;
    private int id;
    private long fileLength;
    private CyclicBarrier endCb;

    public Client(int id, int numConnections, Logger logger, CyclicBarrier endCb) {
        this.id = id;
        this.numConnections = numConnections;
        this.logger = logger;
        this.endCb = endCb;
        log("Client created");
    }

    @Override
    public void run() {
        try {
            log("Starting client");
            this.socket = new DatagramSocket();
            socket.setSoTimeout(50);
            log("Socket created. Requesting file size");
            byte[] buf = Converter.longToByteArray(-2);
            DatagramPacket reqDatagram = new DatagramPacket(
                buf,
                buf.length,
                InetAddress.getByName(SERVER_ADDRESS),
                SERVER_PORT
            );
            socket.send(reqDatagram);
            log("Waiting for file size");
            byte[] fileLengthB = new byte[8];
            DatagramPacket flDatagram = new DatagramPacket(fileLengthB, 8);
            socket.receive(flDatagram);
            log("File size received");
            fileLength = Converter.byteArrayToLong(fileLengthB);
            log("File size correct. Starting file transfer");
            String filePath = Paths.get(CLIENT_FILES_DIR, "ClienteUDP " + id + " de " + numConnections +
                ".bin").toString();
            receiveFile(filePath);
            log("File received and stored on " + filePath);
            endCb.await();
            log("Thread end");
        } catch (Exception e) {
            log("Error working over socket for " + SERVER_ADDRESS + ":" + SERVER_PORT);
            e.printStackTrace();
        }
    }

    private void receiveFile(String fileName) throws Exception{
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        double numberOfDatagrams = Math.ceil((double)(fileLength / FILE_DATAGRAM_SIZE));
        int remainingBits = (int)fileLength % FILE_DATAGRAM_SIZE;
        log("Starting file transfer with " + numberOfDatagrams + " datagrams");
        int i = 0;
        while(i <= numberOfDatagrams) {
            log("Requesting datagtram # " + i);
            byte[] reqB = Converter.longToByteArray(i);
            socket.send(new DatagramPacket(
                reqB, reqB.length,
                InetAddress.getByName(SERVER_ADDRESS),
                SERVER_PORT
                ));
            log("Datagram # " + i + "requested. Waiting for response");
            try {
                if(i < numberOfDatagrams) {
                    byte[] buffer = new byte[FILE_DATAGRAM_SIZE];
                    DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                    socket.receive(dp);
                    fileOutputStream.write(buffer);
                } else {
                    byte[] buffer = new byte[remainingBits];
                    DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                    socket.receive(dp);
                    fileOutputStream.write(buffer);
                }
                log("Received datagram # " + i);
                i++;
            } catch(SocketTimeoutException e) {
                log("File datagram number " + i + " did not arrive. Retrying");
            }
        }
        // for(int i = 0; i < numberOfDatagrams - 1; i++) {
        //     try {
        //         byte[] buffer = new byte[FILE_DATAGRAM_SIZE];
        //         DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
        //         socket.receive(dp);
        //         fileOutputStream.write(buffer);
        //     } catch(SocketTimeoutException e) {
        //         log("File datagram number " + (i + 1) + " did not arrive after 1 seconds of waiting.");
        //         ftSuccess = false;
        //     }
        // }
        // try {
            
        //     byte[] buffer = new byte[remainingBits];
        //     DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
        //     socket.receive(dp);
        //     fileOutputStream.write(buffer);
        // } catch(SocketTimeoutException e) {
        //     log("File datagram number " + numberOfDatagrams + " did not arrive after 5 seconds of waiting.");
        //     ftSuccess = false;
        // }
        fileOutputStream.close();
    }

    private void log(String msg) {
        logger.log("Client (" + id + "): " + msg);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the number of connections to stablish." +
        " (Make sure this number matches with the number of connections the server is expecting)");
        System.out.print(">> ");
        int numConections = sc.nextInt();
        Logger logger = new Logger("UDPClient");
        CyclicBarrier endCb = new CyclicBarrier(numConections + 1);
        for(int i = 1; i <= numConections; i ++) {
            new Client(i, numConections, logger, endCb).start();
        }
        sc.close();
        try {
            endCb.await();
            DatagramSocket socket = new DatagramSocket();
            byte[] buf = Converter.longToByteArray(-1);
                DatagramPacket endReqDatagram = new DatagramPacket(
                    buf,
                    buf.length,
                    InetAddress.getByName(SERVER_ADDRESS),
                    SERVER_PORT
                );
            socket.send(endReqDatagram);
            socket.close();
        } catch(Exception e) {
            logger.log("Unable to send end message to server");
        }
    }
    
}
