package software.baby.wordnotes.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

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
    private List<Word> allWords;
    private boolean undoAction;

    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;
    private DividerItemDecoration dividerItemDecoration;

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
        dividerItemDecoration = new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL);

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        //当动画结束时，刷新序列号
        recyclerView.setItemAnimator(new DefaultItemAnimator(){
            @Override
            public void onAnimationFinished(@NonNull RecyclerView.ViewHolder viewHolder) {
                super.onAnimationFinished(viewHolder);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (linearLayoutManager != null) {
                    int firstPosition = linearLayoutManager.findFirstVisibleItemPosition();
                    int lastPosition = linearLayoutManager.findLastVisibleItemPosition();
                    for (int i = firstPosition; i <= lastPosition; i++) {
                        WordAdapter.WordViewHolder holder = (WordAdapter.WordViewHolder) recyclerView.
                                findViewHolderForAdapterPosition(i);
                        if (holder != null) {
                            holder.textViewNumber.setText(String.valueOf(i + 1));
                        }
                    }
                }
            }
        });
        wordAdapter1 = new WordAdapter(wordViewModel, false);
        wordAdapter2 = new WordAdapter(wordViewModel, true);
        SharedPreferences shp = activity.getSharedPreferences(VIEW_TYPE_SHP, Context.MODE_PRIVATE);
        boolean viewType = shp.getBoolean(IS_USING_CARD_VIEW, false);
        recyclerView.setAdapter(wordAdapter1);
        if (viewType) {
            recyclerView.setAdapter(wordAdapter2);
        } else {
            recyclerView.setAdapter(wordAdapter1);
            recyclerView.addItemDecoration(dividerItemDecoration);
        }

        filterWords = wordViewModel.getAllWordsLive();
        //BugFix：将Fragment的View作为LifecycleOwner，防止监听重叠而导致的闪屏现象
        filterWords.observe(getViewLifecycleOwner(), new Observer<List<Word>>() {
            @Override
            public void onChanged(List<Word> words) {
                allWords = words;
                int temp = wordAdapter1.getItemCount();
                if (temp != words.size()) {
                    //BugFix：当删除且不撤销时不向下滚动
                    if (temp < words.size() && !undoAction) {
                        //当数据发生变化，列表平滑滚动，防止数据较多时，列表无反馈
                        recyclerView.smoothScrollBy(0, -200);
                    }
                    undoAction = false;
                    //提交的数据列表，会在后台进行差异化比较，并根据对比结果，来刷新页面
                    wordAdapter1.submitList(words);
                    wordAdapter2.submitList(words);
                }
            }
        });

        //recyclerView添加触摸辅助工具
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                Word wordFrom = allWords.get(viewHolder.getAdapterPosition());
                Word wordTo = allWords.get(target.getAdapterPosition());
                int idTemp = wordFrom.getId();
                wordFrom.setId(wordTo.getId());
                wordTo.setId(idTemp);
                wordViewModel.updateWords(wordFrom, wordTo);
                wordAdapter1.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                wordAdapter2.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final Word wordToDelete = allWords.get(viewHolder.getAdapterPosition());
                wordViewModel.deleteWords(wordToDelete);
                Snackbar.make(activity.findViewById(R.id.wordFragmentView), "删除了一个词汇", Snackbar.LENGTH_SHORT)
                        .setAction("撤销", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                undoAction = true;
                                wordViewModel.insertWords(wordToDelete);
                            }
                        })
                        .show();
            }

            //添加滑动背景和图标
            Drawable icon = ContextCompat.getDrawable(activity, R.drawable.ic_delete_forever_black_24dp);
            Drawable background = new ColorDrawable(Color.LTGRAY);
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                View itemView = viewHolder.itemView;
                int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                int iconLeft, iconRight, iconTop, iconBottom;
                int backLeft, backRight, backTop, backBottom;
                backTop = itemView.getTop();
                backBottom = itemView.getBottom();
                iconTop = itemView.getTop() + iconMargin;
                iconBottom = iconTop + icon.getIntrinsicHeight();
                if (dX > 0) {
                    backLeft = itemView.getLeft();
                    backRight = itemView.getLeft() + (int)dX;
                    background.setBounds(backLeft, backTop, backRight, backBottom);
                    iconLeft = itemView.getLeft() + iconMargin;
                    iconRight = iconLeft + icon.getIntrinsicWidth();
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                } else if (dX < 0) {
                    backRight = itemView.getRight();
                    backLeft = itemView.getRight() + (int)dX;
                    background.setBounds(backLeft, backTop, backRight, backBottom);
                    iconRight = itemView.getRight() - iconMargin;
                    iconLeft = iconRight - icon.getIntrinsicWidth();
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                } else {
                    background.setBounds(0, 0, 0, 0);
                    icon.setBounds(0, 0, 0, 0);
                }
                background.draw(c);
                icon.draw(c);
            }
        }).attachToRecyclerView(recyclerView);

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
                        allWords = words;
                        int temp = wordAdapter1.getItemCount();
                        if (temp != words.size()) {
                            wordAdapter1.submitList(words);
                            wordAdapter2.submitList(words);
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
                    recyclerView.addItemDecoration(dividerItemDecoration);
                    editor.putBoolean(IS_USING_CARD_VIEW, false);
                } else {
                    recyclerView.setAdapter(wordAdapter2);
                    recyclerView.removeItemDecoration(dividerItemDecoration);
                    editor.putBoolean(IS_USING_CARD_VIEW, true);
                }
                editor.apply();
        }
        return super.onOptionsItemSelected(item);
    }
}
