package com.mylhyl.circledialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.mylhyl.circledialog.view.BuildViewImpl;
import com.mylhyl.circledialog.view.ItemsButton;
import com.mylhyl.circledialog.view.MultipleButton;
import com.mylhyl.circledialog.view.SingleButton;

/**
 * handler 使用
 * <p>
 * Created by hupei on 2017/3/29.
 */

public class Controller {
    private Context mContext;
    private CircleParams mParams;
    private BuildView mCreateView;
    private ButtonHandler mHandler;
    private static final int MSG_DISMISS_DIALOG = -1;
    /**
     * The identifier for the positive button.
     */
    public static final int BUTTON_POSITIVE = 1;

    /**
     * The identifier for the negative button.
     */
    public static final int BUTTON_NEGATIVE = 2;

    /**
     * The identifier for the neutral button.
     */
    public static final int BUTTON_NEUTRAL = 3;

    private BaseCircleDialog mDialog;

    public static class ButtonHandler extends Handler {


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BUTTON_POSITIVE:
                case BUTTON_NEGATIVE:
                case BUTTON_NEUTRAL:
                    ((OnClickListener) msg.obj).onClick((View) msg.obj, msg.what);
                    break;

                case MSG_DISMISS_DIALOG:
                    ((BaseCircleDialog) msg.obj).dismiss();
            }
        }

    }

    public Controller(Context context, CircleParams params, BaseCircleDialog mDialog) {
        this.mContext = context;
        this.mParams = params;
        this.mDialog = mDialog;
        mHandler = new ButtonHandler();
        mCreateView = new BuildViewImpl(mContext, mParams);
    }

    public View createView() {
        applyRoot();
        applyHeader();
        applyBody();
        return getView();
    }

    public void refreshView() {
        mCreateView.refreshText();
        mCreateView.refreshItems();
        mCreateView.refreshProgress();
        mCreateView.refreshMultipleButtonText();
        mCreateView.refreshSingleButtonText();
        //刷新时带动画
        if (mParams.dialogParams.refreshAnimation != 0 && getView() != null)
            getView().post(new Runnable() {
                @Override
                public void run() {
                    Animation animation = AnimationUtils.loadAnimation(mContext, mParams
                            .dialogParams
                            .refreshAnimation);
                    if (animation != null) getView().startAnimation(animation);
                }
            });
    }

    private void applyRoot() {
        mCreateView.buildRoot();
    }

    private void applyHeader() {
        if (mParams.titleParams != null)
            mCreateView.buildTitle();
    }

    private void applyBody() {
        //文本
        if (mParams.textParams != null) {
            mCreateView.buildText();
            applyButton();
        }
        //列表
        else if (mParams.itemsParams != null) {
            mCreateView.buildItems();
            //有确定或者有取消按钮
            if (mParams.positiveParams != null || mParams.negativeParams != null) {
                final ItemsButton itemsButton = mCreateView.buildItemsButton();
                itemsButton.regOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mHandler.obtainMessage(BUTTON_NEGATIVE, itemsButton);
                        mHandler.obtainMessage(MSG_DISMISS_DIALOG, mDialog)
                                .sendToTarget();
                    }
                });
            }

        }
        //进度条
        else if (mParams.progressParams != null) {
            mCreateView.buildProgress();
            applyButton();
        }
        //输入框
        else if (mParams.inputParams != null) {
            mCreateView.buildInput();
            applyButton();
            mCreateView.regInputListener();
        }
    }

    private void applyButton() {
        //有确定并且有取消按钮
        if (mParams.positiveParams != null && mParams.negativeParams != null) {
            final MultipleButton multipleButton = mCreateView.buildMultipleButton();
            multipleButton.regNegativeListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHandler.obtainMessage(BUTTON_NEGATIVE, multipleButton);
                    mHandler.obtainMessage(MSG_DISMISS_DIALOG, mDialog)
                            .sendToTarget();
                }
            });
            multipleButton.regPositiveListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHandler.obtainMessage(BUTTON_POSITIVE, multipleButton).sendToTarget();
                    mHandler.obtainMessage(MSG_DISMISS_DIALOG, mDialog)
                            .sendToTarget();
                }
            });
        }//有确定或者有取消按钮
        else if (mParams.positiveParams != null || mParams.negativeParams != null) {
            final SingleButton singleButton = mCreateView.buildSingleButton();
            singleButton.regOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHandler.obtainMessage(mParams.positiveParams != null ? BUTTON_POSITIVE : BUTTON_NEGATIVE, singleButton).sendToTarget();
                    mHandler.obtainMessage(MSG_DISMISS_DIALOG, mDialog)
                            .sendToTarget();
                }
            });
        }


    }

    /**
     * Interface used to allow the creator of a dialog to run some code when an
     * item on the dialog is clicked..
     */
    public interface OnClickListener {
        /**
         * This method will be invoked when a button in the dialog is clicked.
         *
         * @param view
         * @param which The button that was clicked (e.g.
         *              {@link DialogInterface#BUTTON1}) or the position
         *              of the item clicked.
         */
        /* TODO: Change to use BUTTON_POSITIVE after API council */
        public void onClick(View view, int which);
    }

    private View getView() {
        return mCreateView.getView();
    }
}
