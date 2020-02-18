package software.baby.wordnotes.view;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import software.baby.wordnotes.R;
import software.baby.wordnotes.entity.Word;
import software.baby.wordnotes.viewmodel.WordViewModel;

/**
 * Created by Du Senmiao on 2020/02/18
 */
public class NoteFragment extends Fragment {
    private WordViewModel wordViewModel;
    private InputMethodManager inputMethodManager;

    private EditText editTextEnglish, editTextChinese;
    private Button buttonSubmit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_note, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentActivity activity = requireActivity();
        wordViewModel = new ViewModelProvider(activity).get(WordViewModel.class);
        editTextEnglish = activity.findViewById(R.id.editTextEnglish);
        editTextChinese = activity.findViewById(R.id.editTextChinese);
        buttonSubmit = activity.findViewById(R.id.buttonSubmit);
        buttonSubmit.setEnabled(false);
        editTextEnglish.requestFocus();
        //系统键盘自动跳出
        inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(editTextEnglish, 0);
        //添加文本监视器
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String english = editTextEnglish.getText().toString().trim();
                String chinese = editTextChinese.getText().toString().trim();
                buttonSubmit.setEnabled(!english.isEmpty() && !chinese.isEmpty());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        editTextEnglish.addTextChangedListener(textWatcher);
        editTextChinese.addTextChangedListener(textWatcher);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String english = editTextEnglish.getText().toString().trim();
                String chinese = editTextChinese.getText().toString().trim();
                Word word = new Word(english, chinese);
                wordViewModel.insertWords(word);
                NavController navController = Navigation.findNavController(view);
                navController.navigateUp();
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });
    }
}
