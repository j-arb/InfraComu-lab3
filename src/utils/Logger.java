package utils;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private File log;
    private static final String path = "logs/";

    /**
     * Creates logger
     * @param logInfo information used to name the log. For example "TCPServer" or "UDPClient"
     */
    public Logger(String logInfo) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
        String fileName = logInfo + "-" + formatter.format(date) + ".log";
        try {
            this.log = new File(path, fileName);
            log.createNewFile();
        } catch(Exception e) {
            System.out.println("Unable to create log for '" + log.getPath() + "'");
            e.printStackTrace();
        }
    }

    public synchronized void log(String msg) {
        try {
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            FileWriter mw = new FileWriter(log, true);
            String logMsg = "LOG " + formatter.format(date) + ": " + msg + "\n";
            mw.append(logMsg);
            mw.flush();
            mw.close();
        } catch(Exception e) {
            System.out.println("Unable to log on '" + log.getPath() + "'");
            e.printStackTrace();
        }
    }
}
