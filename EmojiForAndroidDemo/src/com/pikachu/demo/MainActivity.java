
package com.pikachu.demo;

import com.pikachu.emoji.EmojiView;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        EmojiView emojiView = (EmojiView) findViewById(R.id.emojiview);
        emojiView.attachActivity(this);
    }

}
