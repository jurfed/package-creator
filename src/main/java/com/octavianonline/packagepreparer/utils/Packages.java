package com.octavianonline.packagepreparer.utils;


import com.google.gson.Gson;
import com.octavianonline.packagepreparer.model.ManifestModel;
import com.octavianonline.packagepreparer.view.main.MainScreen;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Packages {
    private final static String PACKAGES = "packages";
    private final static String OS = System.getProperty("os.name").toLowerCase();
    private String packagesDirectory;

    public void checkPackages() throws IOException {
        String currentDirectory = new java.io.File(".").getCanonicalPath();
        String pathDelimiter;
        if (OS.contains("win")) {
            pathDelimiter = "\\";
        } else {
            pathDelimiter = "/";
        }
        packagesDirectory = currentDirectory + pathDelimiter + PACKAGES;
        File f = new File(currentDirectory);
        File[] matchingFiles = f.listFiles();
        boolean directoryCreated = false;
        for (File file : matchingFiles) {
            if (file.isDirectory() && file.getName().equals(PACKAGES)) {
                directoryCreated = true;
                break;
            }

        }
        if (!directoryCreated) {
            new File(packagesDirectory).mkdir();

            File manufFile = new File(packagesDirectory + pathDelimiter + "manufacturer.list");
            manufFile.createNewFile();

//            new File(packagesDirectory + pathDelimiter + "manufacturer.list").createNewFile();

            try(FileWriter fw = new FileWriter(manufFile, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.println("Octavian\n" +
                        "INEX");
            } catch (IOException e) {
                //exception handling left as an exercise for the reader
            }

            File depend = new File(packagesDirectory + pathDelimiter + "dependencies.list");
            depend.createNewFile();

            try(FileWriter fw = new FileWriter(depend, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.println("EGM_core");
            } catch (IOException e) {
                //exception handling left as an exercise for the reader
            }

/*            URL dependRes = MainScreen.class.getClassLoader().getResource("dependencies.list");


            File source = new File(dependRes.getFile());
            File dest = new File("packagesDirectory"+pathDelimiter+"dependencies.list");
            try {
                FileUtils.copyDirectory(source, dest);
            } catch (IOException e) {
                e.printStackTrace();
            }*/

        }
    }

    public List<ManifestModel> createManifestModelList() {

        File f = new File(packagesDirectory);
        File[] matchingFiles = f.listFiles();
        List<ManifestModel> manifests = Arrays.stream(matchingFiles).filter(file -> file.getName().contains(".json")).map(file -> {

            Gson gson = new Gson();
            String str = "";
            try {
                str = FileUtils.readFileToString(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ManifestModel model = gson.fromJson(str, ManifestModel.class);
            return model;
        }).collect(Collectors.toList());
        return manifests;
    }



    public String getPackagesDirectory() {
        return packagesDirectory;
    }
}
