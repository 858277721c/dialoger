package com.fanwe.dialoger;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import com.fanwe.lib.dialoger.impl.FDialoger;

public class TestDialoger extends FDialoger
{
    public TestDialoger(Activity activity)
    {
        super(activity);
        setDebug(true);
        /**
         * 设置窗口内容
         */
        setContentView(R.layout.dialog_view);
        /**
         * 设置窗口边距
         */
        setPadding(0, 0, 0, 0);
    }

    @Override
    protected void onContentViewAdded(View contentView)
    {
        super.onContentViewAdded(contentView);
        contentView.findViewById(R.id.btn).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(getContext(), "clicked", Toast.LENGTH_SHORT).show();
                /**
                 * 关闭窗口
                 */
                dismiss();
            }
        });
    }
}
