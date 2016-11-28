package snowson.ife.com.ifeapplication.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Scroller;

import java.util.LinkedList;
import java.util.Queue;

public class HorizontalListView extends AdapterView<ListAdapter> {

	public static interface HorizonalOnScrollListener {
		public void onHorizonalScrollL(int index);
	}

	public boolean mAlwaysOverrideTouch = true;
	protected ListAdapter mAdapter;
	private int mLeftViewIndex = -1;
	private int mRightViewIndex = 0;
	private int mDx = 0;
	private int mDisplayOffset = 0;
	protected Scroller mScroller;
	private GestureDetector mGesture;
	private Queue<View> mRemovedViewQueue = new LinkedList<View>();
	private OnItemSelectedListener mOnItemSelected;
	private OnItemClickListener mOnItemClicked;
	private OnItemLongClickListener mOnItemLongClicked;
	private boolean mDataChanged = false;
	private HorizonalOnScrollListener mOnScroll;

	public HorizontalListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
		mScroller = new Scroller(getContext());
		mGesture = new GestureDetector(getContext(), mOnGesture);
	}

	private synchronized void initView() {
		mLeftViewIndex = -1;
		mRightViewIndex = 0;
		mDisplayOffset = 0;
		mDx = 0;
	}

	public void setScrollListener(HorizonalOnScrollListener l) {
		mOnScroll = l;
	}

	@Override
	public void setOnItemSelectedListener(OnItemSelectedListener listener) {
		mOnItemSelected = listener;
	}

	@Override
	public void setOnItemClickListener(OnItemClickListener listener) {
		mOnItemClicked = listener;
	}

	@Override
	public void setOnItemLongClickListener(OnItemLongClickListener listener) {
		mOnItemLongClicked = listener;
	}

	private DataSetObserver mDataObserver = new DataSetObserver() {

		@Override
		public void onChanged() {
			synchronized (HorizontalListView.this) {
				mDataChanged = true;
			}
			invalidate();
			requestLayout();
		}

		@Override
		public void onInvalidated() {
			reset();
			invalidate();
			requestLayout();
		}

	};

	@Override
	public ListAdapter getAdapter() {
		return mAdapter;
	}

	@Override
	public View getSelectedView() {
		return null;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if (mAdapter != null) {
			mAdapter.unregisterDataSetObserver(mDataObserver);
		}
		mAdapter = adapter;
		if (mAdapter != null) {
			mAdapter.registerDataSetObserver(mDataObserver);
		}
		reset();
	}

	private synchronized void reset() {
		initView();
		removeAllViewsInLayout();
		requestLayout();
	}

	@Override
	public void setSelection(int position) {
		if (mAdapter != null) {
			initView();
			removeAllViewsInLayout();
			mRightViewIndex = position;
			mLeftViewIndex = mRightViewIndex - 1;
			requestLayout();
		}
	}

	private void addAndMeasureChild(final View child, int viewPos) {
		LayoutParams params = child.getLayoutParams();
		if (params == null) {
			params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}

		addViewInLayout(child, viewPos, params, true);
		child.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
	}

	@Override
	protected synchronized void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		if (mAdapter == null) {
			initView();
			removeAllViewsInLayout();
			return;
		}

		if (mDataChanged) {
			if(mLeftViewIndex > mAdapter.getCount()-1){
				mLeftViewIndex = mAdapter.getCount()-1;
				mDisplayOffset = 0;
			}
			mRightViewIndex = mLeftViewIndex+1;
			removeAllViewsInLayout();
			mDataChanged = false;
		}

		mDisplayOffset += mDx;

		fillList();
		removeNonVisibleItems();
		positionItems();

		mDx = 0;

	}

	private void fillList() {
		int edge = mDisplayOffset;
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			child.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
			edge += child.getMeasuredWidth();
		}
		fillListRight(edge);

		fillListLeft();
	}

	private void fillListRight(int rightEdge) {
		
		while (rightEdge < getWidth() && mRightViewIndex >=0 && mRightViewIndex < mAdapter.getCount()) {
			View child = mAdapter.getView(mRightViewIndex, mRemovedViewQueue.poll(), this);
			addAndMeasureChild(child, -1);
			rightEdge += child.getMeasuredWidth();
			mRightViewIndex++;
		}

		if (rightEdge < getWidth()) {
			mDisplayOffset += getWidth() - rightEdge;
		}

	}

	private void fillListLeft() {
		while (mDisplayOffset > 0 && mLeftViewIndex >= 0 && mLeftViewIndex < mAdapter.getCount()) {
			View child = mAdapter.getView(mLeftViewIndex, mRemovedViewQueue.poll(), this);
			addAndMeasureChild(child, 0);
			mLeftViewIndex--;
			mDisplayOffset -= child.getMeasuredWidth();
		}
		
		if (mDisplayOffset > 0) {
			mDisplayOffset = 0;
		}
	}

	private void removeNonVisibleItems() {
		View child = getChildAt(0);
		while (child != null && mDisplayOffset + child.getMeasuredWidth() < 0) {
			mDisplayOffset += child.getMeasuredWidth();
			mRemovedViewQueue.offer(child);
			removeViewInLayout(child);
			mLeftViewIndex++;
			child = getChildAt(0);
		}

		int finalLeft = mDisplayOffset;
		for (int i = 0; i < getChildCount(); i++) {
			finalLeft += getChildAt(i).getMeasuredWidth();
		}
		child = getChildAt(getChildCount() - 1);
		while (child != null && finalLeft-child.getMeasuredWidth() > getWidth()) {
			mRemovedViewQueue.offer(child);
			removeViewInLayout(child);
			mRightViewIndex--;
			finalLeft -= child.getMeasuredWidth();
			child = getChildAt(getChildCount() - 1);
		}
	}

	private void positionItems() {
		int left = mDisplayOffset;
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			int childWidth = child.getMeasuredWidth();
			child.layout(left, 0, left + childWidth, child.getMeasuredHeight());
			left += childWidth;
		}
		

		int right = mDisplayOffset;
		int index = 0;
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			int childWidth = child.getMeasuredWidth();
			right += childWidth;
			if (right > getWidth() / 2) {
				index = i + mLeftViewIndex + 1;
				break;
			}
		}
		if (mOnScroll != null) {
			mOnScroll.onHorizonalScrollL(index);
		}
	}


	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		boolean handled = super.dispatchTouchEvent(ev);
		handled |= mGesture.onTouchEvent(ev);
		return handled;
	}

	protected boolean onDown(MotionEvent e) {
		mScroller.forceFinished(true);
		return true;
	}

	private OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener() {

		@Override
		public boolean onDown(MotionEvent e) {
			return HorizontalListView.this.onDown(e);
		}


		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

			synchronized (HorizontalListView.this) {
				mDx = -(int) distanceX;
			}
			requestLayout();

			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			for (int i = 0; i < getChildCount(); i++) {
				View child = getChildAt(i);
				if (isEventWithinView(e, child)) {
					if (mOnItemClicked != null) {
						mOnItemClicked.onItemClick(HorizontalListView.this, child, mLeftViewIndex + 1 + i, mAdapter.getItemId(mLeftViewIndex + 1 + i));
					}
					if (mOnItemSelected != null) {
						mOnItemSelected.onItemSelected(HorizontalListView.this, child, mLeftViewIndex + 1 + i, mAdapter.getItemId(mLeftViewIndex + 1 + i));
					}
					break;
				}

			}
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				View child = getChildAt(i);
				if (isEventWithinView(e, child)) {
					if (mOnItemLongClicked != null) {
						mOnItemLongClicked.onItemLongClick(HorizontalListView.this, child, mLeftViewIndex + 1 + i, mAdapter.getItemId(mLeftViewIndex + 1 + i));
					}
					break;
				}

			}
		}

		private boolean isEventWithinView(MotionEvent e, View child) {
			Rect viewRect = new Rect();
			int[] childPosition = new int[2];
			child.getLocationOnScreen(childPosition);
			int left = childPosition[0];
			int right = left + child.getWidth();
			int top = childPosition[1];
			int bottom = top + child.getHeight();
			viewRect.set(left, top, right, bottom);
			return viewRect.contains((int) e.getRawX(), (int) e.getRawY());
		}
	};

}