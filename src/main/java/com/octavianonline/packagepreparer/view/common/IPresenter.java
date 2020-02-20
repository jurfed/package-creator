package com.octavianonline.packagepreparer.view.common;

public interface IPresenter<T extends IView> {

    void setView(T view);

}
