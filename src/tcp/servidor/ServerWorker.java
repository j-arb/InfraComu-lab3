package tcp.servidor;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CyclicBarrier;

import utils.Logger;

public class ServerWorker extends Thread {
    private File file;
    private String fileHash;
    private Socket socket;
    private CyclicBarrier startCb;
    private CyclicBarrier endCb;
    private int id;
    private Logger logger;
    
    public ServerWorker(
        int id,
        File file,
        String fileHash,
        Socket socket,
        CyclicBarrier startCb,
        CyclicBarrier endCb,
        Logger logger)
    {
        this.file = file;
        this.fileHash = fileHash;
        this.socket = socket;
        this.startCb = startCb;
        this.id = id;
        this.logger = logger;
        this.endCb = endCb;
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
            if(!readyLine.equals("ready")) {
                writer.println("Unable to understand request. Closing connection");
                log("Client not ready. Closing connection. Recived: " + readyLine);
                socket.close();
                return;
            }
            log("Client ready. There are: " + (startCb.getNumberWaiting() + 1) + " ServerWorkers currently waiting");
            writer.println("ack");
            startCb.await();
            log("Sending file hash: " + fileHash);
            writer.println(fileHash);
            log("Sending file size");
            writer.println(file.length());
            log("Starting file transmition");
            sendFile(file);
            log("Finished file transmition");
            socket.close();
            log("Connection closed.");
            endCb.await();
            log("Thread end");
        } catch(Exception e) {
            log("Error in server worker. StackTrace: " + e.getStackTrace().toString());
            System.out.println("Error in server worker.");
            e.printStackTrace();
        }
    }

    private void sendFile(File file) throws Exception {
        int bytes = 0;
        FileInputStream fileInputStream = new FileInputStream(file);
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        // break file into chunks
        byte[] buffer = new byte[4*1024];
        while ((bytes=fileInputStream.read(buffer))!=-1){
            dos.write(buffer,0,bytes);
            dos.flush();
        }
        fileInputStream.close();
    }

    private void log(String msg) {
        String logMsg = "ServerWorker(" + id + "): " + msg;
        this.logger.log(logMsg);
    }
}
