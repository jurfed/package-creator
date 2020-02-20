package com.octavianonline.packagepreparer.model;
import lombok.Data;

import java.io.File;


@Data
public class ApplicationModel implements IApplicationModel {

    private File workDirectory;

    @Override
    public File getWorkDirectory() {
        return workDirectory;
    }

    @Override
    public void setWorkDirectory(File workDirectory) {
        this.workDirectory = workDirectory;
    }

}
