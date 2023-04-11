package tcp.cliente;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.Scanner;

import utils.FileChecker;
import utils.Logger;

public class Client extends Thread {

    private static final String CLIENT_FILES_DIR = "files/client";
    private static final String SERVER_ADDRESS = "192.168.182.138" /** "192.168.182.138"*/;
    private static final int SERVER_PORT = 6868;

    private Socket socket;
    private Logger logger;
    private int numConnections;
    private int id;
    private long fileLength;

    public Client(int id, int numConnections) {
        this.id = id;
        this.numConnections = numConnections;
        this.logger = new Logger("TCPClient " + id + " of " + numConnections);
        logger.log("Client created");
    }

    @Override
    public void run() {
        try {
            logger.log("Starting client");
            this.socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            logger.log("Socket created");
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            logger.log("Sending ready");
            writer.println("ready");
            logger.log("Ready sent");

            String ackMsg = reader.readLine();
            if(!ackMsg.equals("ack")) {
                logger.log("Server did not acknowledged. Response: " + ackMsg);
                return;
            }
            logger.log("Server acked 'ready' msg. Waiting for hash");
            String hash = reader.readLine();
            logger.log("Recived hash: " + hash + ". Waiting for file length");
            fileLength = Long.parseLong(reader.readLine());
            logger.log("Received file lenght. Waiting for file transfer");
            String filePath = Paths.get(CLIENT_FILES_DIR, "ClienteTCP " + id + " de " + numConnections +
                ".bin").toString();
            receiveFile(filePath);
            logger.log("File received and stored on " + filePath);
            if(!FileChecker.checkHash(hash, new File(filePath))) {
                logger.log("Hash does not match");
            } else {
                logger.log("Hash match");
            }
            logger.log("Clossing connection");
            socket.close();
            logger.log("Connection closed. Thread end");
        } catch (Exception e) {
            logger.log("Error working over socket for " + SERVER_ADDRESS + ":" + SERVER_PORT);
            e.printStackTrace();
        }
    }

    private void receiveFile(String fileName) throws Exception{
        int bytes = 0;
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        
        long size = this.fileLength;
        byte[] buffer = new byte[4*1024];
        while (size > 0 && (bytes = dis.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer,0,bytes);
            size -= bytes;      // read upto file size
        }
        fileOutputStream.close();
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the number of connections to stablish." +
        " (Make sure this number matches with the number of connections the server is expecting)");
        System.out.print(">> ");
        int numConections = sc.nextInt();
        for(int i = 1; i <= numConections; i ++) {
            new Client(i, numConections).start();
        }
        sc.close();
    }
    
}
