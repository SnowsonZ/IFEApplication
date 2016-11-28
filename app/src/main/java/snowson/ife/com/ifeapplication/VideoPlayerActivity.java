package snowson.ife.com.ifeapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.fairlink.common.Analytics;
import com.fairlink.common.AnalyticsType;
import com.fairlink.common.BaseHttpTask.HttpTaskCallback;
import com.fairlink.common.DownloadTask;
import com.fairlink.common.Logger;
import com.fairlink.common.NetworkRequestAPI;
import com.fairlink.common.PhotoManager;
import com.fairlink.common.PhotoManager.PhotoDownloadCallback;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.VideoSurfaceView;

import java.io.File;
import java.lang.ref.WeakReference;

import snowson.ife.com.ifeapplication.application.IFEApplication;
import snowson.ife.com.ifeapplication.bean.Ads;
import snowson.ife.com.ifeapplication.exoplayerwrapper.DashRendererBuilder;
import snowson.ife.com.ifeapplication.exoplayerwrapper.DefaultRendererBuilder;
import snowson.ife.com.ifeapplication.exoplayerwrapper.DemoPlayer;
import snowson.ife.com.ifeapplication.exoplayerwrapper.DemoPlayer.RendererBuilder;
import snowson.ife.com.ifeapplication.exoplayerwrapper.HlsRendererBuilder;
import snowson.ife.com.ifeapplication.exoplayerwrapper.UnsupportedDrmException;
import snowson.ife.com.ifeapplication.exoplayerwrapper.Util;
import snowson.ife.com.ifeapplication.request.AdsRequest;
import snowson.ife.com.ifeapplication.utils.ComUtil;
import snowson.ife.com.ifeapplication.view.DialogLoading;
import snowson.ife.com.ifeapplication.view.VideoDialog;
import snowson.ife.com.ifeapplication.view.VideoDialog.OnSureListener;

public class VideoPlayerActivity extends BaseActivity implements DemoPlayer.Listener, OnClickListener, HttpTaskCallback,
		NetworkRequestAPI, PhotoDownloadCallback {

	@SuppressLint("NewApi")
    private static class AdsVideoCallback implements HttpTaskCallback {
        private WeakReference<VideoPlayerActivity> activity;

        public AdsVideoCallback(VideoPlayerActivity activity) {
            this.activity = new WeakReference<VideoPlayerActivity>(activity);
        }


		@Override
        public void onGetResult(int requestType, Object result) {
            VideoPlayerActivity a = activity.get();
            if (a == null || a.isDestroyed()) {
                return;
            }
            a.setAdsVideo((Ads) result);
        }

        @Override
        public void onError(int requestType) {
            VideoPlayerActivity a = activity.get();
            if (a == null || a.isDestroyed()) {
                return;
            }
            a.setAdsVideo(null);
        }
    }

	@SuppressLint("NewApi")
    private static class AdsImageCallback implements HttpTaskCallback {
        private WeakReference<VideoPlayerActivity> activity;

        public AdsImageCallback(VideoPlayerActivity activity) {
            this.activity = new WeakReference<VideoPlayerActivity>(activity);
        }

		@Override
        public void onGetResult(int requestType, Object result) {
            VideoPlayerActivity a = activity.get();
            if (a == null || a.isDestroyed()) {
                return;
            }
            a.setAdsPicture((Ads) result);
        }

        @Override
        public void onError(int requestType) {
            VideoPlayerActivity a = activity.get();
            if (a == null || a.isDestroyed()) {
                return;
            }
            a.setAdsPicture(null);
        }
    }

    private Logger logger = new Logger(this, "");
    
    private static final String POSITIVE = "+1";
    private static final String NEGATIVE = "-1";
    private static final int ANIMATION_PERIOD = 3000;
    private static final int ANIMATION_AD_FADE_PERIOD = 500;
	private VideoSurfaceView mVideoView;
	private DemoPlayer mVideoPlayer;

	private View mVolumeBrightnessLayout;
	private ImageView mOperationBg;
	private ImageView mOperationPercent;
	private AudioManager mAudioManager;
	/** 最大声音 */
	private int mMaxVolume;
	/** 当前声音 */
	private int mVolume = -1;
	/** 当前亮度 */
	private float mBrightness = -1f;
	private GestureDetector mGestureDetector;

	private View controlView;
	private PopupWindow controler;
	private TextView durationTextView;
	private TextView playedTextView;
	private SeekBar durationSeekBar;
	private SeekBar blightSeekBar;
	int Max_Brightness = 100;
	float fBrightness = -1f;
	WindowManager.LayoutParams lp;
	private SeekBar volumeSeekBar;
	private ImageView mPlayed;
	private ImageView mAddGo;
	private ImageView mAddBack;
//	private ImageButton voteUpBtn;
//	private ImageButton voteDownBtn;
//	private FrameLayout voteUpBg;
//	private FrameLayout voteDownBg;
    private ImageButton closeBtn;
	private boolean isPaused;

	private String name; // 视频名称
	private String videoId; // 视频id
	private int videoType; // 视频类型
	private String typeName;//视频类型名
	private String videoImg; // 视频图片
	private String videoPath; // 视频地址
    private boolean isPlayAds; 
    private boolean canRating; //视频能否顶踩
    private String curUserRatingMsg; //当前用户的顶踩信息
    private int positiveCount; //顶计数
    private int negativeCount; //踩计数
    private String ratingMsg;
	private long mLastPosition = 0;
	private boolean isPlayingAd = false;
	private boolean isAdFinished = false;
	private boolean mShowing = false;
	private TextView mADTime;

	private final static int HIDE_MESSAGE = 1;
	private final static int UPDAT_TIME_MESSAGE = 2;
	private final static int PROGRESS_CHANGED = 3;
	private final static int VIDEO_DURATION = 4;
	private final static int TOUCH_LENGTH_X = 800;
	private final static int TOUCH_LENGTH_Y = 100;

	private static final int AD_TIME = 90;
	private DialogLoading diaLoading;

	private View mAD;
	private ImageView mADPic;

	private Ads videoAds;
	private String videoAdId;
	private Ads pictureAds;
	   
	private AdsRequest adsVideoRequest;
	private AdsRequest adsPicRequest;
	private boolean isPrintLog = false;
	
	private boolean isAdRecorded = false;
	private boolean isCompleteRecorded = false;
	private boolean isExists = false;

//	private void startAnimationForRating(){
//		ObjectAnimator voteUp = ObjectAnimator.ofFloat(voteUpBg, "alpha", 0).setDuration(ANIMATION_PERIOD);
//		voteUp.addListener(new AnimatorListenerAdapter(){
//			@Override
//			public void onAnimationEnd(Animator animation) {
//				voteUpBg.setVisibility(View.GONE);
//			}
//			@Override
//			public void onAnimationCancel(Animator animation) {
//				voteUpBg.setVisibility(View.GONE);
//			}
//
//		});
//		ObjectAnimator voteDown = ObjectAnimator.ofFloat(voteDownBg, "alpha", 0).setDuration(ANIMATION_PERIOD);
//		voteDown.addListener(new AnimatorListenerAdapter(){
//			@Override
//			public void onAnimationEnd(Animator animation) {
//				voteDownBg.setVisibility(View.GONE);
//			}
//			@Override
//			public void onAnimationCancel(Animator animation) {
//				voteDownBg.setVisibility(View.GONE);
//			}
//		});
//		voteUp.start();
//		voteDown.start();
//	}
	/* Handler信息传递，用来监听和实现播放视频区域的实时变化 */
	@SuppressLint("HandlerLeak")
	Handler _handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case VIDEO_DURATION:
				long duration = mVideoPlayer.getDuration();
				if (duration > 0) {

					if (isPlayingAd) {
						long position = mVideoPlayer.getCurrentPosition();
						mADTime.setVisibility(View.VISIBLE);
						updateLeftADtime(duration, position);

					} else {
						int time = (int)mVideoPlayer.getDuration();
						durationSeekBar.setMax(time);
						durationTextView.setText(ComUtil.Time(time));
						_handler.removeMessages(VIDEO_DURATION);
						_handler.sendEmptyMessage(PROGRESS_CHANGED);
					}

				} else {
					_handler.sendEmptyMessageDelayed(VIDEO_DURATION, 500);
				}

				break;

			case PROGRESS_CHANGED:

				int time = (int)mVideoPlayer.getCurrentPosition();
				if (time < 0) {
					time = 0;
				}
				mLastPosition = time;
				durationSeekBar.setProgress(time);
				playedTextView.setText(ComUtil.Time(time));

				sendEmptyMessageDelayed(PROGRESS_CHANGED, 500);
				break;

			case HIDE_MESSAGE:
				hide();
				break;

			case UPDAT_TIME_MESSAGE:
				updateLeftADtime(mVideoPlayer.getDuration(), mVideoPlayer.getCurrentPosition());
				break;

			}

			super.handleMessage(msg);
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.movie_player);

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		initData();
		initView();
		setVolum();
		setListener();
	
		
		
		mShowing = false;
		diaLoading = new DialogLoading(this, new DialogLoading.LoadingListener() {

			@Override
			public void onCancel() {
				VideoPlayerActivity.this.finish();
			}
		});
		diaLoading.show();

		mVideoView.getHolder().addCallback(new SurfaceHolder.Callback() {
			public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			}

			public void surfaceCreated(SurfaceHolder holder) {
				if (mVideoPlayer != null) {
					mVideoPlayer.setSurface(holder.getSurface());
				}
			}

			public void surfaceDestroyed(SurfaceHolder holder) {
				if (mVideoPlayer != null) {
					mVideoPlayer.blockingClearSurface();
				}
			}
		});
		isPaused = false;
		mLastPosition = IFEApplication.getInstance().getVideoPosition(videoPath);
	
        if (isPlayAds) {
            adsVideoRequest = new AdsRequest(new AdsVideoCallback(this), 2);
            adsVideoRequest.execute((String) null);
        } else {
            finishAd();
        }
	}

	@Override
	protected void onPause() {
		super.onPause();
		DownloadTask.resume();
		_handler.removeMessages(UPDAT_TIME_MESSAGE);
		if (!isPlayingAd) {
			mLastPosition = mVideoPlayer.getCurrentPosition();
			long duration = mVideoPlayer.getDuration();
			if (mLastPosition >= duration-500) {
				mLastPosition = 0;
			}
			IFEApplication.getInstance().setVideoPosition(videoPath, mLastPosition);
		}
		mVideoPlayer.getPlayerControl().pause();
		// mVideoView.suspend();

	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		if (!isPlayingAd) {
			mLastPosition = IFEApplication.getInstance().getVideoPosition(videoPath);
			mVideoPlayer.seekTo(mLastPosition);
		}
	}
   
    @Override
    protected void onResume() {
        super.onResume();
        isPrintLog = false;
    
        DownloadTask.suspend();
        mVideoPlayer.setBackgrounded(false);
        mVideoView.requestFocus();
        mPlayed.setImageResource(isPaused ? R.drawable.movie_control_pause : R.drawable.movie_control_play);
        
        long duration = mVideoPlayer.getDuration();
        long curPos = mVideoPlayer.getCurrentPosition();
        // This code is to fix a critical issue.
        // In some special case, shut down the screen or jump to another activity in the last 1
        // second of ad video playing, the player will be stuck.
        if (isPlayingAd && !isAdFinished && (duration != -1 && duration - curPos <= 1000)) {
            playVideo(0);
        }
        if (!isPaused) {
            mVideoPlayer.getPlayerControl().start();
        }
    }

	@Override
	protected void onDestroy() {
		finishEvent();
		if (diaLoading != null) {
			diaLoading.dismiss();
		}
        long duration = mVideoPlayer.getDuration();
        float percent = (float)(mVideoPlayer.getCurrentPosition()) / duration;
        JSONObject data = new JSONObject();
        data.put("percent", (float)(Math.round(percent * 100)) / 100);
        data.put("resId", String.valueOf(videoId));
        Analytics.logEvent(IFEApplication.getInstance().getApplicationContext(), AnalyticsType.getOperationVideoMus(5), AnalyticsType.ORIGIN_DETAIL, data);
		releasePlayer();

		if (adsVideoRequest != null) {
		    adsVideoRequest.cancel();
		}
		if (adsPicRequest != null) {
		    adsPicRequest.cancel();
		}
		
		super.onDestroy();
	}
	
	private void logPlayerEvent(String type) {
	    Analytics.logEvent(IFEApplication.getInstance().getApplicationContext(), type, AnalyticsType.ORIGIN_DETAIL, AnalyticsType.getSingleData(videoId));
	}

	private void initData() {
		name = getIntent().getStringExtra("name");
		videoId = getIntent().getStringExtra("id");
		videoType = getIntent().getIntExtra("type", 0);
        videoImg = getIntent().getStringExtra("img");
        videoPath = getIntent().getStringExtra("videoPath");

		isPlayAds = getIntent().getBooleanExtra("isPlayAds", true);
		canRating = getIntent().getBooleanExtra("canRating", false);
		curUserRatingMsg = getIntent().getStringExtra("curUserRatingMsg");
		positiveCount = getIntent().getIntExtra("positiveCount", 0);
		negativeCount = getIntent().getIntExtra("negativeCount", 0);
		
	}

    private boolean startWebActivity(Ads ads) {
        if (ads == null || ads.getRelateId() <= 0 || ads.getRelateType() == null || !ads.getRelateType().equals("thirdparty_web")) {
            return false;
        }
        Intent i = new Intent(VideoPlayerActivity.this, ThirdpartyWebActivity.class);
        i.putExtra("resourceId", ads.getRelateId());
        VideoPlayerActivity.this.startActivity(i);
        JSONObject adInfo = new JSONObject();
        adInfo.put("id", ads.getId());
        Analytics.logEvent(IFEApplication.getInstance().getApplicationContext(), AnalyticsType.getOperationDynamic(5),
            AnalyticsType.ORIGIN_DETAIL, AnalyticsType.getComplexData(videoId, ads.getRelateId(), "adInfo", adInfo, AnalyticsType.RESOURCE_TYPE_OTHER));
        return true;
    }

	private void initView() {
		mVideoPlayer = new DemoPlayer();
		mVideoPlayer.addListener(this);
		
		mVideoView = (VideoSurfaceView) findViewById(R.id.video_player);
		mVolumeBrightnessLayout = findViewById(R.id.operation_volume_brightness);
		mOperationBg = (ImageView) findViewById(R.id.operation_bg);
		mOperationPercent = (ImageView) findViewById(R.id.operation_percent);
		mADTime = (TextView) findViewById(R.id.ad_time);

		/** 开始、暂停、快进、快退 */
		controlView = getLayoutInflater().inflate(R.layout.movie_control_layout, null);
		controler = new PopupWindow(controlView);
		durationTextView = (TextView) controlView.findViewById(R.id.duration);
		playedTextView = (TextView) controlView.findViewById(R.id.has_played);
		mPlayed = (ImageView) controlView.findViewById(R.id.movic_control_played);
		mAddGo = (ImageView) controlView.findViewById(R.id.addgo);
		mAddBack = (ImageView) controlView.findViewById(R.id.addback);
		blightSeekBar = (SeekBar) controlView.findViewById(R.id.movie_control_bright);
		volumeSeekBar = (SeekBar) controlView.findViewById(R.id.movie_control_volume);
		durationSeekBar = (SeekBar) controlView.findViewById(R.id.movie_control_duration);

		// ad
		mAD = findViewById(R.id.pic_ad);
		mADPic = (ImageView) mAD.findViewById(R.id.pic);
		mADPic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			    startWebActivity(pictureAds);
			}
		});
		
        closeBtn = (ImageButton)findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                JSONObject adInfo = new JSONObject();
                adInfo.put("id", pictureAds.getId());
                Analytics.logEvent(
                                IFEApplication.getInstance().getApplicationContext(),
                                AnalyticsType.getOperationDynamic(12),
                                AnalyticsType.ORIGIN_DETAIL,
                                AnalyticsType.getComplexData(videoId, pictureAds.getRelateId(), "adInfo", adInfo, AnalyticsType.RESOURCE_TYPE_OTHER));
                 hidePicAD();
            }

        });
		
		enableRatingVideo(false);
	}

	private void enableRatingVideo(boolean enable){
		int visibility = View.VISIBLE;
    	if(!enable || POSITIVE.equals(curUserRatingMsg) || NEGATIVE.equals(curUserRatingMsg)){
    		visibility = View.GONE;
    	}
//    	voteDownBg.setVisibility(visibility);
//		voteUpBg.setVisibility(visibility);
    }
    
	private void setListener() {
		mGestureDetector = new GestureDetector(this, new MyGestureListener());

		durationSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			    isPrintLog = false;
				hideControllerDelay();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				cancelDelayHide();
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {

					mVideoPlayer.seekTo(progress);
				}
			}
		});
		lp = getWindow().getAttributes();
		blightSeekBar.setMax(Max_Brightness);
		fBrightness = getWindow().getAttributes().screenBrightness;
		if (fBrightness < 0) {
			if (fBrightness <= 0.00f)
				fBrightness = 0.50f;
			if (fBrightness < 0.01f)
				fBrightness = 0.01f;
		}
		blightSeekBar.setProgress((int) (fBrightness * Max_Brightness));
		blightSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				hideControllerDelay();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				cancelDelayHide();
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				fBrightness = (float) progress / (float) Max_Brightness;
				if (fBrightness <= 0.01f)
					fBrightness = 0.01f;
				lp.screenBrightness = fBrightness;
				// 这句得加上，否则屏幕亮度不启作用
				getWindow().setAttributes(lp);

			}
		});

		volumeSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				hideControllerDelay();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				cancelDelayHide();
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);

			}
		});
		
		mPlayed.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		        cancelDelayHide();
		        if (isPaused) {
		            // 使广告消失
		            hidePicAD();
		            mVideoPlayer.getPlayerControl().start();
		            mVideoView.requestFocus();
		            mPlayed.setImageResource(R.drawable.movie_control_play);
		            hideControllerDelay();
		        } else {
		            getADPic();
		            mVideoPlayer.getPlayerControl().pause();
		            mPlayed.setImageResource(R.drawable.movie_control_pause);
		        }

		        logPlayerEvent(AnalyticsType.getOperationVideoMus(isPaused ? 7 : 6));
		        isPaused = !isPaused;
			}
		});

		mAddGo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				cancelDelayHide();
				hideControllerDelay();
				addgo();
			}
		});

		mAddBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				cancelDelayHide();
				hideControllerDelay();
				addback();

			}
		});

	}

	private void keepScreenOn(boolean screenOn) {
		if (screenOn && mVideoPlayer.getPlayerControl().isPlaying()) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}

	// DemoPlayer.Listener implementation

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
        case ExoPlayer.STATE_BUFFERING:
            keepScreenOn(true);
            if (mVideoPlayer.getDuration() == 0) {
                diaLoading.hide();
                _handler.sendEmptyMessageDelayed(VIDEO_DURATION, 500);
            } else {
                if (isPrintLog) {
                    diaLoading.show();
                    logPlayerEvent(AnalyticsType.getOperationVideoMus(11));
                } else {
                    diaLoading.show();
                    isPrintLog = true;
                }
            }
            break;
        case ExoPlayer.STATE_ENDED:
            keepScreenOn(false);
            ADisPlaying();
            break;
        case ExoPlayer.STATE_IDLE:
            keepScreenOn(false);
            break;
        case ExoPlayer.STATE_PREPARING:
            keepScreenOn(true);
            diaLoading.show();
            break;
        case ExoPlayer.STATE_READY:
            isExists = true;
            keepScreenOn(true);
            diaLoading.hide();
            if (isPlayingAd) {
                long duration = mVideoPlayer.getDuration();
                long position = mVideoPlayer.getCurrentPosition();
                mADTime.setVisibility(View.VISIBLE);
                updateLeftADtime(duration, position);
            } else {
                int time = (int) mVideoPlayer.getDuration();
                durationSeekBar.setMax(time);
                durationTextView.setText(ComUtil.Time(time));
                _handler.sendEmptyMessage(PROGRESS_CHANGED);
            }
            break;
        default:
            keepScreenOn(false);
            break;
        }
    }
	
	@Override
	public void onError(Exception e) {
	    String msg_error = "";
	    if (isPlayingAd) {
	        JSONObject object = new JSONObject();
            object.put("info", "ads video error!");
            Analytics.logEvent(IFEApplication.getInstance().getApplicationContext(),
                    AnalyticsType.getOperationDevice(11), AnalyticsType.ORIGIN_USER, object);
            logger.warn("ads video error!");
            playVideo(mLastPosition);
        }else {
            if ((e instanceof UnsupportedDrmException)) {
                msg_error = getResources().getString(R.string.fail_play_msg);
            }else {
                if (isExists) {
                    msg_error = getResources().getString(R.string.video_player_info_net_error);
                }else {
                    msg_error = getResources().getString(R.string.video_player_info_exists_error);
                }
                
            }
            
            showErrorDialog(isExists,msg_error);
        }
	}
	
	private void playVideo(long position) {
		isPlayingAd = false;
		setAdFinished(true);
		_handler.removeMessages(UPDAT_TIME_MESSAGE);
		mADTime.setVisibility(View.GONE);
		if(videoPath != null) {
		    setVideoURI(videoPath, position);
		    logPlayerEvent(AnalyticsType.getOperationVideoMus(4));
		} else {
		    finish();
		}
	}

	@Override
	public void onVideoSizeChanged(int width, int height, float pixelWidthAspectRatio) {
		mVideoView.setVideoWidthHeightRatio(
				height == 0 ? 1 : (width * pixelWidthAspectRatio) / height);
	}

	private void updateLeftADtime(long duration, long position) {
		if (isFinishing()) {
			return;
		}
		//long lefttime = Math.min(duration, AD_TIME * 1000) - position;
		long lefttime = duration - position;
		int sec = (int)(lefttime / 1000);
		String begin = getString(R.string.video_ad_time) + " ";
		StringBuilder text = new StringBuilder(begin);
		if (sec > 0) {
			int secs = sec;
			int len=String.valueOf(secs).length();
            if (secs < 10) {
                text.append("0" + secs).append(getString(R.string.second));
                len=2;
            } else {
                text.append(secs).append(getString(R.string.second));
            }
			SpannableString ss = new SpannableString(text.toString());
            ss.setSpan(new ForegroundColorSpan(Color.YELLOW), begin.length(), begin.length() + len,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            mADTime.setText(ss);
		}

		if (lefttime > 0) {
			_handler.sendEmptyMessageDelayed(UPDAT_TIME_MESSAGE, 1000);
		}
	}

	/**
	 * 判断如果广告正在播放的处理
	 */
	private void ADisPlaying() {

		if (isPlayingAd) {
			playVideo(mLastPosition);
		} else {
		    if(!isCompleteRecorded) {
		        logPlayerEvent(AnalyticsType.getOperationVideoMus(13));
		        isCompleteRecorded = true;
		    }
			finishEvent();
			finish();
		}
	}

	/* 结束事件要做的操作 */
	private void finishEvent() {
	    
		if (controler.isShowing()) {
			controler.dismiss();

		}
		if (mVideoPlayer != null) {
			mVideoPlayer.stopPlayback();
		}
		hide();
		_handler.removeMessages(UPDAT_TIME_MESSAGE);
		_handler.removeMessages(PROGRESS_CHANGED);
		_handler.removeMessages(VIDEO_DURATION);
		cancelDelayHide();
	}

	// 视频播放出错提示框
	private void showErrorDialog(boolean flag_layout,String result) {

		VideoDialog dialogBuilder = VideoDialog.getInstance(this);
		if (flag_layout) {
            dialogBuilder.getButton(R.id.btn_sure).setText(getResources().getString(R.string.retry));
        }
		dialogBuilder.withMessage(flag_layout, result).setmListener(videoErrorCallback).show();

	}

	OnSureListener videoErrorCallback = new OnSureListener() {

		@Override
		public void doSomeThings(View v) {
		    
		    if (v.getId() == R.id.btn_sure) {
                playVideo(mLastPosition);
            }else {
                if (isPlayingAd) {
                    isPlayingAd = false;
                    _handler.removeMessages(UPDAT_TIME_MESSAGE);
                    mADTime.setVisibility(View.GONE);
                    setAdFinished(true);
                    setVideoURI(videoPath, mLastPosition);
                    mVideoPlayer.getPlayerControl().start();
                    diaLoading.show();
                } else {
                    finishEvent();
                    finish();
                }
            }

		}

	};

	private void show() {
		if (!mShowing) {
			mShowing = true;
		}
		hideControllerDelay();
	}

	private void hide() {
		cancelDelayHide();

		if (mShowing) {

			controler.dismiss();
			mShowing = false;
		}
	}

	@Override
	public void onClick(View arg0) {
		finishEvent();
		finish();
	}

	/** 定时隐藏 */
	private Handler mDismissHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mVolumeBrightnessLayout.setVisibility(View.GONE);
		}
	};
    
	/**
	 * 滑动改变声音大小
	 * 
	 * @param percent
	 */
	private void onVolumeSlide(float percent) {
		if (mVolume == -1) {
			mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			if (mVolume < 0)
				mVolume = 0;

			// 显示
			mOperationBg.setImageResource(R.drawable.video_volumn_bg);
			mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
		}

		int index = (int) (percent * mMaxVolume) + mVolume;
		if (index > mMaxVolume)
			index = mMaxVolume;
		else if (index < 0)
			index = 0;
		volumeSeekBar.setProgress(index);

		// 变更声音
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

		// 变更进度条
		ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
		lp.width = findViewById(R.id.operation_full).getLayoutParams().width * index / mMaxVolume;
		mOperationPercent.setLayoutParams(lp);
	}

	/**
	 * 滑动改变亮度
	 * 
	 * @param percent
	 */
	private void onBrightnessSlide(float percent) {
		if (mBrightness < 0) {
			mBrightness = getWindow().getAttributes().screenBrightness;
			if (mBrightness <= 0.00f)
				mBrightness = 0.50f;
			if (mBrightness < 0.01f)
				mBrightness = 0.01f;

			// 显示
			mOperationBg.setImageResource(R.drawable.video_brightness_bg);
			mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
		}
		WindowManager.LayoutParams lpa = getWindow().getAttributes();
		lpa.screenBrightness = mBrightness + percent; 
		if (lpa.screenBrightness > 1.0f)
			lpa.screenBrightness = 1.0f;
		else if (lpa.screenBrightness < 0.01f)
			lpa.screenBrightness = 0.01f;
		getWindow().setAttributes(lpa);
		blightSeekBar.setProgress((int) (lpa.screenBrightness * Max_Brightness));
		ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
		lp.width = (int) (findViewById(R.id.operation_full).getLayoutParams().width * lpa.screenBrightness);
		mOperationPercent.setLayoutParams(lp);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mGestureDetector.onTouchEvent(event)) {
			return true;
		}

		// 处理手势结束
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_UP:
			endGesture();
			break;
		}

		return super.onTouchEvent(event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean key = super.onKeyUp(keyCode, event);
		if (keyCode==KeyEvent.KEYCODE_VOLUME_UP || keyCode==KeyEvent.KEYCODE_VOLUME_DOWN) {
			mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			volumeSeekBar.setProgress(mVolume);
		}
		return key;
	}

	/** 手势结束 */
	private void endGesture() {
		mVolume = -1;
		mBrightness = -1f;

		// 隐藏
		mDismissHandler.removeMessages(0);
		mDismissHandler.sendEmptyMessageDelayed(0, 1000);
	}
	boolean isUp=true;

    private class MyGestureListener extends SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float mOldX = e1.getX(), mOldY = e1.getY();
            int y = (int) e2.getRawY();
            int x = (int) e2.getRawX();
            Display disp = getWindowManager().getDefaultDisplay();
            int windowWidth = disp.getWidth();
            int windowHeight = disp.getHeight();
            long currentPosition = mVideoPlayer.getCurrentPosition();

            if (e2.getY() - mOldY > TOUCH_LENGTH_Y && e2.getX() < windowWidth * 1.0 / 2.0) {
                isUp = true;
                onBrightnessSlide((mOldY - y) / windowHeight);
            } else if (mOldY - e2.getY() > TOUCH_LENGTH_Y && e2.getX() < windowWidth * 1.0 / 2.0) {
                isUp = false;
                onBrightnessSlide((mOldY - y) / windowHeight);
            } else if (e2.getY() - mOldY > TOUCH_LENGTH_Y && e2.getX() > windowWidth * 1.0 / 2.0) {
                isUp = true;
                onVolumeSlide((mOldY - y) / windowHeight);
            } else if (mOldY - e2.getY() > TOUCH_LENGTH_Y && e2.getX() > windowWidth * 1.0 / 2.0) {
                isUp = false;
                onVolumeSlide((mOldY - y) / windowHeight);
            } else if (Math.abs(distanceX) >= Math.abs(distanceY)) {
                if (!isPlayingAd && e2.getX() - mOldX > 0) {
                    currentPosition += 10000;
                    mVideoPlayer.seekTo(currentPosition);
                } else if (!isPlayingAd && e2.getX() - mOldX < 0) {
                    currentPosition -= 10000;
                    mVideoPlayer.seekTo(currentPosition);
                }
            }

            return false;
        }

        public void onLongPress(MotionEvent e) {
            // mMediaController.hide();
        }
        
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Display display = getWindowManager().getDefaultDisplay();
            if (isPlayingAd) {
                if(videoAds != null) {
                    startWebActivity(videoAds);
                }
                if (!controler.isShowing()) {
                    show();
                } else {
                    hide();
                }
            } else {
                if (controler.isShowing()) {
                    controler.dismiss(); 
                    hide();
                } else {
                    controler.showAtLocation(mVideoView, Gravity.TOP, 135, 555);
                    controler.update(0, (int)((display.getHeight() * 7.8) / 10), (display.getWidth() * 4) / 5,
                            (int)(display.getHeight() / 5.3));
                    show();
                }
            }
            return super.onSingleTapConfirmed(e);
        }
    }

	private void cancelDelayHide() {
		_handler.removeMessages(HIDE_MESSAGE);
	}

	private void hideControllerDelay() {
		_handler.sendEmptyMessageDelayed(HIDE_MESSAGE, 3000);
	}

	private void addgo() {
		long goPosition = mVideoPlayer.getCurrentPosition() + 20000;
		if (goPosition > mVideoPlayer.getDuration()) {
			goPosition = mVideoPlayer.getDuration();
		}
		mVideoPlayer.seekTo(goPosition);
	}

	private void addback() {
		long backPosition = mVideoPlayer.getCurrentPosition() - 20000;
		if (backPosition < 0) {
			backPosition = 0;
		}
		mVideoPlayer.seekTo(backPosition);
	}

	private void setVolum() {
		mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		volumeSeekBar.setMax(mMaxVolume);
		mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		volumeSeekBar.setProgress(mVolume);
	}

	/** 播放影视 */
	private void setVideoURI(final String videoPath, long position) {
       if (videoPath == null) {
           ComUtil.toastText("您访问的资源出了一点问题,请点击返回", Toast.LENGTH_SHORT);
           return;
    } 
	    
	        mVideoPlayer.prepare(getRendererBuilder(videoPath));
            mVideoPlayer.setSurface(mVideoView.getHolder().getSurface());
            mVideoPlayer.setPlayWhenReady(!isPaused);
            mVideoPlayer.seekTo(position);
            isPrintLog = false;
	}
	
	private void finishAd() {
	    setAdFinished(true);
	    if(videoPath != null) {
	        setVideoURI(videoPath, mLastPosition);
	    } else {
	        finish();
	    }
	}
	
	private void setAdsVideo(Ads ads) {
	    videoAds = ads;
        if (ads == null) {
            finishAd();
            return;
        }
        String videoAdPath = ads.getPath();
        if (videoAdPath != null) {
            setVideoURI(videoAdPath, 0);
            _handler.sendEmptyMessageDelayed(UPDAT_TIME_MESSAGE, 1000);
            hideControllerDelay();
            isPlayingAd = true;
            JSONObject adInfo = new JSONObject();
            adInfo.put("id", videoAds.getId());
            Analytics.logEvent(
                            IFEApplication.getInstance().getApplicationContext(),
                            AnalyticsType.getOperationDynamic(11),
                            AnalyticsType.ORIGIN_DETAIL,
                            AnalyticsType.getComplexData(videoId, videoAds.getRelateId(), "adInfo", adInfo, AnalyticsType.RESOURCE_TYPE_OTHER));
        } else {
            finishAd();
        }
	}
	
    private void getADPic() {
        if(adsPicRequest != null) {
            adsPicRequest.cancel();
        }
        adsPicRequest = new AdsRequest(new AdsImageCallback(this), AdsRequest.ADS_TYPE_VIDEO_PAUSE);
        adsPicRequest.execute((String) null);
    }
    
    private void hidePicAD() {
        ObjectAnimator oa = ObjectAnimator.ofFloat(mAD, "alpha", 0).setDuration(ANIMATION_AD_FADE_PERIOD);
        oa.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAD.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mAD.setVisibility(View.GONE);
            }
        });
        oa.start();
    }
    
    private void showPicAD(String path) {
        JSONObject adInfo = new JSONObject();
        adInfo.put("id", pictureAds.getId());
        Analytics.logEvent(
                        IFEApplication.getInstance().getApplicationContext(),
                        AnalyticsType.getOperationDynamic(11),
                        AnalyticsType.ORIGIN_DETAIL,
                        AnalyticsType.getComplexData(videoId, pictureAds.getRelateId(), "adInfo", adInfo, AnalyticsType.RESOURCE_TYPE_OTHER));
        
        mAD.setAlpha(0.f);
        mAD.setVisibility(View.VISIBLE);
        mADPic.setImageURI(Uri.fromFile(new File(path)));
        ObjectAnimator oa = ObjectAnimator.ofFloat(mAD, "alpha", 1).setDuration(ANIMATION_AD_FADE_PERIOD);
        oa.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAD.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mAD.setVisibility(View.VISIBLE);
            }

        });
        oa.start();
    }
    
	private void setAdsPicture(Ads ads) {
        pictureAds = ads;
        String path = null;
        if (ads == null || (path = ads.getPath()) == null || path.isEmpty()) {
            hidePicAD();
        }
        String pic = PhotoManager.getInstance().getImageFile(path);
        if (pic != null) {
            showPicAD(pic);
        } else {
            PhotoManager.getInstance().downloadImage(path, this);
        }
	}

	@Override
	public void onGetResult(int requestType, Object result) {
        if (requestType == VIDEO_RATING_API) {
            if (result == null) {
                ratingMsg = "";
                ComUtil.toastText("抱歉，现在不能评价视频", Toast.LENGTH_SHORT);
                return;
            }
            curUserRatingMsg = ratingMsg;
            if (POSITIVE.equals(ratingMsg)) {
//                voteDownBtn.setSelected(false);
//                voteUpBtn.setSelected(true);
//                ++positiveCount;
            } else if (NEGATIVE.equals(ratingMsg)) {
//                voteUpBtn.setSelected(false);
//                voteDownBtn.setSelected(true);
//                ++negativeCount;
            }
            ratingMsg = "";
            ComUtil.toastText("感谢您的评价", Toast.LENGTH_SHORT);
//            startAnimationForRating();
        }
	}

    @Override
    public void onPhotoDownload(String url, final String path) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (path == null) {
                    hidePicAD();
                } else if(isPaused){
                    showPicAD(path);
                }
            }
        });
    }

	@Override
	public void onPhotoDownloadError(String url, String path) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onError(int requestType) {
		diaLoading.hide();
		if (requestType != REDIRECT_API) {
			ComUtil.toastText( "连接服务器出错", Toast.LENGTH_SHORT);
		}
		if (requestType == VIDEO_RATING_API){
//			voteUpBtn.setSelected(false);
//			voteDownBtn.setSelected(false);
			ratingMsg = "";
		}else{
			finish();
		}
	}

	private RendererBuilder getRendererBuilder(String contentUri) {
		String userAgent = Util.getUserAgent(this);
		String contentId = Util.getContentId(contentUri);
		switch (contentUri.substring(contentUri.lastIndexOf(".")).toLowerCase()) {
//			case DemoUtil.TYPE_SS:
//				return new SmoothStreamingRendererBuilder(userAgent, contentUri.toString(), contentId,
//						new SmoothStreamingTestMediaDrmCallback(), debugTextView);
//			case DemoUtil.TYPE_DASH:
			case ".mpd":
				return new DashRendererBuilder(userAgent, contentUri.toString(), contentId, null);
//						new WidevineTestMediaDrmCallback(contentId), debugTextView);
//			case DemoUtil.TYPE_HLS:
			case ".m3u8":
				return new HlsRendererBuilder(userAgent, contentUri, contentId);
			default:
				return new DefaultRendererBuilder(this, Uri.parse(contentUri));
		}
	}

	private void releasePlayer() {
		if (mVideoPlayer != null) {
			mVideoPlayer.release();
			mVideoPlayer = null;
		}
	}

	private void setAdFinished(boolean isAdFinished) {
		this.isAdFinished = isAdFinished;
		if(isAdFinished){
			enableRatingVideo(true);
		}
	}
}
