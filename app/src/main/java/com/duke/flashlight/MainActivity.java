package com.duke.flashlight;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class MainActivity extends Activity implements FlashlightController.FlashlightListener{

    private View mSwitch;
    private View mDotView;
    private ImageView mSwitchButton;
    private TextView mSwitchText;
    /**
     * flag means whether flashlight is open,default value is true
     */
    private boolean mLightIsOn = true;

    /**
     * the duration of switch animation when flashlight's status changes
     */
    private static final long MOVE_ANIMATION_DURATION = 50;

    private FlashlightController mFlashlightController;
    private boolean mFlag = false;
    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            changeLightStatus();
            onLightStatusChanged();
            translateSwitchAnimation(MOVE_ANIMATION_DURATION);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        mFlashlightController = new FlashlightController(this);
        mFlashlightController.addListener(this);

        mDotView = findViewById(R.id.main_image_dot);

        mSwitchText = (TextView) findViewById(R.id.main_switch_text);

        mSwitchButton = (ImageView) findViewById(R.id.main_switch_btn);

        mSwitch = findViewById(R.id.main_switch_on_off);
        mSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                mLightIsOn = !mLightIsOn;
                changeLightStatus();
                onLightStatusChanged();
                translateSwitchAnimation(0);
                v.setEnabled(true);
            }
        });

        changeLightStatus();
        onLightStatusChanged();
        translateSwitchAnimation(MOVE_ANIMATION_DURATION);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.main_switch_button_width), getResources().getDimensionPixelSize(R.dimen.main_switch_button_height));
        layoutParams.setMargins(0, mLightIsOn ? 0 : getResources().getDimensionPixelSize(R.dimen.main_switch_button_top_margin), 0, 0);
        if(mSwitchButton != null) {
            mSwitchButton.setLayoutParams(layoutParams);
        }
        mFlag = true;
    }

    protected void onDestroy(){
        super.onDestroy();
        mFlashlightController.removeListener(this);
        mFlashlightController = null;
        mFlag = false;
        mDotView = null;
        mSwitch = null;
        mSwitchButton = null;
        mSwitchText = null;
        mHandler = null;
        mRunnable = null;
    }

    /**
     * change flashlight's status
     */
    private void changeLightStatus() {
        if(mFlashlightController != null) {
            mFlashlightController.setFlashlight(mLightIsOn);
        }
    }

    /**
     * views change background when flashlight's status changed
     */
    private void onLightStatusChanged() {
        if(mDotView != null) {
            mDotView.setActivated(mLightIsOn);
        }
        if(mSwitchButton != null) {
            mSwitchButton.setActivated(mLightIsOn);
        }
        if(mSwitchText != null) {
            mSwitchText.setText(mLightIsOn ? getResources().getString(R.string.light_on) : getResources().getString(R.string.light_off));
            mSwitchText.setTextColor(mLightIsOn ? getResources().getColor(R.color.text_on) : getResources().getColor(R.color.text_off));
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.main_switch_button_width), ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, mLightIsOn ? getResources().getDimensionPixelSize(R.dimen.main_switch_button_height) : 0, 0, 0);
            mSwitchText.setLayoutParams(layoutParams);
        }
    }

    /**
     * switch's translate animation when flashlight's status changed
     */
    private void translateSwitchAnimation(long duration) {
        if(mSwitch == null || mSwitchButton == null){
            return;
        }
        int moveHeight = mSwitch.getHeight() - mSwitchButton.getHeight();
        ObjectAnimator.ofFloat(mSwitchButton, "translationY", mLightIsOn ? moveHeight : 0, mLightIsOn ? 0 : moveHeight).setDuration(duration).start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && mLightIsOn){
            mLightIsOn = false;
            onLightStatusChanged();
            changeLightStatus();
            translateSwitchAnimation(MOVE_ANIMATION_DURATION);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onFlashlightChanged(boolean enabled) {
        if(mFlag && enabled != mLightIsOn) {
            mLightIsOn = enabled;
            if(mHandler != null && mRunnable != null) {
                mHandler.post(mRunnable);
            }
        }
    }

    @Override
    public void onFlashlightError() {
        if(mFlag) {
            if(mHandler != null && mRunnable != null) {
                mHandler.post(mRunnable);
            }
        }
    }

    @Override
    public void onFlashlightAvailabilityChanged(boolean available) {
        if(mFlag != available){
            mFlag = available;
            if(mHandler != null && mRunnable != null) {
                mHandler.post(mRunnable);
            }
        }
    }
}
