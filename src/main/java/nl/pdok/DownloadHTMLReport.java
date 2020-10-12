package nl.pdok;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DownloadHTMLReport {
    public static void download(String targetUrl, String filePath) {

        URL urlObj = null;
        ReadableByteChannel rbcObj = null;
        FileOutputStream fOutStream  = null;

        // Checking If The File Exists At The Specified Location Or Not
        Path filePathObj = Paths.get(filePath);
        boolean fileExists = Files.exists(filePathObj);
        if(!fileExists) {
            try {
                urlObj = new URL(targetUrl);
                rbcObj = Channels.newChannel(urlObj.openStream());
                fOutStream = new FileOutputStream(filePath);

                fOutStream.getChannel().transferFrom(rbcObj, 0, Long.MAX_VALUE);
                System.out.println("! File Successfully Downloaded From The Url !");
            } catch (IOException ioExObj) {
                System.out.println("Problem Occured While Downloading The File= " + ioExObj.getMessage());
            } finally {
                try {
                    if(fOutStream != null){
                        fOutStream.close();
                    }
                    if(rbcObj != null) {
                        rbcObj.close();
                    }
                } catch (IOException ioExObj) {
                    System.out.println("Problem Occured While Closing The Object= " + ioExObj.getMessage());
                }
            }
        } else {
            System.out.println("File already Present! Please Check!");
        }
    }

    public static void main(String[] args) {
        download(args[0], args[1]);
    }

}
