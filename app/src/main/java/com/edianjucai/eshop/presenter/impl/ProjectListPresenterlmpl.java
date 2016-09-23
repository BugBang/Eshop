package com.edianjucai.eshop.presenter.impl;

import com.edianjucai.eshop.model.entity.ProjectListModel;
import com.edianjucai.eshop.model.impl.ProjectListModelmpl;
import com.edianjucai.eshop.model.mode.ProjectListMode;
import com.edianjucai.eshop.presenter.usb.OnProjectListListener;
import com.edianjucai.eshop.presenter.usb.ProjectListPresenter;
import com.edianjucai.eshop.ui.view.ProjectListView;

/**
 * Created by user on 2016-09-23.
 */
public class ProjectListPresenterlmpl implements ProjectListPresenter,OnProjectListListener {

    private ProjectListView mProjectListView;
    private ProjectListMode mProjectListMode;

    public ProjectListPresenterlmpl(ProjectListView projectListView){
        this.mProjectListView = projectListView;
        mProjectListMode = new ProjectListModelmpl();
    }

    @Override
    public void setProjectListData(ProjectListModel projectListModel) {
        mProjectListView.setProjectListData(projectListModel);
    }

    @Override
    public void startRequest() {
        mProjectListView.startRequest();
    }

    @Override
    public void finishRequest() {
        mProjectListView.finishRequest();
    }

    @Override
    public void successRequest() {
        mProjectListView.successRequest();
    }

    @Override
    public void failRequest(String showErr) {
        mProjectListView.failRequest(showErr);
    }

    @Override
    public void requestProjectListData() {
        mProjectListMode.requestProjectListData(this);
    }
}
