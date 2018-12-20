package com.sd.lib.dialoger.animator;

import android.animation.Animator;
import android.view.View;

import com.sd.lib.dialoger.Dialoger;

/**
 * 在动画开始的时候修改view的锚点，动画结束后还原view的锚点
 */
public class PivotCreater extends BaseAnimatorCreater
{
    private final Dialoger.AnimatorCreater mCreater;

    private PivotProvider mPivotProviderX;
    private PivotProvider mPivotProviderY;

    private PivotHolder mPivotHolder;

    public PivotCreater(Dialoger.AnimatorCreater creater, PivotProvider pivotProviderX, PivotProvider pivotProviderY)
    {
        if (creater == null)
            throw new NullPointerException("creater is null");

        mCreater = creater;
        mPivotProviderX = pivotProviderX;
        mPivotProviderY = pivotProviderY;
    }

    protected final PivotHolder getPivotHolder()
    {
        if (mPivotHolder == null)
            mPivotHolder = new PivotHolder();
        return mPivotHolder;
    }

    @Override
    protected final Animator onCreateAnimator(boolean show, View view)
    {
        return mCreater.createAnimator(show, view);
    }

    @Override
    protected void onAnimationStart(boolean show, View view)
    {
        super.onAnimationStart(show, view);
        if (mPivotProviderX == null)
        {
            mPivotProviderX = new PivotProvider()
            {
                @Override
                public float getPivot(boolean show, View view)
                {
                    return view.getPivotX();
                }
            };
        }
        if (mPivotProviderY == null)
        {
            mPivotProviderY = new PivotProvider()
            {
                @Override
                public float getPivot(boolean show, View view)
                {
                    return view.getPivotY();
                }
            };
        }

        getPivotHolder().setPivotXY(mPivotProviderX.getPivot(show, view), mPivotProviderY.getPivot(show, view), view);
    }

    @Override
    protected void onAnimationEnd(boolean show, View view)
    {
        super.onAnimationEnd(show, view);
        getPivotHolder().restore(view);
    }

    private static class PivotHolder
    {
        private final float[] mPivotXYOriginal = new float[2];

        public void setPivotXY(float pivotX, float pivotY, View view)
        {
            if (view == null)
                return;

            mPivotXYOriginal[0] = view.getPivotX();
            mPivotXYOriginal[1] = view.getPivotY();

            view.setPivotX(pivotX);
            view.setPivotY(pivotY);
        }

        public void restore(View view)
        {
            if (view == null)
                return;

            view.setPivotX(mPivotXYOriginal[0]);
            view.setPivotY(mPivotXYOriginal[1]);
        }
    }

    public interface PivotProvider
    {
        float getPivot(boolean show, View view);
    }
}
