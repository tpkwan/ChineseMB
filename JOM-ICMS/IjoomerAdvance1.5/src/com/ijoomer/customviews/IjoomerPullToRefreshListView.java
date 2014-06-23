package com.ijoomer.customviews;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ijoomer.src.R;
import com.smart.android.framework.PullToRefreshListView;

/**
 * Created by tasol on 8/8/13.
 */
public class IjoomerPullToRefreshListView extends ListView implements AbsListView.OnScrollListener {

	private static final int TAP_TO_REFRESH = 1;
	private static final int PULL_TO_REFRESH = 2;
	private static final int RELEASE_TO_REFRESH = 3;
	protected static final int REFRESHING = 4;

	protected static final String TAG = "PullToRefreshListView";

	private OnRefreshListener mOnRefreshListener;

	/**
	 * Listener that will receive notifications every time the list scrolls.
	 */
	private OnScrollListener mOnScrollListener;
	protected LayoutInflater mInflater;

	// header
	private RelativeLayout relPullToRefresh;
	private TextView txtPullToRefresh;
	private ImageView imgPullToRefresh;
	private ProgressBar pbrLoading;

	protected int mCurrentScrollState;
	protected int mRefreshState;

	private RotateAnimation mFlipAnimation;
	protected RotateAnimation mReverseFlipAnimation;

	private int mRefreshViewHeight;
	private int mRefreshOriginalTopPadding;
	private int mLastMotionY;

	private boolean mBounceHack;

	private int imageId;

	public int getImageId() {
		return imageId;
	}

	public void setImageId(int imageId) {
		this.imageId = imageId;
	}

	public Bitmap getImageBitamp() {
		return imageBitamp;
	}

	public void setImageBitamp(Bitmap imageBitamp) {
		this.imageBitamp = imageBitamp;
	}

	public String getPullText() {
		return pullText;
	}

	public void setPullText(String pullText) {
		this.pullText = pullText;
	}

	public String getReleaseText() {
		return releaseText;
	}

	public void setReleaseText(String releaseText) {
		this.releaseText = releaseText;
	}

	public String getLoadingText() {
		return loadingText;
	}

	public void setLoadingText(String loadingText) {
		this.loadingText = loadingText;
	}

	private Bitmap imageBitamp;
	private String pullText;
	private String releaseText;
	private String loadingText;

	public IjoomerPullToRefreshListView(Context context) {
		super(context);
		init(context);
	}

	public IjoomerPullToRefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public IjoomerPullToRefreshListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	protected void init(Context context) {
		// Load all of the animations we need in code rather than through XML
		mFlipAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		mFlipAnimation.setInterpolator(new LinearInterpolator());
		mFlipAnimation.setDuration(250);
		mFlipAnimation.setFillAfter(true);
		mReverseFlipAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
		mReverseFlipAnimation.setDuration(250);
		mReverseFlipAnimation.setFillAfter(true);

		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// header
		relPullToRefresh = (RelativeLayout) mInflater.inflate(R.layout.ijoomer_pull_to_refresh_listview, this, false);
		txtPullToRefresh = (TextView) relPullToRefresh.findViewById(R.id.txtPullToRefresh);
		imgPullToRefresh = (ImageView) relPullToRefresh.findViewById(R.id.imgPullToRefresh);
		pbrLoading = (ProgressBar) relPullToRefresh.findViewById(R.id.pbrLoading);

		imgPullToRefresh.setMinimumHeight(50);
		relPullToRefresh.setOnClickListener(new OnClickRefreshListener());
		mRefreshOriginalTopPadding = relPullToRefresh.getPaddingTop();

		mRefreshState = TAP_TO_REFRESH;

		addHeaderView(relPullToRefresh);

		super.setOnScrollListener(this);

		measureView(relPullToRefresh);
		mRefreshViewHeight = relPullToRefresh.getMeasuredHeight();
		
		if (getPullText() != null && getPullText().trim().length() > 0) {
			txtPullToRefresh.setText(getPullText());
		} else {
			txtPullToRefresh.setText(R.string.pull_to_refresh_pull_label);
		}
		// Replace refresh drawable with arrow drawable
		if (getImageBitamp() != null) {
			imgPullToRefresh.setImageBitmap(getImageBitamp());
		} else if (getImageId() != 0) {
			imgPullToRefresh.setImageResource(getImageId());
		} else {
			imgPullToRefresh.setImageResource(R.drawable.ijoomer_pull_to_refresh_arrow);
		}
	}
	

	@Override
	protected void onAttachedToWindow() {
		// have to ask super to attach to window, otherwise it won't scroll in
		// jelly bean.
		super.onAttachedToWindow();
		setSelection(1);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);

		setSelection(1);
	}

	/**
	 * Set the listener that will receive notifications every time the list
	 * scrolls.
	 * 
	 * @param l
	 *            The scroll listener.
	 */
	@Override
	public void setOnScrollListener(AbsListView.OnScrollListener l) {
		mOnScrollListener = l;
	}

	/**
	 * Register a callback to be invoked when this list should be refreshed.
	 * 
	 * @param onRefreshListener
	 *            The callback to run.
	 */
	public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
		mOnRefreshListener = onRefreshListener;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int y = (int) event.getY();
		mBounceHack = false;

		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			if (!isVerticalScrollBarEnabled()) {
				setVerticalScrollBarEnabled(true);
			}
			if (getFirstVisiblePosition() == 0 && mRefreshState != REFRESHING) {
				if ((relPullToRefresh.getBottom() >= mRefreshViewHeight || relPullToRefresh.getTop() >= 0) && mRefreshState == RELEASE_TO_REFRESH) {
					// Initiate the refresh
					mRefreshState = REFRESHING;
					prepareForRefresh();
					onRefresh();
				} else if (relPullToRefresh.getBottom() < mRefreshViewHeight || relPullToRefresh.getTop() <= 0) {
					// Abort refresh and scroll down below the refresh view
					resetHeader();
					setSelection(1);
				}
			}
			break;
		case MotionEvent.ACTION_DOWN:
			mLastMotionY = y;
			break;
		case MotionEvent.ACTION_MOVE:
			applyHeaderPadding(event);
			break;
		}
		return super.onTouchEvent(event);
	}

	private void applyHeaderPadding(MotionEvent ev) {
		// getHistorySize has been available since API 1
		int pointerCount = ev.getHistorySize();

		for (int p = 0; p < pointerCount; p++) {
			if (mRefreshState == RELEASE_TO_REFRESH) {
				if (isVerticalFadingEdgeEnabled()) {
					setVerticalScrollBarEnabled(false);
				}

				int historicalY = (int) ev.getHistoricalY(p);

				// Calculate the padding to apply, we divide by 1.7 to
				// simulate a more resistant effect during pull.
				int topPadding = (int) (((historicalY - mLastMotionY) - mRefreshViewHeight) / 1.7);

				relPullToRefresh.setPadding(relPullToRefresh.getPaddingLeft(), topPadding, relPullToRefresh.getPaddingRight(), relPullToRefresh.getPaddingBottom());
			}
		}
	}

	/**
	 * Sets the header padding back to original size.
	 */
	private void resetHeaderPadding() {
		relPullToRefresh.setPadding(relPullToRefresh.getPaddingLeft(), mRefreshOriginalTopPadding, relPullToRefresh.getPaddingRight(), relPullToRefresh.getPaddingBottom());
	}

	/**
	 * Resets the header to the original state.
	 */
	private void resetHeader() {
		if (mRefreshState != TAP_TO_REFRESH) {
			mRefreshState = TAP_TO_REFRESH;

			resetHeaderPadding();

			// Set refresh view text to the pull label
			if (getPullText() != null && getPullText().trim().length() > 0) {
				txtPullToRefresh.setText(getPullText());
			} else {
				txtPullToRefresh.setText(R.string.pull_to_refresh_pull_label);
			}
			// Replace refresh drawable with arrow drawable
			if (getImageBitamp() != null) {
				imgPullToRefresh.setImageBitmap(getImageBitamp());
			} else if (getImageId() != 0) {
				imgPullToRefresh.setImageResource(getImageId());
			} else {
				imgPullToRefresh.setImageResource(R.drawable.ijoomer_pull_to_refresh_arrow);
			}
			// Clear the full rotation animation
			imgPullToRefresh.clearAnimation();
			// Hide progress bar and arrow.
			imgPullToRefresh.setVisibility(View.VISIBLE);
			pbrLoading.setVisibility(View.GONE);
		}
	}

	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		}

		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// When the refresh view is completely visible, change the text to say
		// "Release to refresh..." and flip the arrow drawable.
		if (mCurrentScrollState == SCROLL_STATE_TOUCH_SCROLL && mRefreshState != REFRESHING) {
			if (firstVisibleItem == 0) {
				imgPullToRefresh.setVisibility(View.VISIBLE);
				if ((relPullToRefresh.getBottom() >= mRefreshViewHeight + 20 || relPullToRefresh.getTop() >= 0) && mRefreshState != RELEASE_TO_REFRESH) {
					if(getReleaseText()!=null && getReleaseText().trim().length()>0){
						txtPullToRefresh.setText(getReleaseText());
					}else{
						txtPullToRefresh.setText(R.string.pull_to_refresh_release_label);
					}
					imgPullToRefresh.clearAnimation();
					imgPullToRefresh.startAnimation(mFlipAnimation);
					mRefreshState = RELEASE_TO_REFRESH;
				} else if (relPullToRefresh.getBottom() < mRefreshViewHeight + 20 && mRefreshState != PULL_TO_REFRESH) {
					if(getReleaseText()!=null && getReleaseText().trim().length()>0){
						txtPullToRefresh.setText(getReleaseText());
					}else{
						txtPullToRefresh.setText(R.string.pull_to_refresh_release_label);
					}
					if (mRefreshState != TAP_TO_REFRESH) {
						imgPullToRefresh.clearAnimation();
						imgPullToRefresh.startAnimation(mReverseFlipAnimation);
					}
					mRefreshState = PULL_TO_REFRESH;
				}
			} else {
				resetHeader();
			}
		} else if (mCurrentScrollState == SCROLL_STATE_FLING && firstVisibleItem == 0 && mRefreshState != REFRESHING) {
			setSelection(1);
			mBounceHack = true;
		} else if (mBounceHack && mCurrentScrollState == SCROLL_STATE_FLING) {
			setSelection(1);
		}

		if (mOnScrollListener != null) {
			mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {
		mCurrentScrollState = scrollState;

		if (mCurrentScrollState == SCROLL_STATE_IDLE) {
			mBounceHack = false;
		}

		if (mOnScrollListener != null) {
			mOnScrollListener.onScrollStateChanged(view, scrollState);
		}

	}

	public void prepareForRefresh() {
		resetHeaderPadding();

		imgPullToRefresh.setVisibility(View.GONE);
		pbrLoading.setVisibility(View.VISIBLE);

		// We need this hack, otherwise it will keep the previous drawable.
		imgPullToRefresh.setImageDrawable(null);

		// Set refresh view text to the refreshing label
		if(getLoadingText()!=null && getLoadingText().trim().length()>0){
			txtPullToRefresh.setText(getLoadingText());
		}else{
			txtPullToRefresh.setText(R.string.pull_to_refresh_loading);
		}

		mRefreshState = REFRESHING;
	}

	public void onRefresh() {

		if (mOnRefreshListener != null) {
			mOnRefreshListener.onRefresh();
		}
	}

	/**
	 * Resets the list to a normal state after a refresh.
	 * 
	 * @param lastUpdated
	 *            Last updated at.
	 */
	public void onRefreshComplete(CharSequence lastUpdated) {
		onRefreshComplete();
	}

	/**
	 * Resets the list to a normal state after a refresh.
	 */
	public void onRefreshComplete() {
		resetHeader();

		// If refresh view is visible when loading completes, scroll down to
		// the next item.
		if (relPullToRefresh.getBottom() > 0) {
			invalidateViews();
			setSelection(1);
		}
	}

	/**
	 * Invoked when the refresh view is clicked on. This is mainly used when
	 * there's only a few items in the list and it's not possible to drag the
	 * list.
	 */
	private class OnClickRefreshListener implements OnClickListener {

		public void onClick(View v) {
			if (mRefreshState != REFRESHING) {
				prepareForRefresh();
				onRefresh();
			}
		}

	}

	/**
	 * Interface definition for a callback to be invoked when list should be
	 * refreshed.
	 */
	public interface OnRefreshListener {
		/**
		 * Called when the list should be refreshed.
		 * <p>
		 * A call to {@link PullToRefreshListView #onRefreshComplete()} is
		 * expected to indicate that the refresh has completed.
		 */
		public void onRefresh();
	}

}
