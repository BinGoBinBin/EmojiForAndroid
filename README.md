# EmojiForAndroid

Threre is a feature of emoji in project,and i found a great project that's [emojicon](https://github.com/rockerhieu/emojicon).Thanks a lot for rockerhieu's contribution.
      
Now , i add a emojo_board and is a simple emoji implemention,fixed delete single or double character bug,fix issues clicking back event when emoji_board showwing,etc.

Thanks again for [rockerhieu](https://github.com/rockerhieu)

#Example
Adding emoji feature only two steps：   
At first,copying code to your xml file ,as follows:  
 
```
<com.pikachu.emoji.widget.EmojiBoard 
        android:id="@+id/emojiview"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#e3e3e3"/>
```

The Second,find this view and attact activity,as follows:   

```
EmojiBoard emojiView = (EmojiBoard) findViewById(R.id.emojiview);
emojiView.attachActivity(this);
```

If success,the result is :     

<img src="https://github.com/BinGoBinBin/EmojiForAndroid/raw/master/images/sample.png" width="540" height="960">

#Contribution

If you find a bug or other questions,please send a pull request,thanks.



