package snowson.ife.com.ifeapplication.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import snowson.ife.com.ifeapplication.R;

public class VideoDialog extends Dialog implements
		View.OnClickListener {

	private View mDialogView;

	private LinearLayout linBtnsLayout;
	private TextView tvTitle;
	private Button btnSure;
	private Button btnCancel;
	private Button btnDismiss;
	private TextView tvContent;

	private volatile static VideoDialog instance;
	
	 public interface OnSureListener {
			public void doSomeThings(View v);
		}

	 private OnSureListener mListener;

	public VideoDialog(Context context) {
		super(context, R.style.dialog);
		init(context);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WindowManager.LayoutParams params = getWindow().getAttributes();
		params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
		getWindow().setAttributes(
				(WindowManager.LayoutParams) params);

		this.setCanceledOnTouchOutside(false);

	}

	public static VideoDialog getInstance(Context context) {

		instance = new VideoDialog(context);
		return instance;

	}

	private void init(Context context) {

		mDialogView = View.inflate(context, R.layout.dialog, null);
		tvContent = (TextView) mDialogView.findViewById(R.id.tv_content);
		linBtnsLayout = (LinearLayout) mDialogView.findViewById(R.id.lin_btns);
		tvTitle = (TextView) mDialogView.findViewById(R.id.tv_title);
		btnSure = (Button) mDialogView.findViewById(R.id.btn_sure);
		btnCancel = (Button) mDialogView.findViewById(R.id.btn_cancel);
		btnDismiss = (Button) mDialogView.findViewById(R.id.btn_dismiss);
		setContentView(mDialogView);

		btnSure.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		btnDismiss.setOnClickListener(this);

	}

	/** 切换视图 */
	private void toggleView(boolean showBtns) {
		if (showBtns) {
			linBtnsLayout.setVisibility(View.VISIBLE);
			btnDismiss.setVisibility(View.GONE);
		} else {
			linBtnsLayout.setVisibility(View.GONE);
			btnDismiss.setVisibility(View.VISIBLE);
		}

	}

	public VideoDialog withMessage(boolean showBtns, CharSequence msg) {
		toggleView(showBtns);
		tvContent.setText(msg);
		return this;
	}
	
	public VideoDialog setmListener(OnSureListener mListener) {
		this.mListener = mListener;
		return this;
	}
	

	@Override
	public void onClick(View v) {
		
        if (null != mListener) {
            mListener.doSomeThings(v);
        }
        this.dismiss();
	}

    public Button getButton(int resId) {
        Button button = null;
        switch (resId) {
            case R.id.btn_sure:
                if (btnSure == null) {
                    btnSure = (Button) findViewById(R.id.btn_sure);
                    btnSure.setOnClickListener(this);
                }
                button = btnSure;
                break;
            case R.id.btn_cancel:
                if (btnCancel == null) {
                    btnCancel = (Button) findViewById(R.id.btn_cancel);
                    btnCancel.setOnClickListener(this);
                }
                button = btnCancel;
                break;
            case R.id.btn_dismiss:
                if (btnDismiss == null) {
                    btnDismiss = (Button) findViewById(R.id.btn_dismiss);
                    btnDismiss.setOnClickListener(this);
                }
                button = btnDismiss;
                break;

            default:
                break;
        }
        
        return button;
    }

}
