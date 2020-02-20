package com.octavianonline.packagepreparer.model;



import java.io.File;

public interface IApplicationModel {
    File getWorkDirectory();

    void setWorkDirectory(File file);

}
