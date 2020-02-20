package com.octavianonline.packagepreparer.view.common;

import com.google.common.eventbus.EventBus;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import lombok.Data;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.ResourceBundle;

@Data
public abstract class BaseView implements IView, Initializable {

    @FXML
    protected Parent rootView;
    private final EventBus eventBus;
    private final IPresenter presenter;

    protected BaseView(EventBus eventBus, IPresenter presenter) {
        this.eventBus = eventBus;
        this.presenter = presenter;
        this.presenter.setView(this);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }



    @Override
    public Object getRoot() {
        return rootView;
    }
}
