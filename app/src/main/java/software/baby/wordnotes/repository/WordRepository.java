package software.baby.wordnotes.repository;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import software.baby.wordnotes.dao.WordDao;
import software.baby.wordnotes.db.WordDatabase;
import software.baby.wordnotes.entity.Word;

/**
 * Created by Du Senmiao on 2020/02/18
 */
public class WordRepository {
    private LiveData<List<Word>> allWordsLive;
    private WordDao wordDao;

    public WordRepository(Context context) {
        WordDatabase wordDatabase = WordDatabase.getInstance(context);
        wordDao = wordDatabase.getWordDao();
        allWordsLive = wordDao.getAllWordsLive();
    }

    public LiveData<List<Word>> getAllWordsLive() {
        return allWordsLive;
    }

    public LiveData<List<Word>> findWordsWithPattern(String pattern) {
        return wordDao.findWordsWithPattern("%" + pattern + "%");  //通配符实现模糊匹配
    }

    public void insertWords(Word... words) {
        new InsertAsyncTask(wordDao).execute(words);
    }

    public void updateWords(Word... words) {
        new UpdateAsyncTask(wordDao).execute(words);
    }

    public void deleteAllWords() {
        new ClearAsyncTask(wordDao).execute();
    }

    private static class InsertAsyncTask extends AsyncTask<Word, Void, Void> {
        private WordDao wordDao;

        InsertAsyncTask(WordDao wordDao) {
            this.wordDao = wordDao;
        }

        @Override
        protected Void doInBackground(Word... words) {
            wordDao.insertWords(words);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<Word, Void, Void> {
        private WordDao wordDao;

        UpdateAsyncTask(WordDao wordDao) {
            this.wordDao = wordDao;
        }

        @Override
        protected Void doInBackground(Word... words) {
            wordDao.updateWords(words);
            return null;
        }
    }

    private static class ClearAsyncTask extends AsyncTask<Void, Void, Void> {
        private WordDao wordDao;

        ClearAsyncTask(WordDao wordDao) {
            this.wordDao = wordDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            wordDao.deleteAllWords();
            return null;
        }
    }
}
