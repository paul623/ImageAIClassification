package com.paul623.android.imageaiclassification;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.AlbumLoader;

public class ImageLoader implements AlbumLoader {

    @Override
    public void load(ImageView imageView, AlbumFile albumFile) {
        load(imageView, albumFile.getPath());
    }

    @Override
    public void load(ImageView imageView, String url) {
        Glide.with(imageView.getContext())
                .load(ImageHelper.getBitmapByPath(imageView.getContext(), url))
                .error(R.drawable.icon_error_loading)
                .placeholder(R.drawable.icon_loading_image)
                .into(imageView);
    }

}
