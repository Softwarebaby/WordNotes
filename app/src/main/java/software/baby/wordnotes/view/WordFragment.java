package software.baby.wordnotes.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import software.baby.wordnotes.R;
import software.baby.wordnotes.entity.Word;
import software.baby.wordnotes.view.adapter.WordAdapter;
import software.baby.wordnotes.viewmodel.WordViewModel;

/**
 * Created by Du Senmiao on 2020/02/18
 */
public class WordFragment extends Fragment {
    private static final String VIEW_TYPE_SHP = "view_type_shp";
    private static final String IS_USING_CARD_VIEW = "is_using_card_view";
    
    private FragmentActivity activity;
    private WordViewModel wordViewModel;
    private WordAdapter wordAdapter1, wordAdapter2;
    private LiveData<List<Word>> filterWords;

    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_word, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = requireActivity();
        //最新版的API已弃用ViewModelProviders.of这种方法初始化ViewModel
//        wordViewModel = ViewModelProviders.of(this).get(WordViewModel.class);
        wordViewModel = new ViewModelProvider(activity).get(WordViewModel.class);  //注意：ViewModel的作用域应为整个Activity

        recyclerView = activity.findViewById(R.id.recyclerView);
        floatingActionButton = activity.findViewById(R.id.floatingActionButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        wordAdapter1 = new WordAdapter(wordViewModel, false);
        wordAdapter2 = new WordAdapter(wordViewModel, true);
        SharedPreferences shp = activity.getSharedPreferences(VIEW_TYPE_SHP, Context.MODE_PRIVATE);
        boolean viewType = shp.getBoolean(IS_USING_CARD_VIEW, false);
        recyclerView.setAdapter(wordAdapter1);
        if (viewType) {
            recyclerView.setAdapter(wordAdapter2);
        } else {
            recyclerView.setAdapter(wordAdapter1);
        }

        filterWords = wordViewModel.getAllWordsLive();
        filterWords.observe(activity, new Observer<List<Word>>() {
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

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_wordFragment_to_noteFragment);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setMaxWidth(700);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String pattern = newText.trim();
                filterWords.removeObservers(activity);  //先移除原有的观察，防止出现碰撞而导致出错
                filterWords = wordViewModel.findWordWithPattern(pattern);
                filterWords.observe(activity, new Observer<List<Word>>() {
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
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clearData:
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("清空数据");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        wordViewModel.deleteAllWords();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.create();
                builder.show();
                break;
            case R.id.switchViewType:
                SharedPreferences shp = activity.getSharedPreferences(VIEW_TYPE_SHP, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = shp.edit();
                boolean viewType = shp.getBoolean(IS_USING_CARD_VIEW, false);
                if (viewType) {
                    recyclerView.setAdapter(wordAdapter1);
                    editor.putBoolean(IS_USING_CARD_VIEW, false);
                } else {
                    recyclerView.setAdapter(wordAdapter2);
                    editor.putBoolean(IS_USING_CARD_VIEW, true);
                }
                editor.apply();
        }
        return super.onOptionsItemSelected(item);
    }
}
