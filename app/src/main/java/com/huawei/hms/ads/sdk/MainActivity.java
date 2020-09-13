package com.huawei.hms.ads.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.InterstitialAd;
import com.huawei.hms.ads.banner.BannerView;
import com.huawei.hms.ads.consent.bean.AdProvider;
import com.huawei.hms.ads.consent.constant.ConsentStatus;
import com.huawei.hms.ads.consent.constant.DebugNeedConsent;
import com.huawei.hms.ads.consent.inter.Consent;
import com.huawei.hms.ads.consent.inter.ConsentUpdateListener;
import com.huawei.hms.ads.sdk.dialogs.ConsentDialog;
import com.huawei.hms.ads.sdk.dialogs.ProtocolDialog;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /*
    Iklan Banner
     */
    private BannerView defaultBannerView;
    private static final int REFRESH_TIME = 30;


    /*
    Iklan Intertitial
     */
    private InterstitialAd interstitialAd; //inter gambar
    private InterstitialAd interstitialAd2; //inter video

    private static final String TAG = MainActivity.class.getSimpleName();

    /*
    GDPR
     */
    private static final int PROTOCOL_MSG_TYPE = 100;

    private static final int CONSENT_MSG_TYPE = 200;

    private static final int MSG_DELAY_MS = 1000;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (MainActivity.this.hasWindowFocus()) {
                switch (msg.what) {
                    case PROTOCOL_MSG_TYPE:
                        showPrivacyDialog();
                        break;
                    case CONSENT_MSG_TYPE:
                        checkConsentStatus();
                        break;
                }
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        /*
        Implementasi banner
         */
        defaultBannerView = findViewById(R.id.hw_banner_view);
        defaultBannerView.setBannerRefresh(REFRESH_TIME);
        AdParam adParam = new AdParam.Builder().build();
        defaultBannerView.loadAd(adParam);



            /*
            Muncul awal GDPR
             */
        sendMessage(PROTOCOL_MSG_TYPE, MSG_DELAY_MS);

        loadInterstitialAd();
        loadInterstitialAd2();



        final Button inter_image = findViewById(R.id.tb_gambar);
        inter_image .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInterstitial();
            }
        });


        final Button inter_video = findViewById(R.id.tb_video);
        inter_video .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInterstitial2();
            }
        });
    }

    private void showPrivacyDialog() {
        if (getPreferences(AdsConstant.SP_PROTOCOL_KEY, AdsConstant.DEFAULT_SP_PROTOCOL_VALUE) == 0) {
            Log.i(TAG, "Show protocol dialog.");
            ProtocolDialog dialog = new ProtocolDialog(this);
            dialog.setCallback(new ProtocolDialog.ProtocolDialogCallback() {
                @Override
                public void agree() {
                    sendMessage(CONSENT_MSG_TYPE, MSG_DELAY_MS);
                }

                @Override
                public void cancel() {
                    finish();
                }
            });
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } else {
            sendMessage(CONSENT_MSG_TYPE, MSG_DELAY_MS);
        }
    }

    private void showConsentDialog(List<AdProvider> adProviders) {
        Log.i(TAG, "Show consent dialog.");
        ConsentDialog dialog = new ConsentDialog(this, adProviders);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private int getPreferences(String key, int defValue) {
        SharedPreferences preferences = getSharedPreferences(AdsConstant.SP_NAME, Context.MODE_PRIVATE);
        int value = preferences.getInt(key, defValue);
        Log.i(TAG, "Key:" + key + ", Preference value is: " + value);
        return value;
    }

    private void sendMessage(int what, int delayMillis) {
        Message msg = Message.obtain();
        msg.what = what;
        mHandler.sendMessageDelayed(msg, delayMillis);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void checkConsentStatus() {
        final List<AdProvider> adProviderList = new ArrayList<>();
        Consent consentInfo = Consent.getInstance(this);
        consentInfo.addTestDeviceId("********");
        consentInfo.setDebugNeedConsent(DebugNeedConsent.DEBUG_NEED_CONSENT);
        consentInfo.requestConsentUpdate(new ConsentUpdateListener() {
            @Override
            public void onSuccess(ConsentStatus consentStatus, boolean isNeedConsent, List<AdProvider> adProviders) {
                Log.i(TAG, "ConsentStatus: " + consentStatus + ", isNeedConsent: " + isNeedConsent);
                if (isNeedConsent) {
                    if (adProviders != null && adProviders.size() > 0) {
                        adProviderList.addAll(adProviders);
                    }
                    showConsentDialog(adProviderList);
                }
            }

            @Override
            public void onFail(String errorDescription) {
                Log.e(TAG, "User's consent status failed to update: " + errorDescription);
                if (getPreferences(AdsConstant.SP_CONSENT_KEY, AdsConstant.DEFAULT_SP_CONSENT_VALUE) < 0) {
                    // In this example, if the request fails, the consent dialog box is still displayed. In this case, the ad publisher list is empty.
                    showConsentDialog(adProviderList);
                }
            }
        });
    }


    /*
    Inter gambar
     */
    private void loadInterstitialAd() {
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdId(getString(R.string.image_ad_id));
        AdParam adParam = new AdParam.Builder().build();
        interstitialAd.loadAd(adParam);
    }

    /*
    Inter Video
     */

    private void loadInterstitialAd2() {
        interstitialAd2 = new InterstitialAd(this);
        interstitialAd2.setAdId(getString(R.string.video_ad_id));
        AdParam adParam = new AdParam.Builder().build();
        interstitialAd2.loadAd(adParam);
    }


    /*
    memunculkan Intertitial Gambar
     */
    private void showInterstitial() {
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    memunculkan Intertitial Video
     */
    private void showInterstitial2() {
        if (interstitialAd2 != null && interstitialAd2.isLoaded()) {
            interstitialAd2.show();
        } else {
            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
        }
    }
}
