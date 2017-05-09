package com.app.random.backapp.Fragments;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.app.random.backapp.R;


public class AboutFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_about, container, false);
        TextView email = (TextView) view.findViewById(R.id.email);
        TextView privecy = (TextView) view.findViewById(R.id.privacy_policy);

        Button done = (Button) view.findViewById(R.id.button);

        email.setClickable(true);
        privecy.setClickable(true);

        SpannableString string = new SpannableString("eli200601@gmail.com");
        string.setSpan(new UnderlineSpan(), 0, string.length(), 0);
        email.setText(string);

        SpannableString stringPrivacy = new SpannableString("Privacy Policy");
        stringPrivacy.setSpan(new UnderlineSpan(), 0, stringPrivacy.length(), 0);
        privecy.setText(stringPrivacy);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto: eli200601@gmail.com"));
                startActivity(Intent.createChooser(emailIntent, "Send feedback"));
            }
        });

        privecy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://sites.google.com/view/backapp-privacy-policy/home";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        return view;
    }
}
