package com.gfq.gdatabind;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BindingCustomAttribute {
    private static final String TAG="BindingCustomAttribute";
    @BindingAdapter({"imageUrl"})
    public static void loadImage(ImageView view, String url) {
        Glide.with(view.getContext()).load(url).into(view);
    }

    @BindingAdapter({"imageUrl", "corner"})
    public static void loadImage(ImageView view, String url, int dpCorner) {
        RoundedCorners roundedCorners = new RoundedCorners(dp2px(dpCorner));
        Glide.with(view.getContext()).load(url).apply(RequestOptions.bitmapTransform(roundedCorners)).into(view);
    }

    @BindingAdapter({"imageUrl", "corner", "overrideW", "overrideH"})
    public static void loadImage(ImageView view, String url, int dpCorner, int w, int h) {
        RoundedCorners roundedCorners = new RoundedCorners(dp2px(dpCorner));
        RequestOptions options = RequestOptions.bitmapTransform(roundedCorners).override(w, h);
        Glide.with(view.getContext()).load(url).apply(options).into(view);
    }

    //---------------------

    @BindingAdapter({"imageUrl", "placeHolder"})
    public static void loadImage(ImageView view, String url, Drawable holderDrawable) {
        Glide.with(view.getContext()).load(url).placeholder(holderDrawable).into(view);
    }

    @BindingAdapter({"imageUrl", "placeHolder", "corner"})
    public static void loadImage(ImageView view, String url, Drawable holderDrawable, int dpCorner) {
        RoundedCorners roundedCorners = new RoundedCorners(dp2px(dpCorner));
        Glide.with(view.getContext()).load(url).placeholder(holderDrawable).apply(RequestOptions.bitmapTransform(roundedCorners)).into(view);
    }

    @BindingAdapter({"imageUrl", "placeHolder", "corner", "overrideW", "overrideH"})
    public static void loadImage(ImageView view, String url, Drawable holderDrawable, int dpCorner, int w, int h) {
        RoundedCorners roundedCorners = new RoundedCorners(dp2px(dpCorner));
        RequestOptions options = RequestOptions.bitmapTransform(roundedCorners).override(w, h);
        Glide.with(view.getContext()).load(url).placeholder(holderDrawable).apply(options).into(view);
    }

    //---------------------

    @BindingAdapter({"imageUrl", "placeHolder", "error"})
    public static void loadImage(ImageView view, String url, Drawable holderDrawable, Drawable errorDrawable) {
        Glide.with(view.getContext()).load(url).placeholder(holderDrawable).error(errorDrawable).into(view);
    }

    @BindingAdapter({"imageUrl", "placeHolder", "error", "corner"})
    public static void loadImage(ImageView view, String url, Drawable holderDrawable, Drawable errorDrawable, int dpCorner) {
        RoundedCorners roundedCorners = new RoundedCorners(dp2px(dpCorner));
        RequestOptions options = RequestOptions.bitmapTransform(roundedCorners);
        Glide.with(view.getContext()).load(url).placeholder(holderDrawable).error(errorDrawable).apply(options).into(view);
    }

    @BindingAdapter({"imageUrl", "placeHolder", "error", "corner", "overrideW", "overrideH"})
    public static void loadImage(ImageView view, String url, Drawable holderDrawable, Drawable errorDrawable, int dpCorner, int w, int h) {
        RoundedCorners roundedCorners = new RoundedCorners(dp2px(dpCorner));
        RequestOptions options = RequestOptions.bitmapTransform(roundedCorners).override(w, h);
        Glide.with(view.getContext()).load(url).placeholder(holderDrawable).error(errorDrawable).apply(options).into(view);
    }

    //---------------------

    @BindingAdapter({"timeText","format"})
    public static void setTimeText(TextView view, long timeStamp,String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.CHINA);
        String time = simpleDateFormat.format(new Date(timeStamp));
        view.setText(time);
        Log.d(TAG, "timeText format time = "+time);
    }


    public static int dp2px(float dpValue) {
        return (int) (0.5f + dpValue * Resources.getSystem().getDisplayMetrics().density);
    }
}
