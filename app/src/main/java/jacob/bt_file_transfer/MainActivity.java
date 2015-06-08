package jacob.bt_file_transfer;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;


public class MainActivity extends FragmentActivity implements View.OnClickListener {

    private TransFileItemView mTransItemOne;
    private TransFileItemView mTransItemTwo;
    private TransFileItemView mTransItemThree;
    private TransFileItemView mTransItemFour;
    private TransFileItemView mTransItemFive;
    private View mEditTextImsi;
    private Button mButtonStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTransItemOne = (TransFileItemView) findViewById(R.id.transItemOne);
        mTransItemTwo = (TransFileItemView) findViewById(R.id.transItemTwo);
        mTransItemThree = (TransFileItemView) findViewById(R.id.transItemThree);
        mTransItemFour = (TransFileItemView) findViewById(R.id.transItemFour);
        mTransItemFive = (TransFileItemView) findViewById(R.id.transItemFive);
        mButtonStart = (Button) findViewById(R.id.button_start);
        mButtonStart.setOnClickListener(this);
        findViewById(R.id.button_send_file).setOnClickListener(this);
        mEditTextImsi = findViewById(R.id.edit_text_imsi);

        mTransItemOne.setTransFileItem(TransFileItem.item_one);
        mTransItemTwo.setTransFileItem(TransFileItem.item_two);
        mTransItemThree.setTransFileItem(TransFileItem.item_three);
        mTransItemFour.setTransFileItem(TransFileItem.item_four);
        mTransItemFive.setTransFileItem(TransFileItem.item_five);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_start:
                break;
            case R.id.button_send_file:
                break;
        }
    }
}
