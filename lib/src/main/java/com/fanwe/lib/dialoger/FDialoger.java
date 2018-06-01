package com.fanwe.lib.dialoger;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.fanwe.lib.dialoger.animator.ScaleXYCreater;
import com.fanwe.lib.dialoger.utils.VisibilityAnimatorHandler;

public class FDialoger implements Dialoger
{
    private final Activity mActivity;
    private final ViewGroup mDialogParent;
    private final ViewGroup mDialogView;

    private View mContentView;
    private boolean mCancelable = true;
    private boolean mCanceledOnTouchOutside = true;

    private int mGravity = Gravity.NO_GRAVITY;

    private OnDismissListener mOnDismissListener;
    private boolean mAttach;

    private VisibilityAnimatorHandler mAnimatorHandler;
    private AnimatorCreater mDialogAnimatorCreater;
    private AnimatorCreater mContentAnimatorCreater;
    private boolean mStartShowAnimator;

    public FDialoger(Activity activity, ViewGroup dialogView)
    {
        if (activity == null)
            throw new NullPointerException("activity is null");
        if (dialogView == null)
            throw new NullPointerException("dialogView is null");

        mActivity = activity;
        mDialogParent = activity.findViewById(android.R.id.content);
        mDialogView = dialogView;

        dialogView.addOnAttachStateChangeListener(mOnAttachStateChangeListener);
        setContentAnimatorCreater(new ScaleXYCreater());
    }

    @Override
    public View getDialogView()
    {
        return mDialogView;
    }

    @Override
    public final View getContentView()
    {
        return mContentView;
    }

    @Override
    public void setContentView(int layoutId)
    {
        final View view = LayoutInflater.from(mActivity).inflate(layoutId, mDialogView, false);
        setDialogView(view);
    }

    @Override
    public void setContentView(View view)
    {
        setDialogView(view);
    }

    private void setDialogView(View view)
    {
        mContentView = view;

        final ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        final ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params != null)
        {
            p.width = params.width;
            p.height = params.height;
        }

        mDialogView.removeAllViews();
        mDialogView.addView(view, p);

        onContentViewAdded(view);
    }

    protected void onContentViewAdded(View contentView)
    {
    }

    @Override
    public void setCancelable(boolean cancel)
    {
        mCancelable = cancel;
    }

    @Override
    public void setCanceledOnTouchOutside(boolean cancel)
    {
        if (cancel && !mCancelable)
            mCancelable = true;

        mCanceledOnTouchOutside = cancel;
    }

    @Override
    public void setDialogAnimatorCreater(AnimatorCreater creater)
    {
        mDialogAnimatorCreater = creater;
    }

    @Override
    public void setContentAnimatorCreater(AnimatorCreater creater)
    {
        mContentAnimatorCreater = creater;
    }

    @Override
    public void setOnDismissListener(OnDismissListener listener)
    {
        mOnDismissListener = listener;
    }

    @Override
    public void setGravity(int gravity)
    {
        mGravity = gravity;
    }

    @Override
    public int getGravity()
    {
        return mGravity;
    }

    @Override
    public void paddingLeft(int padding)
    {
        final View view = mDialogView;
        view.setPadding(padding, view.getPaddingTop(),
                view.getPaddingRight(), view.getPaddingBottom());
    }

    @Override
    public void paddingTop(int padding)
    {
        final View view = mDialogView;
        view.setPadding(view.getPaddingLeft(), padding,
                view.getPaddingRight(), view.getPaddingBottom());
    }

    @Override
    public void paddingRight(int padding)
    {
        final View view = mDialogView;
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(),
                padding, view.getPaddingBottom());
    }

    @Override
    public void paddingBottom(int padding)
    {
        final View view = mDialogView;
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(),
                view.getPaddingRight(), padding);
    }

    @Override
    public void paddings(int paddings)
    {
        mDialogView.setPadding(paddings, paddings, paddings, paddings);
    }

    @Override
    public void show()
    {
        attach(true);
    }

    @Override
    public boolean isShowing()
    {
        return mDialogView.getParent() == mDialogParent;
    }

    @Override
    public void dismiss()
    {
        attach(false);
    }

    @Override
    public void startDismissRunnable(long delay)
    {
        stopDismissRunnable();
        getHandler().postDelayed(mDismissRunnable, delay);
    }

    @Override
    public void stopDismissRunnable()
    {
        getHandler().removeCallbacks(mDismissRunnable);
    }

    private Handler mHandler;

    private Handler getHandler()
    {
        if (mHandler == null)
            mHandler = new Handler(Looper.getMainLooper());
        return mHandler;
    }

    private final Runnable mDismissRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            dismiss();
        }
    };

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        if (mStartShowAnimator)
        {
            getAnimatorHandler().setShowAnimator(createAnimator(true));
            getAnimatorHandler().startShowAnimator();
            mStartShowAnimator = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (!isViewUnder(mContentView, (int) event.getX(), (int) event.getY()))
        {
            if (mCanceledOnTouchOutside)
                dismiss();
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (isShowing() && keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (mCancelable)
                dismiss();
            return true;
        }
        return false;
    }

    private boolean isViewUnder(View view, int x, int y)
    {
        if (view == null)
            return false;

        return x >= view.getLeft() && x < view.getRight()
                && y >= view.getTop() && y < view.getBottom();
    }

    private void attach(boolean attach)
    {
        if (mAttach == attach)
            return;

        if (attach)
        {
            if (!isShowing() && mContentView != null)
            {
                if (mGravity == Gravity.NO_GRAVITY)
                    setGravity(Gravity.CENTER);

                onStart();
                mDialogParent.addView(mDialogView);
            }
        } else
        {
            if (isShowing() && !mActivity.isFinishing())
            {
                getAnimatorHandler().setHideAnimator(createAnimator(false));
                if (!getAnimatorHandler().startHideAnimator())
                    removeDialogView();
            }
        }

        mAttach = attach;
    }

    private boolean mRemoveByAnimator;

    private VisibilityAnimatorHandler getAnimatorHandler()
    {
        if (mAnimatorHandler == null)
        {
            mAnimatorHandler = new VisibilityAnimatorHandler();
            mAnimatorHandler.setHideAnimatorListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    super.onAnimationEnd(animation);
                    mRemoveByAnimator = true;
                    removeDialogView();
                }
            });
        }
        return mAnimatorHandler;
    }

    private Animator createAnimator(boolean show)
    {
        Animator animator = null;

        final Animator dialogAnimator = mDialogAnimatorCreater != null ?
                mDialogAnimatorCreater.createAnimator(show, mDialogView) : null;

        final Animator contentAnimator = (mContentAnimatorCreater != null && getContentView() != null) ?
                mContentAnimatorCreater.createAnimator(show, getContentView()) : null;

        if (dialogAnimator != null && contentAnimator != null)
        {
            final AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(dialogAnimator).with(contentAnimator);
            animator = animatorSet;
        } else if (dialogAnimator != null)
        {
            animator = dialogAnimator;
        } else if (contentAnimator != null)
        {
            animator = contentAnimator;
        }
        return animator;
    }

    private final View.OnAttachStateChangeListener mOnAttachStateChangeListener = new View.OnAttachStateChangeListener()
    {
        @Override
        public void onViewAttachedToWindow(View v)
        {
            if (mDialogView.getParent() != mDialogParent)
                throw new RuntimeException("dialog view can not be add to:" + mDialogView.getParent());
            mStartShowAnimator = true;
        }

        @Override
        public void onViewDetachedFromWindow(View v)
        {
            if (mAttach && !mActivity.isFinishing())
                throw new RuntimeException("you must call dismiss() method to remove dialog view");
            mStartShowAnimator = false;
            stopDismissRunnable();

            if (!mRemoveByAnimator)
                getAnimatorHandler().cancelAnimators();

            if (mOnDismissListener != null)
                mOnDismissListener.onDismiss(FDialoger.this);

            mRemoveByAnimator = false;
        }
    };

    private void removeDialogView()
    {
        try
        {
            final ViewParent parent = mDialogView.getParent();
            if (parent instanceof ViewGroup)
            {
                onStop();
                ((ViewGroup) parent).removeView(mDialogView);
            }
        } catch (Exception e)
        {
        }
    }

    /**
     * dialog要显示之前的回调
     */
    protected void onStart()
    {
    }

    /**
     * dialog要关闭之前的回调
     */
    protected void onStop()
    {
    }
}