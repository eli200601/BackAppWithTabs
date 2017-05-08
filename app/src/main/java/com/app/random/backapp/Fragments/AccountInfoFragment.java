package com.app.random.backapp.Fragments;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.random.backapp.R;
import com.app.random.backapp.Utils.Keys;
import com.app.random.backapp.Utils.SharedPrefsUtils;

import java.util.regex.Pattern;


public class AccountInfoFragment extends Fragment {
    final static String TAG = "AccountInfoFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_account_info, null);
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.info_space_progressBar);
        TextView fullNameText = (TextView) view.findViewById(R.id.info_fullName);
        TextView uidText= (TextView) view.findViewById(R.id.info_uid);
        TextView referralText = (TextView) view.findViewById(R.id.info_referralURL);
        TextView totalSpaceText = (TextView) view.findViewById(R.id.info_total_space_text);
        TextView url = (TextView) view.findViewById(R.id.info_url_text);
        Button btn = (Button) view.findViewById(R.id.force_crash_button);

        final TextView usedSpaceText = (TextView) view.findViewById(R.id.info_space_text);

        Context context = getActivity().getApplicationContext();

        String fullName = SharedPrefsUtils.getStringPreference(context, Keys.DROPBOX_USER_NAME) + " " + SharedPrefsUtils.getStringPreference(context, Keys.DROPBOX_LAST_NAME);
        String uid = SharedPrefsUtils.getStringPreference(context, Keys.DROPBOX_UID);
        String referralURL = SharedPrefsUtils.getStringPreference(context, Keys.DROPBOX_REFERRAL_URL);
        String sizeTotalText = SharedPrefsUtils.getStringPreference(context, Keys.DROPBOX_TOTAL_SPACE);
        final String usedSizeText = SharedPrefsUtils.getStringPreference(context, Keys.DROPBOX_USED_SPACE);

        long total = SharedPrefsUtils.getLongPreference(context, Keys.DROPBOX_TOTAL_SPACE_LONG, 0);
        long used = SharedPrefsUtils.getLongPreference(context, Keys.DROPBOX_USED_SPACE_LONG, 0);

        int targetProgress = (int) Math.round(((used * 100.0) /total));
        if (targetProgress == 0) {
            targetProgress = 1;
        }
        else {
            if (used > total) {
                targetProgress = 100;
            }
        }

        fullNameText.setText("User Name: " + fullName);
        uidText.setText("Account ID: " + uid);
        url.setText(referralURL);

        Pattern pattern = Pattern.compile(referralURL);
        Linkify.addLinks(url, pattern, "https://");

        totalSpaceText.setText("Total: " + sizeTotalText);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "starting crashing");
                throw new RuntimeException("This is a crash");
            }
        });

        final ValueAnimator animator = new ValueAnimator();
        animator.setObjectValues(0, targetProgress);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                int progress = (int) animation.getAnimatedValue();
                progressBar.setProgress(progress);
                usedSpaceText.setText("Used Space: " + usedSizeText + " " + String.valueOf(progress) + "%");
            }
        });
        animator.setEvaluator(new TypeEvaluator<Integer>() {
            public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
                return Math.round(startValue + (endValue - startValue) * fraction);
            }
        });
        animator.setDuration(2000);
        animator.start();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


}
