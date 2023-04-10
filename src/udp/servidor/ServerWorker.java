package udp.servidor;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import utils.Converter;
import utils.Logger;

public class ServerWorker extends Thread {
    private int id = 0;
    private File file;
    private Logger logger;
    private InetAddress clientAddress;
    private int clientPort;
    private DatagramSocket socket;
    private Server server;
    private long datagramNo;
    private static final int FILE_DATAGRAM_SIZE = 10*1024;
    
    public ServerWorker(
        File file,
        InetAddress clientAddress,
        int clientPort,
        long datagramNo,
        Logger logger,
        Server server
        ) {
        this.file = file;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.logger = logger;
        this.server = server;
        this.datagramNo = datagramNo;
        this.id = server.incrementNumSW();
        log("Started");
    }

    @Override
    public void run() {
        try {
            this.socket = new DatagramSocket();
            if(this.datagramNo == -2) {
                log("File size requested. Sending file size");
                byte[] fileLength = Converter.longToByteArray(file.length());
                socket = new DatagramSocket();
                DatagramPacket dpFileSize = new DatagramPacket(fileLength, 8, clientAddress, clientPort);
                socket.send(dpFileSize);
                log("Sent file size.");
            } else {
                log("Datagram no " + datagramNo + " requested. Starting transfer");
                double numberOfDatagrams = Math.ceil((double)(file.length() / FILE_DATAGRAM_SIZE));
                int remainingBits = (int)file.length() % FILE_DATAGRAM_SIZE;
                long startByte = (long)datagramNo * FILE_DATAGRAM_SIZE;
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                raf.seek(startByte);
                if(datagramNo < numberOfDatagrams) {
                    byte[] buffer = new byte[FILE_DATAGRAM_SIZE];
                    raf.read(buffer);
                    DatagramPacket data = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
                    socket.send(data);
                } else {
                    byte[] buffer = new byte[remainingBits];
                    raf.read(buffer);
                    DatagramPacket data = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
                    socket.send(data);
                }
                log("Datagram no " + datagramNo + " sent");
                raf.close();
            }
        } catch(Exception e) {
            log("Error in server worker. StackTrace: " + e.getStackTrace().toString());
            System.out.println("Error in server worker.");
            e.printStackTrace();
        }
        server.reduceNumSW();
    }

    // private void sendFile(File file) throws Exception {
    //     FileInputStream fileInputStream = new FileInputStream(file);
    //     double numberOfDatagrams = Math.ceil((double)(file.length() / FILE_DATAGRAM_SIZE));
    //     int remainingBits = (int)file.length() % FILE_DATAGRAM_SIZE;
    //     for(int i = 0; i < numberOfDatagrams; i++) {
    //         byte[] buffer = new byte[FILE_DATAGRAM_SIZE];
    //         fileInputStream.read(buffer);
    //         DatagramPacket data = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
    //         socket.send(data);
    //     }

    //     byte[] buffer = new byte[remainingBits];
    //     fileInputStream.read(buffer);
    //     DatagramPacket data = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
    //     socket.send(data);

    //     fileInputStream.close();
    // }

    private void log(String msg) {
        String logMsg = "ServerWorker(" + id + "): " + msg;
        this.logger.log(logMsg);
    }
}
