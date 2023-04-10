package tcp.servidor;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CyclicBarrier;

import utils.FileEncoder;
import utils.Logger;

public class ServerWorker extends Thread {
    private File file;
    private String fileHash;
    private Socket socket;
    private CyclicBarrier cb;
    private int id;
    private Logger logger;
    
    public ServerWorker(int id, File file, String fileHash, Socket socket, CyclicBarrier cb, Logger logger) {
        this.file = file;
        this.fileHash = fileHash;
        this.socket = socket;
        this.cb = cb;
        this.id = id;
        this.logger = logger;
        log("Started");
    }

    @Override
    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            String readyLine = reader.readLine();
            if(readyLine != "ready") {
                writer.println("Unable to understand request. Closing connection");
                log("Client not ready. Closing connection");
                socket.close();
                return;
            }
            log("Client ready. There are: " + cb.getNumberWaiting() + 1 + " sw currently waiting");
            writer.println("ack");
            cb.await();
            log("Sending file hash: " + fileHash);
            writer.println(fileHash);
            log("Starting file transmition");
            String fileStr = FileEncoder.encodeFile(Files.readAllBytes(Paths.get(file.getPath())));
            writer.println(fileStr);
            log("Finished file transmition");
            log("Sending EOT");
            writer.println("EOT");
            log("EOT sent. Clossing connection");
            socket.close();
            log("Connection closed. Thread end");
        } catch(Exception e) {
            log("Error in server worker. StackTrace: " + e.getStackTrace().toString());
            System.out.println("Error in server worker.");
            e.printStackTrace();
        }
    }

    private void log(String msg) {
        String logMsg = "ServerWorker(" + id + "): " + msg;
        this.logger.log(logMsg);
    }
}
