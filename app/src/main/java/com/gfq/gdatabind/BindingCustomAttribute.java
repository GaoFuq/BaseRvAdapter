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

    @BindingAdapter(value = {"imageUrl", "placeHolder", "error", "radius", "overrideW", "overrideH"},requireAll = false)
    public static void loadImage(ImageView view, String url, Drawable holderDrawable, Drawable errorDrawable, int dpRadius, int w, int h) {
        RoundedCorners roundedCorners = new RoundedCorners(dp2px(dpRadius));
        RequestOptions options = RequestOptions.bitmapTransform(roundedCorners).override(w, h);
        Glide.with(view.getContext()).load(url).placeholder(holderDrawable).error(errorDrawable).apply(options).into(view);
    }


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
