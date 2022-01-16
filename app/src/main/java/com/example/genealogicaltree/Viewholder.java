package com.example.genealogicaltree;
import android.view.View;
import android.widget.TextView;

public class Viewholder implements View.OnClickListener {

    TextView textView;

    Viewholder(View view) {
        textView = view.findViewById(R.id.idTvnode);
        //textView.setOnClickListener(this);
    }

    public void onClick(View arg0) {
        textView.setText("My text on click");
    }

}
