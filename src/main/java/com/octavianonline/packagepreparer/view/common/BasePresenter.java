package com.octavianonline.packagepreparer.view.common;

public abstract class BasePresenter<T extends IView> implements IPresenter <T>{

    protected T view;

    @Override
    public void setView(T view) {
        this.view = view;
    }


}
