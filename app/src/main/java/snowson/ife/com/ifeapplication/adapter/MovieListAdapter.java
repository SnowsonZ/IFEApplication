package snowson.ife.com.ifeapplication.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import snowson.ife.com.ifeapplication.R;
import snowson.ife.com.ifeapplication.VideoDetailActivity2;
import snowson.ife.com.ifeapplication.bean.VideoListInfo;
import snowson.ife.com.ifeapplication.utils.ImageUtil;

/**
 * Created by admin on 2016/11/29.
 */
public class MovieListAdapter extends BaseAdapter {

    private ArrayList<VideoListInfo> mAll;
    private VideoDetailActivity2 mContext;
    public MovieListAdapter(ArrayList<VideoListInfo> data, Context context) {
        mAll = data;
        mContext = (VideoDetailActivity2) context;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mAll == null ? 0 : mAll.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return mAll.get(arg0);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemView itemView = null;
        if (convertView == null) {
            itemView = new ItemView();
            convertView = View.inflate(mContext, R.layout.movie_detail_item, null);
            convertView.setTag(itemView);
        }else {
            itemView = (ItemView) convertView.getTag();
        }
        itemView.img_movie = (ImageView) convertView.findViewById(R.id.image);
        itemView.text_movie = (TextView) convertView.findViewById(R.id.name);
        final VideoListInfo info = mAll.get(position);
        ImageUtil.setImageView(info.videoPoster, ImageUtil.SMALL, itemView.img_movie, null);
        itemView.text_movie.setText(info.videoName);

        return convertView;
    }

    class ItemView {
        ImageView img_movie;
        TextView text_movie;
    }
}
