package tcp.servidor;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CyclicBarrier;

import utils.FileChecker;


public class Server {
    private int numConections;
    private final String file100MB = "files/100MB.bin";
    private final String file250MB = "files/250MB.bin";
    private String fileHash;
    private File file;
    private CyclicBarrier cb;

    /**
     * Inicializa el servidor
     * @param numConections - Número de conexiones a esperar. Debe ser menor
     * o igual a 25.
     * @param file - 1 para enviar el archivo de 100MB o 2 para enviar el
     * de 250MB.
     */
    public Server(int numConections, int file) {
        if(numConections > 25) {
            System.out.println("Número inválido de conexiones. Debe ser menor a 25");
            return;
        } else if (file > 2 || file < 1) {
            System.out.println("File debe ser 1 o 2");
            return;
        }

        this.numConections = numConections;
        if(file == 1) {
            this.file = new File(file100MB);
        } else {
            this.file = new File(file250MB);
        }
        this.fileHash = FileChecker.generateHash(this.file);
        this.cb = new CyclicBarrier(numConections);
        startServer();
    }

    private void startServer() {
        try {
            while(true) {
                ServerSocket serverSocket = new ServerSocket(6868);
                Socket socket = serverSocket.accept();
                ServerWorker sw = new ServerWorker(file, fileHash, socket, cb);
                sw.start();
            }
        } catch (Exception e) {
            System.out.println("Error listening");
            e.printStackTrace();
        }

    }
}
