package com.jacob.bt.file.logic;

import com.jacob.bt.file.R;

/**
 * Package : jacob.bt_file_transfer
 * Author : jacob
 * Date : 15-6-8
 * Description : 这个类是用来xxx
 */
public enum TransFileItem {
    item_one(1, R.string.item_one),
    item_two(2, R.string.item_two),
    item_three(3, R.string.item_three),
    item_four(4, R.string.item_four),
    item_five(5, R.string.item_five);

    int order;
    int content;

    TransFileItem(int order, int content) {
        this.order = order;
        this.content = content;
    }

    public int getOrder() {
        return order;
    }

    public int getContent() {
        return content;
    }
}
