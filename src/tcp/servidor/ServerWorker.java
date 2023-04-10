package tcp.servidor;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CyclicBarrier;

public class ServerWorker extends Thread {
    private File file;
    private String fileHash;
    private Socket socket;
    private CyclicBarrier cb;
    
    public ServerWorker(File file, String fileHash, Socket socket, CyclicBarrier cb) {
        this.file = file;
        this.fileHash = fileHash;
        this.socket = socket;
        this.cb = cb;
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
                System.out.println("Client not ready. Closing connection");
                socket.close();
                return;
            }
            System.out.println("Client ready. CB#: " + cb.getNumberWaiting() + 1);
            writer.println("ack");
            cb.await();
            writer.println(fileHash);



        } catch(Exception e) {
            System.out.println("Error in server worker");
            e.printStackTrace();
        }
    }
}
