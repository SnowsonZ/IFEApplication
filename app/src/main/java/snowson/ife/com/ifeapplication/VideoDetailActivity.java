package snowson.ife.com.ifeapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fairlink.common.BaseHttpTask.HttpTaskCallback;
import com.fairlink.common.DownloadTask;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.VideoSurfaceView;
import com.google.android.exoplayer.util.PlayerControl;

import java.util.ArrayList;

import snowson.ife.com.ifeapplication.adapter.MovieListAdapter;
import snowson.ife.com.ifeapplication.application.IFEApplication;
import snowson.ife.com.ifeapplication.bean.VideoListInfo;
import snowson.ife.com.ifeapplication.exoplayerwrapper.DashRendererBuilder;
import snowson.ife.com.ifeapplication.exoplayerwrapper.DefaultRendererBuilder;
import snowson.ife.com.ifeapplication.exoplayerwrapper.DemoPlayer;
import snowson.ife.com.ifeapplication.exoplayerwrapper.HlsRendererBuilder;
import snowson.ife.com.ifeapplication.exoplayerwrapper.Util;
import snowson.ife.com.ifeapplication.request.MovieDetailRequest;
import snowson.ife.com.ifeapplication.request.MovieDetailRequest.MovieDetail;
import snowson.ife.com.ifeapplication.request.VideoListDetailByParentIdRequest;
import snowson.ife.com.ifeapplication.utils.ImageUtil;
import snowson.ife.com.ifeapplication.view.HorizontalListView;

public class VideoDetailActivity extends Activity implements DemoPlayer.Listener{

    private HorizontalListView mHListView;
    private MovieListAdapter mAdapter;
    private ArrayList<VideoListInfo> mDatas;
    private ImageView iv_ding,iv_cai,iv_extends,iv_video_img;
    private TextView tv_ding_num, tv_cai_num, tv_movie_desc;
    private ClickEventListener mClickListener;
    private VideoSurfaceView videoView;
    private DemoPlayer videoPlayer;
    private boolean isCommited = false;
    private boolean isShow = false;
    private String videoPath;
    private long lastPos = 0;
    private ProgressBar mProgess;
    private MediaController mediaController;
//    private PopupWindow mControllerContainer;
//    private View mControllerView;
    private boolean isAdFinished = false;
//    private TextView durationTextView;
//    private TextView playedTextView;
//    private ImageView mPlayed;
//    private ImageView mAddGo;
//    private ImageView mAddBack;
//    private SeekBar blightSeekBar;
//    private SeekBar volumeSeekBar;
//    private SeekBar durationSeekBar;
    private boolean isPause = false;
    private static final int HIDE_MESSAGE = 1;
    private static final int UPDAT_TIME_MESSAGE = 2;
    private static final int PROGRESS_CHANGED = 3;
    private static final int VIDEO_DURATION = 4;
    private static final int AD_TIME = 90;
    private static final int AD_LEFT_TIME = 1500;
    private boolean control_show = true;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case VIDEO_DURATION:
                    long duration = videoPlayer.getDuration();
                    if (duration > 0) {
                        long position = videoPlayer.getCurrentPosition();
//                        adTimeView.setVisibility(View.VISIBLE);
//                        updateLeftADtime(duration, position);
                    } else {
                        handler.sendEmptyMessageDelayed(VIDEO_DURATION, 500);
                    }
                    break;
                case UPDAT_TIME_MESSAGE:
//                    updateLeftADtime(videoPlayer.getDuration(), videoPlayer.getCurrentPosition());
                    break;

            }
            super.handleMessage(msg);
        }
    };
    private PlayerControl playerControl;
    private TextView mTv_actor_name;
    private TextView mTv_video_desc;
    private TextView mTv_name;
    private ImageView mIv_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);
        initView();
        initDatas();
        initPlayer();
        initListener();
    }

    private void initPlayer() {
        videoPlayer = new DemoPlayer();
        videoPlayer.addListener(this);
        videoView.getHolder().addCallback(new SurfaceHolder.Callback() {
            public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {}

            public void surfaceCreated(SurfaceHolder holder) {
                if (videoPlayer != null) {
                    videoPlayer.setSurface(holder.getSurface());
                }
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                if (videoPlayer != null) {
                    videoPlayer.blockingClearSurface();
                }
            }
        });
        //绑定点击事件, 控制显示隐藏controller
        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mediaController.isShowing()) {
                        mediaController.hide();
                    } else {
                        mediaController.show();
                    }
                }
                return true;
            }
        });
        //创建controller
        mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);

        mediaController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        playerControl = videoPlayer.getPlayerControl();
        mediaController.setMediaPlayer(playerControl);
        mediaController.setEnabled(true);
        videoPlayer.prepare(getRendererBuilder(videoPath));
        videoPlayer.setSurface(videoView.getHolder().getSurface());
        videoPlayer.setPlayWhenReady(true);
        videoPlayer.seekTo(lastPos);
    }

    private DemoPlayer.RendererBuilder getRendererBuilder(String contentUri) {
        String userAgent = Util.getUserAgent(this);
        String contentId = Util.getContentId(contentUri);
        switch (contentUri.substring(contentUri.lastIndexOf(".")).toLowerCase()) {
            case ".mpd":
                return new DashRendererBuilder(userAgent, contentUri.toString(), contentId, null);
            case ".m3u8":
                return new HlsRendererBuilder(userAgent, contentUri, contentId);
            default:
                return new DefaultRendererBuilder(this, Uri.parse(contentUri));
        }
    }

    private void initListener() {
        mClickListener = new ClickEventListener(this);
        iv_cai.setOnClickListener(mClickListener);
        iv_ding.setOnClickListener(mClickListener);
        iv_extends.setOnClickListener(mClickListener);
    }

    private void initView() {
        mHListView = (HorizontalListView) findViewById(R.id.movie_list);
        iv_cai = (ImageView) findViewById(R.id.img_cai);
        iv_ding = (ImageView) findViewById(R.id.img_ding);
        tv_cai_num = (TextView) findViewById(R.id.tv_cai_num);
        tv_ding_num = (TextView) findViewById(R.id.tv_ding_num);
        iv_extends = (ImageView) findViewById(R.id.img_extends);
        tv_movie_desc = (TextView) findViewById(R.id.tv_movie_desc);
        videoView = (VideoSurfaceView) findViewById(R.id.player);
        mProgess = (ProgressBar) findViewById(R.id.loadingIcon);
        iv_video_img = (ImageView) findViewById(R.id.video_img);
        mTv_actor_name = (TextView) findViewById(R.id.tv_actorname);
        mTv_video_desc = (TextView) findViewById(R.id.tv_movie_desc);
        mTv_name = (TextView) findViewById(R.id.tv_name);
        mIv_back = (ImageView) findViewById(R.id.img_back);

//        mControllerView = getLayoutInflater().inflate(R.layout.movie_control_layout, null);
////        mControllerContainer = new PopupWindow(mControllerView);
//        durationTextView = (TextView) mControllerView.findViewById(R.id.duration);
//        playedTextView = (TextView) mControllerView.findViewById(R.id.has_played);
//        mPlayed = (ImageView) mControllerView.findViewById(R.id.movic_control_played);
//        mAddGo = (ImageView) mControllerView.findViewById(R.id.addgo);
//        mAddBack = (ImageView) mControllerView.findViewById(R.id.addback);
//        blightSeekBar = (SeekBar) mControllerView.findViewById(R.id.movie_control_bright);
//        volumeSeekBar = (SeekBar) mControllerView.findViewById(R.id.movie_control_volume);
//        durationSeekBar = (SeekBar) mControllerView.findViewById(R.id.movie_control_duration);

//        mControllerContainer.showAtLocation(videoView, Gravity.TOP, 135, 555);

    }

    private HttpTaskCallback VideoDetailCallBack = new HttpTaskCallback() {
        @Override
        public void onGetResult(int requestType, Object result) {
            if (result == null) {
                return;
            }
            MovieDetail detail = (MovieDetail) result;
            if (detail.items == null || detail.items.isEmpty()) {
                return;
            }
            tv_ding_num.setText(String.valueOf(detail.positiveCount));
            tv_cai_num.setText(String.valueOf(detail.negativeCount));
            ImageUtil.setImageView(detail.image, ImageUtil.SMALL, iv_video_img, null);
            mTv_actor_name.setText(detail.actor);
            mTv_video_desc.setText(detail.content);
            mTv_name.setText(detail.name);
        }

        @Override
        public void onError(int requestType) {

        }
    };

    private HttpTaskCallback VideoListCallBack = new HttpTaskCallback() {
        @Override
        public void onGetResult(int requestType, Object result) {
            if(result != null) {
                mDatas = (ArrayList<VideoListInfo>) result;
                mAdapter = new MovieListAdapter(mDatas, VideoDetailActivity.this);
                mHListView.setAdapter(mAdapter);
            }else {
                Toast.makeText(VideoDetailActivity.this, "暂无其他视频信息", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public void onError(int requestType) {

        }
    };

    private void initDatas() {

        mDatas = new ArrayList<VideoListInfo>();
        Intent intent = getIntent();
        int videoId = intent.getBundleExtra("data").getInt("videoId");
        new MovieDetailRequest(videoId, VideoDetailCallBack).execute((String)null);
        new VideoListDetailByParentIdRequest(videoId, VideoListCallBack).execute((String)null);

        videoPath = "http://192.168.10.66/data/source/video/A_004_0030_256/1/default.m3u8";
        lastPos = IFEApplication.getInstance().getVideoPosition(videoPath);
    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {

        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                keepScreenOn(true);
                if (videoPlayer.getDuration() == 0) {
                    if(mProgess != null) {
                        mProgess.setVisibility(View.GONE);
                    }
                    handler.sendEmptyMessageDelayed(VIDEO_DURATION, 500);
                } else {
                    if(mProgess != null) {
                        mProgess.setVisibility(View.VISIBLE);
                    }
                }
                break;
            case ExoPlayer.STATE_ENDED:
                keepScreenOn(false);
                stop();
                break;
            case ExoPlayer.STATE_IDLE:
                keepScreenOn(false);
                break;
            case ExoPlayer.STATE_PREPARING:
                keepScreenOn(true);
                if(mProgess != null) {
                    mProgess.setVisibility(View.VISIBLE);
                }
                break;
            case ExoPlayer.STATE_READY:
                keepScreenOn(true);
                if(mProgess != null) {
                    mProgess.setVisibility(View.GONE);
                }
                long duration = videoPlayer.getDuration();
                if(duration == -1) {
                    stop();
                }
                long position = videoPlayer.getCurrentPosition();
//                adTimeView.setVisibility(View.VISIBLE);
//                updateLeftADtime(duration, position);
                break;
            default:
                keepScreenOn(false);
                break;
        }

    }

    @Override
    public void onError(Exception e) {
        stop();
    }

    @Override
    public void onVideoSizeChanged(int width, int height, float pixelWidthHeightRatio) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeMessages(UPDAT_TIME_MESSAGE);
        lastPos = videoPlayer.getCurrentPosition();
        long duration = videoPlayer.getDuration();
        if (lastPos >= duration - 500) {
            lastPos = 0;
        }
        IFEApplication.getInstance().setVideoPosition(videoPath, lastPos);
        videoPlayer.getPlayerControl().pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isAdFinished) {
            return;
        }

        DownloadTask.suspend();
        videoPlayer.setBackgrounded(false);
        videoView.requestFocus();

        videoPlayer.getPlayerControl().start();
        long duration = videoPlayer.getDuration();
        long curPos = videoPlayer.getCurrentPosition();

        // This code is to fix a critical issue.
        // In some special case, shut down the screen or jump to another activity in the last 1
        // second of ad video playing, the player will be stuck.
        if (!isAdFinished && (duration != -1 && duration - curPos <= AD_LEFT_TIME)) {
            stop();
        }
    }

    private void stop() {
        isAdFinished = true;
        if(videoPlayer != null) {
            videoPlayer.stopPlayback();
        }
//        adTimeView.setVisibility(View.GONE);
        handler.removeMessages(UPDAT_TIME_MESSAGE);
        if(this != null) {
//            LoginMain main = (LoginMain)getActivity();
//            main.enterLogin();
            stop();
        }
    }

    private void releasePlayer() {
        if (videoPlayer != null) {
            videoPlayer.release();
            videoPlayer = null;
        }
    }

    private void finishEvent() {
        if (videoPlayer != null) {
            videoPlayer.stopPlayback();
        }
        handler.removeMessages(UPDAT_TIME_MESSAGE);
        handler.removeMessages(PROGRESS_CHANGED);
        handler.removeMessages(VIDEO_DURATION);
        handler.removeMessages(HIDE_MESSAGE);
    }

    private void keepScreenOn(boolean screenOn) {
        if(this == null || this.isFinishing()) {
            return;
        }
        if (screenOn && videoPlayer.getPlayerControl().isPlaying()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    protected void onDestroy() {
        finishEvent();
        releasePlayer();
        super.onDestroy();
    }

    class ClickEventListener implements View.OnClickListener {

        private Context mContext = null;

        public ClickEventListener(Context context) {
            mContext = context;
        }

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.img_cai:
                    if(!isCommited) {
                        iv_cai.setImageResource(R.drawable.btn_cai_selected);
                        tv_cai_num.setText(String.valueOf(Integer.parseInt(tv_cai_num.getText().toString()) + 1));
                        isCommited = true;
                    }else {
                        Toast.makeText(mContext, "您已评价过该视频", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.img_ding:
                    if(!isCommited) {
                        iv_ding.setImageResource(R.drawable.btn_ding_selected);
                        tv_ding_num.setText(String.valueOf(Integer.parseInt(tv_ding_num.getText().toString()) + 1));
                        isCommited = true;
                    }else {
                        Toast.makeText(mContext, "您已评价过该视频", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.img_extends:
                    isShow = !isShow;
                    if(isShow) {
                        iv_extends.setImageResource(R.drawable.btn_zhankai_n);
                        tv_movie_desc.setVisibility(View.VISIBLE);
                    }else {
                        iv_extends.setImageResource(R.drawable.btn_shouqi_n);
                        tv_movie_desc.setVisibility(View.GONE);
                    }
                    break;
                case R.id.img_back:
                    finish();
                default:
                    break;
            }
        }
    }
}
