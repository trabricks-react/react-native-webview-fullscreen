package cc.creamcookie.webview;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.uimanager.ThemedReactContext;
import com.reactnativecommunity.webview.RNCWebViewManager;

@ReactModule(name = RNWebviewFsManager.REACT_CLASS)
public class RNWebviewFsManager extends RNCWebViewManager {

    protected static final String REACT_CLASS = "RCTFSWebView";

    protected static class FSWebChromeClient extends WebChromeClient {

        private RNCWebViewManager context;
        private ReactContext reactContext;
        private View mCustomView;

        private final Handler handler = new Handler();

        private WebChromeClient.CustomViewCallback mCustomViewCallback;
        protected FrameLayout mFullscreenContainer;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;

        private int iNavColor;

        View.OnSystemUiVisibilityChangeListener listener1;
        View.OnApplyWindowInsetsListener listener2;

        public FSWebChromeClient(RNCWebViewManager context, ReactContext reactContext) {
            this.context = context;
            this.reactContext = reactContext;
        }

        @Override
        public void onPermissionRequest(PermissionRequest request) {
            super.onPermissionRequest(request);
        }

        @Override
        public void onHideCustomView() {

            final Activity mActivity = reactContext.getCurrentActivity();

            ((FrameLayout)mActivity.getWindow().getDecorView()).removeView(this.mCustomView);
            mActivity.setRequestedOrientation(this.mOriginalOrientation);

            if (Build.VERSION.SDK_INT >= 21) {
                mActivity.getWindow().setNavigationBarColor(iNavColor);
            } else {
                mActivity.getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
                mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

            mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            if (Build.VERSION.SDK_INT >= 21) {
                WindowInsets insets = mActivity.getWindow().getDecorView().getRootWindowInsets();

                mActivity.getWindow().getDecorView().setOnApplyWindowInsetsListener(null);
                mActivity.getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(null);

                this.listener1 = null;
                this.listener2 = null;

                mCustomView.setPadding(0, 0, 0, 0);
            }

            this.mCustomView = null;
            this.mCustomViewCallback.onCustomViewHidden();
            this.mCustomViewCallback = null;

        }

        @Override @SuppressWarnings("deprecation")
        public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) // Available in API level 14+, deprecated in API level 18+
        {
            onShowCustomView(view, callback);
        }

        @Override
        public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback) {
            if (this.mCustomView != null) {
                onHideCustomView();
                return;
            }
            this.mCustomView = paramView;
            this.mCustomView.setBackgroundColor(Color.BLACK);

            final Activity mActivity = reactContext.getCurrentActivity();

            this.mOriginalSystemUiVisibility = mActivity.getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation = mActivity.getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;

            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

            ((FrameLayout)mActivity.getWindow().getDecorView()).addView(this.mCustomView,
                    new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

            if (Build.VERSION.SDK_INT >= 21) {
                WindowInsets insets = mActivity.getWindow().getDecorView().getRootWindowInsets();
                mCustomView.setPadding(0, 0, insets.getStableInsetRight(), insets.getStableInsetBottom());

                iNavColor = mActivity.getWindow().getNavigationBarColor();
                mActivity.getWindow().setNavigationBarColor(Color.BLACK);

                int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
                mActivity.getWindow().getDecorView().setSystemUiVisibility(uiOptions);

                this.listener1 = new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        handler.removeCallbacksAndMessages(null);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
                                mActivity.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
                            }
                        }, 3000);
                    }
                };
                mActivity.getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(this.listener1);

                this.listener2 = new View.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                        mCustomView.setPadding(0, 0, insets.getStableInsetRight(), insets.getStableInsetBottom());
                        return insets;
                    }
                };
                mActivity.getWindow().getDecorView().setOnApplyWindowInsetsListener(this.listener2);
            }
            else {
                int uiOptions = mActivity.getWindow().getDecorView().getSystemUiVisibility();
                int newUiOptions = uiOptions;
                if (Build.VERSION.SDK_INT >= 14) {
                    newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                }
                if (Build.VERSION.SDK_INT >= 16) {
                    newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
                }
                if (Build.VERSION.SDK_INT >= 18) {
                    newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                }
                mActivity.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
                mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

            mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        }
    }


    protected static class CustomWebView extends RNCWebView {
        public CustomWebView(ThemedReactContext reactContext) {
            super(reactContext);
        }
    }

    @Override
    protected RNCWebView createRNCWebViewInstance(ThemedReactContext reactContext) {
        System.out.println("createRNCWebViewInstance");
        return new CustomWebView(reactContext);
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected void addEventEmitters(ThemedReactContext reactContext, WebView view) {
        System.out.println("addEventEmitters");
        view.setWebChromeClient(new FSWebChromeClient(this, reactContext));
        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        view.getSettings().setSupportMultipleWindows(true);
    }
}