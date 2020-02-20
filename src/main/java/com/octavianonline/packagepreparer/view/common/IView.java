package com.octavianonline.packagepreparer.view.common;

public interface IView {
    Object getRoot();
    enum State {
        IDLE, WORKING, WAITING, FONT_CHOICE, FONT_CONFIG
    }
}
