package jacob.bt_file_transfer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Package : jacob.bt_file_transfer
 * Author : jacob
 * Date : 15-6-8
 * Description : 这个类是用来xxx
 */
public class TransFileItemView extends RelativeLayout {

    private TextView mTextViewItemOrder;
    private TextView mTextViewItemContent;
    private ImageView mImageViewError;
    private ImageView mImageViewOk;
    private ProgressBar mProgress;

    public TransFileItemView(Context context) {
        this(context, null);
    }

    public TransFileItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransFileItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.layout_ble_transfer_item, this);
        mTextViewItemOrder = (TextView) findViewById(R.id.text_view_item_order);
        mTextViewItemContent = (TextView) findViewById(R.id.text_view_item_content);
        mImageViewError = (ImageView) findViewById(R.id.image_view_error);
        mImageViewOk = (ImageView) findViewById(R.id.image_view_ok);
        mProgress = (ProgressBar) findViewById(R.id.progress);
    }

    public void setTransFileItem(TransFileItem item) {
        mTextViewItemOrder.setText("" + item.getOrder());
        mTextViewItemContent.setText(item.getContent());
    }

    public void showOKState() {
        mImageViewOk.setVisibility(View.VISIBLE);
        mImageViewError.setVisibility(View.GONE);
        mProgress.setVisibility(View.GONE);
    }

    public void showProgressState() {
        mImageViewOk.setVisibility(View.GONE);
        mImageViewError.setVisibility(View.GONE);
        mProgress.setVisibility(View.VISIBLE);
    }

    public void showErrorState() {
        mImageViewOk.setVisibility(View.GONE);
        mImageViewError.setVisibility(View.VISIBLE);
        mProgress.setVisibility(View.GONE);
    }
}
