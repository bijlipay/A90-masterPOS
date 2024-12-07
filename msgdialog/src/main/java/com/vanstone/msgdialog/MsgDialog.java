package com.vanstone.msgdialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class MsgDialog extends DialogFragment {

    private View root;
    TextView tvInfo;
    private String text = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.DialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.dialog_msg, null);
        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvInfo = root.findViewById(R.id.tvInfo);
        tvInfo.setText(text);
        getDialog().setCanceledOnTouchOutside(false);
        setCancelable(false);
    }

    public void setTvInfo(String msg){
        if (tvInfo != null && (msg != null) && !msg.equals("")) {
            tvInfo.setText(msg);
            tvInfo.invalidate();
        }
    }

    public void setText(String text){
        this.text = text;
    }
}
