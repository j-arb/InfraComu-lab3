package udp.servidor;

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

import utils.Converter;
import utils.Logger;


public class Server {
    private final String file100MB = "files/server/100MB.bin";
    private final String file250MB = "files/server/250MB.bin";
    private File file;
    private Logger logger;
    private DatagramSocket socket;
    private int currentServerWorkers = 0;

    /**
     * Inicializa el servidor
     * @param numConections - Número de conexiones a esperar. Debe ser menor
     * o igual a 25.
     * @param file - 1 para enviar el archivo de 100MB o 2 para enviar el
     * de 250MB.
     */
    public Server(int file) {
        this.logger = new Logger("UDPServer");

        if(file == 1) {
            this.file = new File(file100MB);
            logger.log("Creating server. File name: " + file100MB + ". File size: 100MB");
        } else {
            this.file = new File(file250MB);
            logger.log("Creating server. File name: " + file250MB + ". File size: 250MB");
        }
        logger.log("Server created succesfully. Starting server");
        startServer();
    }

    private void startServer() {
        try {
            logger.log("Creating socket");
            socket = new DatagramSocket(6869);
            byte[] datagramNoB = new byte[8];
            long datagramNo = 0;
            logger.log("Waiting for requests...");
            while(true) {
                DatagramPacket req = new DatagramPacket(datagramNoB, datagramNoB.length);
                socket.receive(req);
                datagramNo = Converter.byteArrayToLong(datagramNoB);
                if(datagramNo == -1) {
                    logger.log("Client requested server end. Server end");
                    break;
                }
                InetAddress clientAddress = req.getAddress();
                int clientPort = req.getPort();
                new ServerWorker(file, clientAddress, clientPort, datagramNo, logger, this).start();
            }
        } catch (Exception e) {
            System.out.println("Error listening");
            e.printStackTrace();
        }

    }

    public synchronized int incrementNumSW() {
        currentServerWorkers++;
        return currentServerWorkers;
    }
    
    public synchronized int reduceNumSW() {
        currentServerWorkers--;
        return currentServerWorkers;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter 1 for 100MB file or 2 for 250MB file: ");
        int fileType = sc.nextInt();
        if(fileType < 1 || fileType > 2) {
            System.out.println("Invalid input. Terminating process");
        }
        new Server(fileType);
        sc.close();
    }
}
