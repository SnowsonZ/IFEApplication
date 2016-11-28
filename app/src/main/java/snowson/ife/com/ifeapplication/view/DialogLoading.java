package snowson.ife.com.ifeapplication.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.fairlink.common.DownloadTask;
import com.fairlink.common.Logger;

import snowson.ife.com.ifeapplication.R;

/**
 * @ClassName ： LoadingDialog
 * @Description: 自定义加载框
 */

public class DialogLoading extends Dialog{
	
	static private Logger logger = new Logger(null, "DialogLoading");

	@Override
	public void show() {
		super.show();
		logger.debug("show");
	}

	@Override
	public void hide() {
		super.hide();
		logger.debug("hide");
	}

	private Context mContext;
	private LayoutInflater inflater;
	private LayoutParams lp;
	private TextView loadtext;
	private TextView progressText;
	private LoadingListener listener;
	private DownloadTask task;

	public interface LoadingListener {
		void onCancel();

	}

	public DialogLoading(Context context) {
		this(context, null);
	}

	public DialogLoading(Context context, LoadingListener l) {
		super(context, R.style.dialog_loading);

		this.mContext = context;
		listener = l;

		inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_loading, null);
		loadtext = (TextView) layout.findViewById(R.id.loading_text);
		progressText = (TextView) layout.findViewById(R.id.loading_progress);
		ImageView spaceshipImage = (ImageView) layout.findViewById(R.id.loading_img);

		// 加载动画
		Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(context, R.anim.loading_animation);
		// 使用ImageView显示动画
		spaceshipImage.startAnimation(hyperspaceJumpAnimation);

		setContentView(layout);
		setCancelable(false);

		// 设置window属性
		lp = getWindow().getAttributes();
		lp.gravity = Gravity.CENTER;
		lp.dimAmount = 0; // 去背景遮盖
		lp.alpha = 1.0f;
		getWindow().setAttributes(lp);

//        ApplicationManager.getInstence().setUpdateProgressListener(new UpdateProgressListener() {
//
//            @Override
//            public void updateProgress(int progress) {
//                if (progressText.getVisibility() == View.VISIBLE) {
//                    progressText.setText(progress+"%");
//                }
//            }
//        });

	}

	public void setLoadText(String content) {
		loadtext.setText(content);
	}

    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(listener != null){
				listener.onCancel();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
    
    public TextView getProgressView(){
        return progressText;
    }

}
