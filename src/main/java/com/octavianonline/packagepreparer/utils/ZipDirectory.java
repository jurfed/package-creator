package com.octavianonline.packagepreparer.utils;

import com.google.common.eventbus.EventBus;
import com.octavianonline.packagepreparer.view.main.MainScreen;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipDirectory extends Task {
    private String sourceFile;
    private String zipName;
    private TextArea jsonText;
    private boolean breakZip;


    public ZipDirectory(String packagesDirectory, String directory, String zipName, ProgressBar progressId, TextArea jsonText, TextField pathText) {
        System.out.println();

        //if(pathText.getText().length()>1){
            this.sourceFile = directory + "/";
            this.zipName = new File(packagesDirectory) + "/" + zipName;
//        }else{
//            this.sourceFile = directory;
//            this.zipName = new File(packagesDirectory) + "/" + zipName;
//        }


        this.jsonText = jsonText;
        b = false;
        breakZip = false;

    }

    public void setBreakZip(boolean breakZip) {
        this.breakZip = breakZip;
    }

    private static int filesCount = 0;

    @Override
    protected Object call() throws Exception {
        filesCount = 0;
        currentFileNumber = 0;
        FileOutputStream fos = new FileOutputStream(zipName);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(sourceFile);
        countFiles(fileToZip);
        zipFile(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
        fos.close();
        this.updateProgress(filesCount, filesCount);
        return null;
    }

    private void countFiles(File fileToZip) {
        File[] files = fileToZip.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                filesCount++;
            } else {
                countFiles(file);
            }
        }

    }


    private static boolean b = false;
    private static int currentFileNumber = 0;

    String textFile;

    private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException, InterruptedException {
        if (!breakZip) {
            textFile = fileName;

/*            if (fileToZip.isHidden()) {
                return;
            }*/


            if (fileToZip.isDirectory()) {
                if (fileName.endsWith("/")) {
                    zipOut.putNextEntry(new ZipEntry(fileName));
                    zipOut.closeEntry();
                } else {
                    if (b) {
//                        zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                        zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                        zipOut.closeEntry();
                    } else {
                        fileName = "";
                        b = true;
                    }

                }
                File[] children = fileToZip.listFiles();
                for (File childFile : children) {
                    if(fileName.length()>0){
                        zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
                    }else{
                        zipFile(childFile, childFile.getName(), zipOut);
                    }
                }
                return;
            }else{
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {


                        String text = jsonText.getText();
                        jsonText.appendText(textFile + "\n");
                    }
                });
            }
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileName);
//        eventBus.post(new FileZipEvent(fileName));
            Thread.sleep(20);
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
            this.updateProgress(currentFileNumber, filesCount);
            currentFileNumber++;
        }
    }


}