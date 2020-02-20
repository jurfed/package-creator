package com.octavianonline.packagepreparer.view.main;

import com.octavianonline.packagepreparer.view.common.IPresenter;

import java.io.File;

public interface IMainPresenter extends IPresenter<IMainView> {
    void openAction();

    void openManifest();

    void setWorkingDirectory(String path);



}
