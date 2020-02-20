package com.octavianonline.packagepreparer.view.main;

import com.octavianonline.packagepreparer.view.common.IView;

import java.io.File;

public interface IMainView extends IView {
    void showProjectFile(File workDirectory);
    void showManifestFile(File workDirectory);

}
