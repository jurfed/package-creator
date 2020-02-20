package com.octavianonline.packagepreparer.view.main;

import com.google.common.eventbus.EventBus;
import com.octavianonline.packagepreparer.model.IApplicationModel;
import com.octavianonline.packagepreparer.view.common.BasePresenter;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class MainPresenter extends BasePresenter<IMainView> implements IMainPresenter {

    private final IApplicationModel model;
    private final EventBus eventBus;


    public MainPresenter(IApplicationModel applicationModel, EventBus eventBus) {
        this.model = applicationModel;
        this.eventBus = eventBus;
    }

    @Override
    public void openAction() {
        this.view.showProjectFile(model.getWorkDirectory());
    }

    @Override
    public void openManifest() {
        this.view.showManifestFile(model.getWorkDirectory());
    }

    @Override
    public void setWorkingDirectory(String path) {
        this.model.setWorkDirectory(new File(path));
    }


}
