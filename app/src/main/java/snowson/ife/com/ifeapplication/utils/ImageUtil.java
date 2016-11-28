package snowson.ife.com.ifeapplication.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.fairlink.common.PhotoManager;

import java.util.Map;

import snowson.ife.com.ifeapplication.R;
import snowson.ife.com.ifeapplication.application.IFEApplication;

public class ImageUtil {

    public static final short SMALL = 1;
    public static final short MID = 2;
    public static final short BIG = 3;
    public static boolean isPortrait = false;
    private static Bitmap imgPortrait,imgLandscape;

    public static class ImageViewHolder {
        public ImageView imageView;
    }

    static Map<ImageView, ImageViewHolder> map;

    private static int getImageSize(short decodeImageSize) {
        switch (decodeImageSize) {
        case SMALL:
            return 200;

        case MID:
            return 400;

        case BIG:
            return 1280;
        }
        return (Integer) null;
    }

    private static int getDefaultImageId(short decodeImageSize) {
        switch (decodeImageSize) {
        case SMALL:
            return R.drawable.default_400;

        case MID:
            return R.drawable.default_400;

        case BIG:
            return R.drawable.default_800;
        }
        return (Integer) null;
    }
    
    private static Bitmap getDefaultBitmap(){
        Point screen = new Point();
        screen = getScreenSize();
        LinearLayout.LayoutParams Lparams =
                        new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.MATCH_PARENT);
        Lparams.gravity = Gravity.CENTER;
        LinearLayout ll = new LinearLayout(IFEApplication.getInstance());
        ll.setBackgroundColor(Color.LTGRAY);
        ImageView iv = new ImageView(IFEApplication.getInstance());
        LayoutParams params = iv.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }
        params.width = screen.x;
        params.height = screen.y;
        iv.setLayoutParams(params);
        iv.setImageResource(R.drawable.default_800);
        ll.addView(iv);
        Bitmap result = convertViewToBitmap(ll);
        iv = null;
        ll = null;
        return result;
       
    }
    
    

    private static void downloadImage(String imgUrl, final ImageView imgView, final ProgressBar progressBar,
            short imageSize) {
        final short size = imageSize;
        PhotoManager.getInstance().downloadImage(imgUrl, new PhotoManager.PhotoDownloadCallback() {
            @Override
            public void onPhotoDownloadError(String url, String path) {
            }

            @Override
            public void onPhotoDownload(String url, String path) {
                PhotoManager.decodePhotoAsync(path, getImageSize(size), getImageSize(size),
                        new PhotoManager.PhotoDecodedCallback() {

                            @Override
                            public void onPhotoDecoded(String path, Bitmap bitmap) {
                                if (bitmap != null && imgView != null) {
                                    imgView.setScaleType(ScaleType.FIT_XY);
                                    imgView.setImageBitmap(bitmap);
                                    if (progressBar != null) {
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }

                            }
                        });
            }
            
        });
    }

    private static void downloadImageHolder(String imgUrl, final ImageViewHolder imgHolder,
            final ProgressBar progressBar, short imageSize) {
        final short size = imageSize;
        PhotoManager.getInstance().downloadImage(imgUrl, new PhotoManager.PhotoDownloadCallback() {

            @Override
            public void onPhotoDownloadError(String url, String path) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onPhotoDownload(String url, String path) {

                PhotoManager.decodePhotoAsync(path, getImageSize(size), getImageSize(size),
                        new PhotoManager.PhotoDecodedCallback() {

                            @Override
                            public void onPhotoDecoded(String path, Bitmap bitmap) {
                                if (bitmap != null && imgHolder != null && imgHolder.imageView != null) {
                                    imgHolder.imageView.setScaleType(ScaleType.FIT_XY);
                                    imgHolder.imageView.setImageBitmap(bitmap);
                                    if (progressBar != null) {
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }

                            }
                        });

            }
        });
    }

    private static void setExistImage(String imgPath, final ImageView imgView, final ProgressBar progressBar,
            short imageSize) {
        final short size = imageSize;
        imgView.setTag(imgPath);//加入tag防止ImageView复用时错误加载
        PhotoManager.decodePhotoAsync(imgPath, getImageSize(size), getImageSize(size),
                new PhotoManager.PhotoDecodedCallback() {

                    @Override
                    public void onPhotoDecoded(String path, Bitmap bitmap) {
                    	//Log.v("IMG", imgView.getTag().equals(path)+"@"+path+"==>"+bitmap.toString());
                        if (bitmap != null && imgView != null && imgView.getTag().equals(path)) {
                            imgView.setScaleType(ScaleType.FIT_XY);
                            imgView.setImageBitmap(bitmap);
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                        }

                    }
                });
    }

    public static void setImageHolder(String imgUrl, short decodeImageSize, ImageViewHolder imageHolder,
            ProgressBar spinner) {

        if (spinner != null) {
            spinner.setVisibility(View.VISIBLE);
        }

        if (imageHolder != null && imageHolder.imageView != null) {
            imageHolder.imageView.setScaleType(ScaleType.FIT_CENTER);
            if (decodeImageSize != BIG) {
                imageHolder.imageView.setImageResource(getDefaultImageId(decodeImageSize));
            }else {
                if (isPortrait) {
                    if (imgPortrait == null) {
                        imgPortrait = getDefaultBitmap();
                    }
                    imageHolder.imageView.setImageBitmap(imgPortrait);
                }else {
                    if (imgLandscape == null) {
                        imgLandscape = getDefaultBitmap();
                    }
                    imageHolder.imageView.setImageBitmap(imgLandscape);
                }
            }
            
        }
        
        String imgPath = PhotoManager.getInstance().getImageFile(imgUrl);
        if (imgPath == null) {
            downloadImageHolder(imgUrl, imageHolder, spinner, decodeImageSize);
        } else {
            setExistImage(imgPath, imageHolder.imageView, spinner, decodeImageSize);
        }

    }


    public static void setImageView(String imgUrl, short decodeImageSize, ImageView imageView, ProgressBar spinner) {

        if (spinner != null) {
            spinner.setVisibility(View.VISIBLE);
        }
        
        if (imageView != null) {
            imageView.setScaleType(ScaleType.FIT_CENTER);
            imageView.setImageResource(getDefaultImageId(decodeImageSize));
        }
        
        String imgPath = PhotoManager.getInstance().getImageFile(imgUrl);
        if (imgPath == null) {
            downloadImage(imgUrl, imageView, spinner, decodeImageSize);
        } else {
            setExistImage(imgPath, imageView, spinner, decodeImageSize);
        }
        
    }
    
    public static Point getScreenSize() {
        DisplayMetrics displayMetrics = IFEApplication.getInstance().getResources().getDisplayMetrics();
        Point point = new Point();
        point.x = displayMetrics.widthPixels;
        point.y = displayMetrics.heightPixels;
        
        return point;
    }
    
    public static Bitmap convertViewToBitmap(View view){
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();

        return view.getDrawingCache();
  }
}
