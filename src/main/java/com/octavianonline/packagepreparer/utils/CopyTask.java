package com.octavianonline.packagepreparer.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
 
import javafx.concurrent.Task;
 
// Copy all file in C:/Windows
public class CopyTask extends Task{
 
    @Override
    protected List call() throws Exception {
 
        File dir = new File("/home/develop/OGA_RumblingRun_Assets/");
        File[] files = dir.listFiles();
        int count = files.length;
 
        List<File> copied = new ArrayList<File>();


        for(int i=0; i<30; i++){
            this.updateProgress(i, 30);
            this.copy(dir);
        }
        return copied;

    }
 
    private void copy(File file) throws Exception {
        this.updateMessage("Copying: " + file.getAbsolutePath());
        Thread.sleep(500);
    }
 
}