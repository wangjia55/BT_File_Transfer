package com.jacob.bt.file;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.jacob.bt.file.logic.NewPreferenceManager;

public class SetFileAddressActivity extends Activity implements View.OnClickListener {

    private EditText mEditTextAddress;
    private TextView mTextViewFile1;
    private TextView mTextViewFile2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_file_address);

        findViewById(R.id.button_save).setOnClickListener(this);
        mTextViewFile1 = (TextView) findViewById(R.id.text_view_file1);
        mTextViewFile1.setOnClickListener(this);

        mTextViewFile2 = (TextView) findViewById(R.id.text_view_file2);
        mTextViewFile2.setOnClickListener(this);

        mEditTextAddress = (EditText) findViewById(R.id.edit_text_file_name);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_save:
                String address = mEditTextAddress.getText().toString();
                NewPreferenceManager.getInstance(getApplicationContext()).saveFileAddress(address);
                Intent intent = new Intent();
                intent.putExtra("address",address);
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.text_view_file1:
                mEditTextAddress.setText(mTextViewFile1.getText().toString());
                break;
            case R.id.text_view_file2:
                mEditTextAddress.setText(mTextViewFile2.getText().toString());
                break;
        }
    }
}
