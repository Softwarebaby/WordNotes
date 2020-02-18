package software.baby.wordnotes.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by Du Senmiao on 2020/02/18
 */
@Entity
public class Word {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "english_word")
    private String englishWord;
    @ColumnInfo(name = "chinese_meaning")
    private String chineseMeaning;
    @ColumnInfo(name = "chinese_invisible")
    private boolean chineseInvisible;

    public Word(String englishWord, String chineseMeaning) {
        this.englishWord = englishWord;
        this.chineseMeaning = chineseMeaning;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEnglishWord() {
        return englishWord;
    }

    public void setEnglishWord(String englishWord) {
        this.englishWord = englishWord;
    }

    public String getChineseMeaning() {
        return chineseMeaning;
    }

    public void setChineseMeaning(String chineseMeaning) {
        this.chineseMeaning = chineseMeaning;
    }

    public boolean isChineseInvisible() {
        return chineseInvisible;
    }

    public void setChineseInvisible(boolean chineseInvisible) {
        this.chineseInvisible = chineseInvisible;
    }

    @Override
    public String toString() {
        return "Word{" +
                "id=" + id +
                ", englishWord='" + englishWord + '\'' +
                ", chineseMeaning='" + chineseMeaning + '\'' +
                ", chineseInvisible=" + chineseInvisible +
                '}';
    }
}

