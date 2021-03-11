package com.paul623.android.imageaiclassification;

import org.litepal.crud.LitePalSupport;

/**
 * Created by Android Studio.
 * User: paul623
 * Date: 2021/3/11
 * Time: 14:56
 * Email:zhubaoluo@outlook.com
 */
public class ImageBean extends LitePalSupport {
    String path;
    String kindName;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getKindName() {
        return kindName;
    }

    public void setKindName(String kindName) {
        this.kindName = kindName;
    }
}
