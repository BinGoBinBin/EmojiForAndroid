/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Umeng, Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.pikachu.emoji;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.pikachu.emoji.EmojiBorad.OnEmojiItemClickListener;
import com.pikachu.emoji.utils.ResFinder;
import com.pikachu.emoji.utils.ResFinder.ResType;

/**
 * 
 */
public class EmojiView extends RelativeLayout {

    private View mSendView;
    private ImageView mEmojiImageView;
    private CommentEditText mEditText;
    private EmojiBorad mEmojiBoard;

    private int totalTime = 0;
    private boolean isFinish = false;

    private int INPUT_METHOD_SHOW = 0x00;
    private int INPUT_METHOD_DISMISS = 0x01;

    /**
     * 检测输入法是否显示的空闲时间
     */
    private static final int IDLE = 50;
    /**
     * 关闭输入法总共尝试3000ms
     */
    private static final int LIMIT_TIME = 3000;

    private InputMethodManager mInputMethodManager;
    private Activity mActivity;
    private int mEmojiIconRes = 0;
    private int mEmojiKeyboardRes = 0;

    private BaseInputConnection mInputConnection = null;

    /**
     * 该Handler主要处理软键盘的弹出跟隐藏
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            View view = (View) msg.obj;
            // 显示软键盘
            if (msg.what == INPUT_METHOD_SHOW) {
                boolean result = mInputMethodManager.showSoftInput(view, 0);
                if (!result && totalTime < LIMIT_TIME) {
                    totalTime += IDLE;
                    Message message = Message.obtain(msg);
                    mHandler.sendMessageDelayed(message, IDLE);
                } else if (!isFinish) {
                    totalTime = 0;
                    result = view.requestFocus();
                    isFinish = true;
                }
            } else if (msg.what == INPUT_METHOD_DISMISS) {
                // 隐藏软键盘
                mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    };

    /**
     * @param context
     */
    public EmojiView(Context context) {
        super(context);
        initView();
    }

    /**
     * @param context
     * @param attrs
     */
    public EmojiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    /**
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public EmojiView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        Context tempContext = getContext().getApplicationContext();
        if (tempContext != null) {
            ResFinder.initContext(tempContext);
        } else {
            ResFinder.initContext(getContext());
        }

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);

        View rootView = LayoutInflater.from(getContext()).inflate(
                ResFinder.getLayout("emoji_view"), null);
        RelativeLayout.LayoutParams childParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        childParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        addView(rootView, childParams);

        mInputMethodManager = (InputMethodManager) getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);

        mSendView = rootView.findViewById(ResFinder.getId("emoji_send"));
        mEmojiImageView = (ImageView) rootView.findViewById(ResFinder.getId("emoji_imageview"));
        mEditText = (CommentEditText) rootView.findViewById(ResFinder.getId("emoji_editview"));
        mEmojiBoard = (EmojiBorad) rootView.findViewById(ResFinder.getId("emoji_board"));
        mEmojiIconRes = ResFinder.getResourceId(ResType.DRAWABLE, "emoji_icon");
        mEmojiKeyboardRes = ResFinder.getResourceId(ResType.DRAWABLE, "emoji_keyboard");

        mInputConnection = new BaseInputConnection(mEditText, true);

        mEmojiImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mEmojiBoard.getVisibility() == View.VISIBLE) { // 显示输入法，隐藏表情board
                    mEmojiBoard.setVisibility(View.GONE);
                    mEmojiImageView.setImageResource(mEmojiIconRes);
                    mActivity.getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                                    | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    sendInputMethodMessage(INPUT_METHOD_SHOW, mEditText);
                } else { // 隐藏输入法，显示表情board
                    mEmojiImageView.setImageResource(mEmojiKeyboardRes);
                    sendInputMethodMessage(INPUT_METHOD_DISMISS, mEditText);
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            mEmojiBoard.setVisibility(View.VISIBLE);
                        }
                    }, 80);
                }
            }
        });

        // 点击表情的某一项的回调函数
        mEmojiBoard.setOnEmojiItemClickListener(new OnEmojiItemClickListener() {

            @Override
            public void onItemClick(EmojiBean emojiBean) {
                // delete event
                if (EmojiBorad.DELETE_KEY.equals(emojiBean.getEmoji())) {
                    // 对于删除事件，此时模拟一个输入法上的删除事件达到删除的效果
                    // 【注意：此处不能调用delete方法，原因是emoji有些是单字符，有的是双字符】
                    mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                            KeyEvent.KEYCODE_DEL));
                    return;
                }

                int start = mEditText.getSelectionStart();
                int end = mEditText.getSelectionEnd();
                if (start < 0) {
                    mEditText.append(emojiBean.getEmoji());
                } else {
                    mEditText.getText().replace(Math.min(start, end), Math.max(start, end),
                            emojiBean.getEmoji(), 0, emojiBean.getEmoji().length());
                }
            }
        });

        // 此时如果点击其它区域，需要隐藏表情面板
        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mEmojiBoard.setVisibility(View.GONE);
                }
            }
        });

    }

    public void setSendOnclickListener(OnClickListener onClickListener) throws Exception {
        if (onClickListener == null) {
            throw new Exception("click event callback is null");
        }
        mSendView.setOnClickListener(onClickListener);
    }

    public void resetEditText() {
        mEditText.setText("");
    }

    public void attachActivity(Activity activity) {
        this.mActivity = activity;
    }

    protected void sendInputMethodMessage(int type, View view) {
        Message message = mHandler.obtainMessage(type);
        message.obj = view;
        mHandler.sendMessage(message);
    }

}
