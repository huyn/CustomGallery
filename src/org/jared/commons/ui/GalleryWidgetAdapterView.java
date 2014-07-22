package org.jared.commons.ui;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.OnGestureListener;
import android.widget.AbsSpinner;
import android.widget.AdapterView;
import android.widget.Scroller;
import android.widget.SpinnerAdapter;

public class GalleryWidgetAdapterView extends AbsSpinner implements OnGestureListener {
	
	private boolean locked = false;
	private Scroller mScroller = null;
	private Paint paint;
	private GestureDetector gestureDetector;
	private int mItemWidth;
	private int mOffsetLeft=0;
	private int mMaxInOneLine=3;
	private int mMaxWidth;
	private FlingRunnable mFlingRunnable;
	private View mSelectedChild;
	private int mAnimationDuration = 500;
	private boolean mShouldStopFling;
	private int mOffsetMax;
	private int mScrollDistance;
	private boolean needScrollBack = false;
	private int mPaddingTop;
	
	private void initWorkspace() {
		mScroller = new Scroller(getContext());
		mFlingRunnable = new FlingRunnable();
		
		paint = new Paint();
		paint.setDither(false);
		
		gestureDetector = new GestureDetector(this);
	}
	
	public GalleryWidgetAdapterView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initWorkspace();
	}
	
	public GalleryWidgetAdapterView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initWorkspace();
	}

	public GalleryWidgetAdapterView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initWorkspace();
	}

	public void setOnClickListener(OnItemClickListener listener) {
		mOnItemClickListener = listener;
	}
	
	public OnItemClickListener getOnClickListener() {
		return mOnItemClickListener;
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		System.out.println("dispatchdraw...." + this.getChildCount());
		super.dispatchDraw(canvas);
//		if(getAdapter() != null) {
//			for(int i=0; i<getAdapter().getCount(); i++) {
//				View v = getAdapter().getView(i, null, this);
//				drawChild(canvas, v, getDrawingTime());
//			}
//		}
	}

	/**
	 * Measure the workspace AND also children
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int width = MeasureSpec.getSize(widthMeasureSpec);
		mItemCount = mAdapter.getCount();
		setUpCachedView();
		if(getAdapter() != null && getAdapter().getCount() > 0) {
			SpinnerAdapter adapter = getAdapter();
			int size = adapter.getCount();
			//平均分配childview的空间
			mItemWidth = width/mMaxInOneLine;
			mMaxWidth = size*mItemWidth;
			mOffsetMax = mMaxWidth>width?mMaxWidth-width:0;
			final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
			if (widthMode != MeasureSpec.EXACTLY) {
				throw new IllegalStateException(
						"Workspace can only be used in EXACTLY mode.");
			}

			final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
			if (heightMode != MeasureSpec.EXACTLY) {
				throw new IllegalStateException(
						"Workspace can only be used in EXACTLY mode.");
			}

			//一定要这样子。不然childview在onMeasure的时候会采用wrap_content的方式只获取最小需要的控件
			int mMeasuredWidth = MeasureSpec.makeMeasureSpec(mItemWidth, MeasureSpec.EXACTLY);
			System.out.println("mitemwidth====" + mItemWidth);
			View view = adapter.getView(0, null, this);
			view.measure(mMeasuredWidth, LayoutParams.WRAP_CONTENT);
			mPaddingTop = (MeasureSpec.getSize(heightMeasureSpec)-view.getMeasuredHeight())/2;
		}
	}

	/**
	 * Overrided method to layout child
	 */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		System.out.println("onlayout called");
//		int childLeft = mOffsetLeft;
//		if(getAdapter() != null) {
//			SpinnerAdapter adapter = getAdapter();
//			final int count = adapter.getCount();
//			for (int i = 0; i < count; i++) {
//				final View child = adapter.getView(i, null, this);
//				if (child.getVisibility() != View.GONE) {
//					final int childWidth = child.getMeasuredWidth();
//					child.layout(childLeft, mPaddingTop, childLeft + childWidth, child.getMeasuredHeight() + mPaddingTop);
//					childLeft += childWidth;
//				}
//			}
//			setSection(0);
//		}
		if(getAdapter() == null || getAdapter().getCount() == 0)
			return;
		scrollInSlots();
		setSection(0);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		boolean flag = this.gestureDetector.onTouchEvent(ev);
		//松开按键后，滑回来填补左边或者右边的空白
		if(ev.getAction() == MotionEvent.ACTION_UP) {
			mDownTouchView.setPressed(false);
			if(needScrollBack) {
				mOffsetLeft+=mScrollDistance;
				onUp();
			}
		}
		return flag;
	}
	
	private int mSelection = 0;
	//定位到某个childview
	public void setSection(int index) {
		if(getAdapter() == null || index >= getAdapter().getCount())
			return;
		int mLeftWidth = index*mItemWidth;
		int i = mLeftWidth + mOffsetLeft;
		
		performItemClick(index);
		
		if(i >=0 && i <= getWidth()-mItemWidth)
			return;
		if(i < 0)
			mFlingRunnable.startUsingDistance(Math.abs(i));
		if(i > getWidth()-mItemWidth)
			mFlingRunnable.startUsingDistance(-Math.abs(i-getWidth()+mItemWidth));
		
//		mSelection=index;
	}
	
	public void lock() {
		locked = true;
	}

	public void unLock() {
		locked = false;
	}

	private int mDownTouchPosition;
	private View mDownTouchView;
	
	
	//计算处于当前坐标下的控件的ID
	private int pointToPosition(float x, float y) {
		int xOff = (int)x-mOffsetLeft;
		if(xOff%mItemWidth != 0)
			return xOff/mItemWidth;
		return -1;
	}
	
	//执行当拉的距离有空白的时候直接倒退回来的操作
	private void onUp() {
		int distance = 0;
		if(mOffsetLeft > 0) {
			distance = -mOffsetLeft;
		} else if(mOffsetMax < Math.abs(mOffsetLeft)) {
			distance = Math.abs(mOffsetLeft)-mOffsetMax;
		} else {
			if(mOffsetLeft%mItemWidth+mItemWidth/2 <= 0)
				distance = Math.abs(mOffsetLeft)%mItemWidth;
			else
				distance = -mOffsetLeft-(Math.abs(mOffsetLeft)/mItemWidth+1)*mItemWidth;
		}
		mFlingRunnable.startUsingDistance(distance);
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		System.out.println("onDown");
		// TODO Auto-generated method stub
		mDownTouchPosition = pointToPosition(e.getX(), e.getY());
		System.out.println("ondowntouchposition:" + mDownTouchPosition);
		System.out.println(mRecycle.size());
        if (mDownTouchPosition >= 0) {
//            mDownTouchView = getAdapter().getView(mDownTouchPosition, null, this);
        	mDownTouchView = mRecycle.get(mDownTouchPosition);
            mDownTouchView.setPressed(true);
        }
		return true;
	}
	
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		System.out.println("onKeyUp");
		return super.onKeyUp(keyCode, event);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		System.out.println("onKeyDown");
		return super.onKeyDown(keyCode, event);
	}
	
	/*
	 * if velocityX is less than 0, e2.getX() is less than e1.getX()
	 * (non-Javadoc)
	 * @see android.view.GestureDetector.OnGestureListener#onFling(android.view.MotionEvent, android.view.MotionEvent, float, float)
	 */
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		needScrollBack = true;
		// TODO Auto-generated method stub
		mFlingRunnable.startUsingVelocity((int) -velocityX);
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		needScrollBack = false;
		System.out.println("onLongPress");
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		needScrollBack = true;
		mScrollDistance = (int) (e2.getX() - e1.getX());
		trackMotionScroll((int) -distanceX);
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		needScrollBack = false;
		// TODO Auto-generated method stub
		System.out.println("onShowPress");
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		needScrollBack = false;
		// TODO Auto-generated method stub
		System.out.println("onSingleTapUp-" + e.getX());
		//单击动作，通过获取坐标，然后根据offsetLeft来确定哪个childview被点击，然后调用onclick。AdapterView就是这个做法
		performItemClick(pointToPosition(e.getX(), e.getY()));
		return false;
	}

	private void performItemClick(int position) {
		if(position != -1 && getAdapter() != null && position < getAdapter().getCount()) {
			System.out.println("click position is " + position + " and leftoffset=" + mOffsetLeft);
			System.out.println("selection is " + mSelection);
			View view;
			if(mSelection != -1) {
				view = mRecycle.get(mSelection);
				view.setSelected(false);
				view.setFocusable(false);
			}

			if(mSelection != position) {
				view = mRecycle.get(position);
				view.setPressed(false);
				view.setSelected(true);
				view.setFocusable(true);
				if(hasFocus())
					view.requestFocus();
				mSelection = position;
				//execute adapterview.performItemClick
				performItemClick(view, 0, 0);
			}
		}
	}
	
	public boolean performItemClick(View view, int position, long id) {
        if (mOnItemClickListener != null) {
            playSoundEffect(SoundEffectConstants.CLICK);
            mOnItemClickListener.onItemClick(this, view, position, id);
            return true;
        }

        return false;
    }
	
	private OnItemClickListener mOnItemClickListener;
	public interface OnItemClickListener {
		
		public void onItemClick(AdapterView<?> parent, View view, int position, long id);
	}
	
	private void trackMotionScroll(int deltaX) {
        if (getAdapter() == null || getAdapter().getCount() == 0) {
            return;
        }
        mOffsetLeft+=deltaX;
        scrollInSlots();
        invalidate();
    }
	
	private void scrollInSlots() {
		int childLeft = mOffsetLeft;
		SpinnerAdapter adapter = getAdapter();
		final int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			final View child = mRecycle.get(i);
			if (child.getVisibility() != View.GONE) {
				setUpChild(child, i, childLeft);
				final int childWidth = child.getMeasuredWidth();
				childLeft += childWidth;
			}
		}
	}
	
	private void setUpChild(View child, int position, int left) {
		if(position == mSelection) {
			System.out.println("position====" + position);
			child.setFocusable(true);
			child.setSelected(true);
		}
		
	    int mMeasuredWidth = MeasureSpec.makeMeasureSpec(mItemWidth, MeasureSpec.EXACTLY);
	    // Measure child
	    child.measure(mMeasuredWidth, LayoutParams.WRAP_CONTENT);
	    child.layout(left, mPaddingTop, left+mItemWidth, child.getMeasuredHeight()+mPaddingTop);
	}
	
	private void setUpCachedView() {
		mRecycle.clear();
		detachAllViewsFromParent();
		mSelection = 0;
		
		View child;
		for(int i=0; i<mItemCount; i++) {
			child = mAdapter.getView(i, null, this);
			
			ViewGroup.LayoutParams lp = child.getLayoutParams();
		    if (lp == null) {
		        lp = generateDefaultLayoutParams();
		    }
		
		    addViewInLayout(child, -1, lp);
		    
			mRecycle.put(i, child);
		}
	}
	
	private class FlingRunnable implements Runnable {

		private int mLastFlingX;
		
		private void startCommon() {
            // Remove any pending flings
            removeCallbacks(this);
        }
		
		/*
		 * initialVelocity大于0表示往左滑动，小于0表示往右滑
		 */
		public void startUsingVelocity(int initialVelocity) {
            if (initialVelocity == 0) return;
            
            startCommon();
            int initialX = initialVelocity < 0 ? Integer.MAX_VALUE : 0;

            mLastFlingX = initialX;
            mScroller.fling(initialX, 0, initialVelocity, 0, 0, 100, 0, Integer.MAX_VALUE);
            post(this);
        }

        public void startUsingDistance(int distance) {
            if (distance == 0) return;
            
            startCommon();
            mLastFlingX = 0;
            mScroller.startScroll(0, 0, -distance, 0, mAnimationDuration);
            post(this);
        }
        
        public void stop(boolean scrollIntoSlots) {
            removeCallbacks(this);
            endFling(scrollIntoSlots);
        }
        
        private void endFling(boolean scrollIntoSlots) {
            /*
             * Force the scroller's status to finished (without setting its
             * position to the end)
             */
            mScroller.forceFinished(true);
        }
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
            if (getAdapter() == null || getAdapter().getCount() == 0) {
                endFling(true);
                return;
            }

            mShouldStopFling = false;
            
            final Scroller scroller = mScroller;
            boolean more = scroller.computeScrollOffset();
            final int x = scroller.getCurrX();

            // Flip sign to convert finger direction to list items direction
            // (e.g. finger moving down means list is moving towards the top)
            int delta = mLastFlingX - x;

            if (more && !mShouldStopFling) {
                trackMotionScroll(delta);
                mLastFlingX = x;
                post(this);
            } else {
               endFling(true);
            }
        }
		
	}
	
	private int mItemCount = 0;
	private class AdapterDataSetObserver extends DataSetObserver {
		
        @Override
        public void onChanged() {
        	mItemCount = getAdapter().getCount();
        	setUpCachedView();
            checkFocus();
            requestLayout();
        }

        @Override
        public void onInvalidated() {
            checkSelectionChanged();
            checkFocus();
            requestLayout();
        }

	}
	
	private DataSetObserver mDataSetObserver;
	private SpinnerAdapter mAdapter;
	private RecycleBin mRecycle = new RecycleBin();
	
	/**
     * The Adapter is used to provide the data which backs this Spinner.
     * It also provides methods to transform spinner items based on their position
     * relative to the selected item.
     * @param adapter The SpinnerAdapter to use for this Spinner
     */
    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        if (null != mAdapter) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
            resetList();
        }
        
        mAdapter = adapter;
        
        if (mAdapter != null) {
            mItemCount = mAdapter.getCount();
            checkFocus();

            mDataSetObserver = new AdapterDataSetObserver();
            mAdapter.registerDataSetObserver(mDataSetObserver);

            int position = mItemCount > 0 ? 0 : INVALID_POSITION;

            if (mItemCount == 0) {
                // Nothing selected
                checkSelectionChanged();
            }
            
        } else {
            checkFocus();            
            resetList();
            // Nothing selected
            checkSelectionChanged();
        }

        requestLayout();
    }
    
    @Override
    public SpinnerAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public int getCount() {
        return mItemCount;
    }
    
    void checkSelectionChanged() {
    	
    }
    
    void checkFocus() {
        final SpinnerAdapter adapter = getAdapter();
        final boolean empty = adapter == null || adapter.getCount() == 0;
        final boolean focusable = !empty;
        // The order in which we set focusable in touch mode/focusable may matter
        // for the client, see View.setFocusableInTouchMode() comments for more
        // details
        super.setFocusableInTouchMode(focusable);
        super.setFocusable(focusable);
    }
    
    /**
     * Clear out all children from the list
     */
    private void resetList() {
    	mRecycle.clear();
    	mSelection = 0;
        
        removeAllViewsInLayout();
        invalidate();
    }
    
    private class RecycleBin {
        private final SparseArray<View> mScrapHeap = new SparseArray<View>();

        public void put(int position, View v) {
            mScrapHeap.put(position, v);
        }
        
        View get(int position) {
            View result = mScrapHeap.get(position);
            return result;
        }

        void clear() {
            final SparseArray<View> scrapHeap = mScrapHeap;
            final int count = scrapHeap.size();
            for (int i = 0; i < count; i++) {
                final View view = scrapHeap.valueAt(i);
                if (view != null) {
                    removeDetachedView(view, true);
                }
            }
            scrapHeap.clear();
        }
        
        int size() {
        	return mScrapHeap.size();
        }
    }

}