package com.kivanov.diploma.model;

public class WebUrls {
    final static public String PROJECT = "project";
    final static public String HOME = "home";
    final static public String REDIRECT = "redirect";
    final static public String SYNC = "sync";
    final static public String UPLOAD = "upload";
    final static public String DOWNLOAD = "download";
    final static public String DELETE = "delete";
    final static public String DELETE_SOURCE = "delete-source";
    final static public String DELETE_SOURCE_FROM_NEW =  "/" + WebUrls.NEW + "/" + DELETE_SOURCE;
    final static public String NEW = "new";
    final static public String REGISTER = "register";
    final static public String SHOW = "show";
    final static public String OAUTH2 = "oauth2";
    final static public String YANDEX = "yandex";
    final static public String OAUTH2_YANDEX = OAUTH2 + "/" + YANDEX;
    final static public String DELETE_SOURCE_FROM_NEW_PROJECT =  "/" + PROJECT + "/" + NEW + "/" + DELETE_SOURCE;
    final static public String SHOW_PROJECT =  "/" + PROJECT + "/" + SHOW;
    final static public String SYNC_PROJECT =  "/" + PROJECT + "/" + SYNC;
    final static public String NEW_PROJECT =  "/" + PROJECT + "/" + NEW;
    final static public String UPLOAD_PROJECT =  "/" + PROJECT + "/" + UPLOAD;
    final static public String DOWNLOAD_PROJECT =  "/" + PROJECT + "/" + DOWNLOAD;
    final static public String DELETE_PROJECT =  "/" + PROJECT + "/" + DELETE;

}
