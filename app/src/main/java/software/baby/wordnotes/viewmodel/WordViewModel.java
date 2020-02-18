package software.baby.wordnotes.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import software.baby.wordnotes.entity.Word;
import software.baby.wordnotes.repository.WordRepository;

/**
 * Created by Du Senmiao on 2020/02/18
 */
public class WordViewModel extends AndroidViewModel {
    private WordRepository wordRepository;

    public WordViewModel(@NonNull Application application) {
        super(application);
        wordRepository = new WordRepository(application);
    }

    public LiveData<List<Word>> getAllWordsLive() {
        return wordRepository.getAllWordsLive();
    }

    public LiveData<List<Word>> findWordWithPattern(String pattern) {
        return wordRepository.findWordsWithPattern(pattern);
    }

    public void insertWords(Word... words) {
        wordRepository.insertWords(words);
    }

    public void updateWords(Word... words) {
        wordRepository.updateWords(words);
    }

    public void deleteWords(Word... words) {
        wordRepository.deleteWords(words);
    }

    public void deleteAllWords() {
        wordRepository.deleteAllWords();
    }
}
