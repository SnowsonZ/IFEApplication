package snowson.ife.com.ifeapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fairlink.common.BaseHttpTask.HttpTaskCallback;

import java.util.ArrayList;

import snowson.ife.com.ifeapplication.adapter.MovieListAdapter;
import snowson.ife.com.ifeapplication.bean.VideoListInfo;
import snowson.ife.com.ifeapplication.request.MovieDetailRequest;
import snowson.ife.com.ifeapplication.request.MovieDetailRequest.MovieDetail;
import snowson.ife.com.ifeapplication.request.VideoDetailByParentIdRequest;
import snowson.ife.com.ifeapplication.request.VideoListDetailByParentIdRequest;
import snowson.ife.com.ifeapplication.utils.ImageUtil;
import snowson.ife.com.ifeapplication.view.HorizontalListView;

public class VideoDetailActivity2 extends BaseActivity {

    private HorizontalListView mHListView;
    private MovieListAdapter mAdapter;
    private ArrayList<VideoListInfo> mDatas;
    private ImageView iv_ding,iv_cai,iv_extends;
    private TextView tv_ding_num, tv_cai_num, tv_movie_desc;
    private ClickEventListener mClickListener;
    private boolean isCommited = false;
    private boolean isShow = true;
    private TextView mTv_actor_name;
    private TextView mTv_video_desc;
    private TextView mTv_name;
    private ImageView mIv_back, img_poster;
    private Button btn_play, btn_play_now;
    private MovieDetail currentMDetail = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail2);
        initView();
        initDatas();
        initListener();
    }

    private void initListener() {
        mClickListener = new ClickEventListener(this);
        iv_cai.setOnClickListener(mClickListener);
        iv_ding.setOnClickListener(mClickListener);
        iv_extends.setOnClickListener(mClickListener);
        btn_play.setOnClickListener(mClickListener);
        mIv_back.setOnClickListener(mClickListener);
        btn_play_now.setOnClickListener(mClickListener);
        mHListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(VideoDetailActivity2.this, VideoDetailActivity2.class);
                Bundle bundle = new Bundle();
                bundle.putInt("videoId", Integer.parseInt(mDatas.get(position).videoId));
                intent.putExtras(bundle);
                VideoDetailActivity2.this.startActivity(intent);
            }
        });
    }

    private void initView() {
        mHListView = (HorizontalListView) findViewById(R.id.movie_list);
        iv_cai = (ImageView) findViewById(R.id.img_cai);
        iv_ding = (ImageView) findViewById(R.id.img_ding);
        tv_cai_num = (TextView) findViewById(R.id.tv_cai_num);
        tv_ding_num = (TextView) findViewById(R.id.tv_ding_num);
        iv_extends = (ImageView) findViewById(R.id.img_extends);
        tv_movie_desc = (TextView) findViewById(R.id.tv_movie_desc);
        mTv_actor_name = (TextView) findViewById(R.id.tv_actorname);
        mTv_video_desc = (TextView) findViewById(R.id.tv_movie_desc);
        mTv_name = (TextView) findViewById(R.id.tv_name);
        mIv_back = (ImageView) findViewById(R.id.img_back);
        btn_play = (Button) findViewById(R.id.btn_play);
        img_poster = (ImageView) findViewById(R.id.large_poster);
        btn_play_now = (Button) findViewById(R.id.btn_play_now);
    }

    private HttpTaskCallback VideoDetailCallBack = new HttpTaskCallback() {
        @Override
        public void onGetResult(int requestType, Object result) {
            if (result == null) {
                Toast.makeText(VideoDetailActivity2.this, "暂时无法获取视频信息", Toast.LENGTH_SHORT).show();
                return;
            }
            MovieDetail detail = (MovieDetail) result;
            currentMDetail = detail;
            tv_ding_num.setText(String.valueOf(detail.positiveCount));
            tv_cai_num.setText(String.valueOf(detail.negativeCount));
            ImageUtil.setImageView(detail.image, ImageUtil.MID, img_poster, null);
            mTv_actor_name.setText(detail.actor);
            mTv_video_desc.setText(detail.content);
            mTv_name.setText(detail.name);
        }

        @Override
        public void onError(int requestType) {
            Toast.makeText(VideoDetailActivity2.this, "暂时无法获取视频信息", Toast.LENGTH_SHORT).show();
        }
    };

    private HttpTaskCallback VideoListCallBack = new HttpTaskCallback() {
        @Override
        public void onGetResult(int requestType, Object result) {
            if(result != null) {
                mDatas = (ArrayList<VideoListInfo>) result;
                mAdapter = new MovieListAdapter(mDatas, VideoDetailActivity2.this);
                mHListView.setAdapter(mAdapter);
            }else {
                Toast.makeText(VideoDetailActivity2.this, "暂无其他视频信息", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public void onError(int requestType) {
            Toast.makeText(VideoDetailActivity2.this, "暂无其他视频信息", Toast.LENGTH_SHORT).show();
        }
    };

    private HttpTaskCallback mMovieDetailByParentIdCallBack = new HttpTaskCallback() {
        @Override
        public void onGetResult(int requestType, Object result) {
            if(result != null) {
                MovieDetail detail = (MovieDetail) result;
                currentMDetail = detail;
                new VideoListDetailByParentIdRequest(Integer.parseInt(detail.id), VideoListCallBack).execute((String)null);
                tv_ding_num.setText(String.valueOf(detail.positiveCount));
                tv_cai_num.setText(String.valueOf(detail.negativeCount));
                ImageUtil.setImageView(detail.image, ImageUtil.MID, img_poster, null);
                mTv_actor_name.setText(detail.actor);
                mTv_video_desc.setText(detail.content);
                mTv_name.setText(detail.name);
            }else {
                Toast.makeText(VideoDetailActivity2.this, "暂时无法获取视频信息", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onError(int requestType) {
            Toast.makeText(VideoDetailActivity2.this, "暂时无法获取视频信息", Toast.LENGTH_SHORT).show();
        }
    };

    private void initDatas() {

        mDatas = new ArrayList<VideoListInfo>();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        boolean isComment = bundle.getBoolean("isComment");
        int videoId = bundle.getInt("videoId");
        if(isComment) {
            int commentId = bundle.getInt("commentId");
            new VideoDetailByParentIdRequest(commentId, mMovieDetailByParentIdCallBack).execute((String)null);
        }else {
            new MovieDetailRequest(videoId, VideoDetailCallBack).execute((String)null);
            new VideoListDetailByParentIdRequest(videoId, VideoListCallBack).execute((String)null);
        }
    }


    class ClickEventListener implements View.OnClickListener {

        private VideoDetailActivity2 mContext = null;

        public ClickEventListener(VideoDetailActivity2 context) {
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
                    break;
                case R.id.btn_play:
                case R.id.btn_play_now:
                    if(currentMDetail == null) {
                        return;
                    }
                    if (currentMDetail.items == null || currentMDetail.items.isEmpty()) {
                       Toast.makeText(mContext, "该视频暂无法播放", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(mContext, VideoPlayerActivity.class);
                    intent.putExtra("name", currentMDetail.name);
                    intent.putExtra("img", currentMDetail.image);
                    intent.putExtra("id", currentMDetail.id);
                    intent.putExtra("videoPath", currentMDetail.items.get(0).location);
                    mContext.startActivity(intent);
                    mContext.overridePendingTransition(0, 0);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
