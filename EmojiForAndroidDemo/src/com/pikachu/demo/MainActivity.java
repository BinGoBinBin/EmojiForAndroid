
package com.pikachu.demo;

import com.pikachu.emoji.widget.EmojiBoard;

import android.app.Activity;
import android.os.Bundle;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        EmojiBoard emojiView = (EmojiBoard) findViewById(R.id.emojiview);
        emojiView.attachActivity(this);
    }

}
