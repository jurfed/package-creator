package com.octavianonline.packagepreparer.service;

import com.octavianonline.packagepreparer.model.ManifestModel;
import javafx.scene.control.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class ManifestServiceImpl implements ManifestService {
    ManifestModel manifestModel;
    private final static String OS = System.getProperty("os.name").toLowerCase();

    ManifestServiceImpl() {
        manifestModel = new ManifestModel();
    }

    @Override
    public void checksum(String packagePath, TextField packageNameText, TextField versionText, TextField descriptionText, ToggleGroup toggleGroup, TextField manufacturerText, TextField path, ListView dependencyListView) throws IOException, NoSuchAlgorithmException {
        String pathDelimiter;
        if (OS.contains("win")) {
            pathDelimiter = "\\";
        } else {
            pathDelimiter = "/";
        }
        String version = versionText.getText();
        if (version.length() > 0) {
            version = "-" + version;
        }
        String zipFilePath = packagePath + pathDelimiter + packageNameText.getText() + version + ".zip";
        String res = sha1Code(zipFilePath);
        manifestModel.setChecksum(res.toLowerCase());
        manifestModel.setPackageName(packageNameText.getText());
        manifestModel.setVersion(versionText.getText());
        manifestModel.setDescription(descriptionText.getText());
        manifestModel.setDependencies(dependencyListView.getItems());
        RadioButton selectedButton = (RadioButton) toggleGroup.getSelectedToggle();
        manifestModel.setType(selectedButton.getText());
        manifestModel.setManufacturer(manufacturerText.getText());
        manifestModel.setFileName(packageNameText.getText() + version + ".zip");

        String endPath = path.getText();
        if (endPath.length() > 1 && endPath.substring(endPath.length() - 1, endPath.length()).equals("/")) {
            path.setText(endPath = endPath.substring(0, endPath.length() - 1));
        }
        manifestModel.setPath(endPath);

        String str = manifestModel.toString();
        BufferedWriter writer = new BufferedWriter(new FileWriter(packagePath + pathDelimiter + packageNameText.getText() + version + ".json"));
        writer.write(str);

        writer.close();
        System.out.println(res);
    }


    public String sha1Code(String filePath) throws IOException, NoSuchAlgorithmException {
        FileInputStream fileInputStream = new FileInputStream(filePath);
        setFileSize(fileInputStream);
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        DigestInputStream digestInputStream = new DigestInputStream(fileInputStream, digest);
        byte[] bytes = new byte[1024];
        // read all file content
        while (digestInputStream.read(bytes) > 0) ;
        byte[] resultByteArry = digest.digest();
        return bytesToHexString(resultByteArry);
    }

    /**
     * Convert a array of byte to hex String. <br/>
     * Each byte is covert a two character of hex String. That is <br/>
     * if byte of int is less than 16, then the hex String will append <br/>
     * a character of '0'.
     *
     * @param bytes array of byte
     * @return hex String represent the array of byte
     */
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            int value = b & 0xFF;
            if (value < 16) {
                sb.append("0");
            }
            sb.append(Integer.toHexString(value).toUpperCase());
        }
        return sb.toString();
    }

    private void setFileSize(FileInputStream fileInputStream) throws IOException {
        Long size = fileInputStream.getChannel().size();
        manifestModel.setSize(size);
    }

    @Override
    public String getGson() {
        return manifestModel.toString();
    }

}
