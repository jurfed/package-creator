package com.octavianonline.packagepreparer.service;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface ManifestService {
    String getGson();
    void checksum(String packagePath, TextField packageNameText, TextField versionText, TextField descriptionText, ToggleGroup toggleGroup, TextField manufacturerText, TextField path, ListView dependencyListView) throws IOException, NoSuchAlgorithmException;
}
