package software.baby.wordnotes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.Context;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;

public class MainActivity extends AppCompatActivity {
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navController = Navigation.findNavController(findViewById(R.id.fragment));
        NavigationUI.setupActionBarWithNavController(this, navController);
//        initView();

        //最新版的API已弃用ViewModelProviders.of这种方法初始化ViewModel
//        wordViewModel = ViewModelProviders.of(this).get(WordViewModel.class);
        //采用"单Activity+多Fragment"模式
        /*
        wordViewModel = new ViewModelProvider(this).get(WordViewModel.class);

        wordAdapter1 = new WordAdapter(wordViewModel, false);
        wordAdapter2 = new WordAdapter(wordViewModel, true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(wordAdapter1);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    recyclerView.setAdapter(wordAdapter2);
                } else {
                    recyclerView.setAdapter(wordAdapter1);
                }
            }
        });

        wordViewModel.getAllWordsLive().observe(this, new Observer<List<Word>>() {
            @Override
            public void onChanged(List<Word> words) {
                int temp = wordAdapter1.getItemCount();
                wordAdapter1.setAllWords(words);
                wordAdapter2.setAllWords(words);
                if (temp != words.size()) {
                    wordAdapter1.notifyDataSetChanged();
                    wordAdapter2.notifyDataSetChanged();
                }
            }
        });

        buttonInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Word word1 = new Word("hello", "你好");
                Word word2 = new Word("world", "世界");
                wordViewModel.insertWords(word1, word2);
            }
        });
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wordViewModel.deleteAllWords();
            }
        });
        */
    }

    @Override
    public boolean onSupportNavigateUp() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(findViewById(R.id.fragment).getWindowToken(), 0);
        navController.navigateUp();
        return super.onSupportNavigateUp();
    }

    /*
    private void initView() {
        recyclerView = findViewById(R.id.recyclerView);
        aSwitch = findViewById(R.id.aSwitch);
        buttonInsert = findViewById(R.id.buttonInsert);
        buttonClear = findViewById(R.id.buttonClear);
    }
    */

    //这部分逻辑移入到Repository中
    /*
    static class InsertAsyncTask extends AsyncTask<Word, Void, Void> {
        private WordDao wordDao;

        public InsertAsyncTask(WordDao wordDao) {
            this.wordDao = wordDao;
        }

        @Override
        protected Void doInBackground(Word... words) {
            wordDao.insertWords(words);
            return null;
        }
    }

    static class ClearAsyncTask extends AsyncTask<Void, Void, Void> {
        private WordDao wordDao;

        public ClearAsyncTask(WordDao wordDao) {
            this.wordDao = wordDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            wordDao.deleteAllWords();
            return null;
        }
    }
    */

    //使用LiveData之后，就不用每次都去手动更新数据
    /*
    private void updateView() {
        List<Word> list = wordDao.getAllWords();
        String text = "";
        for (int i = 0; i < list.size(); i++) {
            Word word = list.get(i);
            text += word.getId() + ":" + word.getEnglishWord() + "=" + word.getChineseMeaning() + "\n";
        }
        textView.setText(text);
    }
    */
}
