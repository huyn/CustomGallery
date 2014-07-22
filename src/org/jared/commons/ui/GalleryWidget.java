package org.jared.commons.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.OnGestureListener;
import android.widget.Scroller;

public class GalleryWidget extends ViewGroup implements OnGestureListener {
	
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

	private OnItemClickListener mOnItemClickListener = null;
	
	private void initWorkspace() {
		mScroller = new Scroller(getContext());
		mFlingRunnable = new FlingRunnable();
		
		paint = new Paint();
		paint.setDither(false);
		
		gestureDetector = new GestureDetector(this);
	}
	
	public GalleryWidget(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initWorkspace();
	}
	
	public GalleryWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initWorkspace();
	}

	public GalleryWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initWorkspace();
	}

	public void setOnClickListener(View.OnClickListener listener) {
		for(int i=0; i<getChildCount(); i++) {
			getChildAt(i).setOnClickListener(null);
		}
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		System.out.println("dispatchdraw...." + this.getChildCount());
		for(int i=0; i<this.getChildCount(); i++) {
			View v = getChildAt(i);
			drawChild(canvas, v, getDrawingTime());
		}
	}

	/**
	 * Measure the workspace AND also children
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int width = MeasureSpec.getSize(widthMeasureSpec);
		int size = getChildCount();
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
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			//高度用wrap_content来避免填充整个显示区间
			getChildAt(i).measure(mMeasuredWidth, LayoutParams.WRAP_CONTENT);
		}
		if(count > 0) {
			mPaddingTop = (MeasureSpec.getSize(heightMeasureSpec)-MeasureSpec.getSize(getChildAt(0).getHeight()))/2;
		}
	}

	/**
	 * Overrided method to layout child
	 */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		System.out.println("onlayout called");
		int childLeft = mOffsetLeft;
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				final int childWidth = child.getMeasuredWidth();
				child.layout(childLeft, mPaddingTop, childLeft + childWidth, child.getMeasuredHeight() + mPaddingTop);
				childLeft += childWidth;
			}
		}
		
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
	
	//定位到某个childview
	public void setSection(int index) {
		if(getChildCount() == 0 || index >= getChildCount())
			return;
		int mLeftWidth = index*mItemWidth;
		int i = mLeftWidth + mOffsetLeft;
		if(i >=0 && i <= getWidth()-mItemWidth)
			return;
		if(i < 0)
			mFlingRunnable.startUsingDistance(Math.abs(i));
		if(i > getWidth()-mItemWidth)
			mFlingRunnable.startUsingDistance(-Math.abs(i-getWidth()+mItemWidth));
		
		performItemClick(index);
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
		mDownTouchPosition = pointToPosition((int) e.getX(), (int) e.getY());
        
        if (mDownTouchPosition >= 0) {
            mDownTouchView = getChildAt(mDownTouchPosition);
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
//		setSection(4);
		return false;
	}

	private int mLastPosition = -1;
	private void performItemClick(int position) {
		if(position != -1 && position < getChildCount()) {
			System.out.println("click position is " + position + " and leftoffset=" + mOffsetLeft);
			View view;
			if(mLastPosition != -1) {
				view = getChildAt(mLastPosition);
				view.setSelected(false);
				view.setFocusable(false);
			}

			if(mLastPosition != position) {
				view = getChildAt(position);
				view.setPressed(false);
				view.setSelected(true);
				view.setFocusable(true);
				if(hasFocus())
					view.requestFocus();
				mLastPosition = position;
				if(mOnItemClickListener != null)
					mOnItemClickListener.onItemClick(view);
			}
		}
	}
	
	public void setOnItemClickListener(OnItemClickListener listener) {
		mOnItemClickListener = listener;
	}
	
	public OnItemClickListener getOnItemClickListener() {
		return mOnItemClickListener;
	}
	
	public interface OnItemClickListener {
		
		public void onItemClick(View view);
	}
	
	private void trackMotionScroll(int deltaX) {
        if (getChildCount() == 0) {
            return;
        }
        mOffsetLeft+=deltaX;
        int childLeft = mOffsetLeft;
        final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				final int childWidth = child.getMeasuredWidth();
				child.layout(childLeft, mPaddingTop, childLeft + childWidth, child.getMeasuredHeight() + mPaddingTop);
				childLeft += childWidth;
			}
		}
        invalidate();
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
            if (getChildCount() == 0) {
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

}