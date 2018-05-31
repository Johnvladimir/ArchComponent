package com.example.nesto.myapplication;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

public class WordViewModel extends AndroidViewModel {

    private WordRepository mRepository;

    private LiveData<List<Word>> mAllWords;
    private LiveData<Double> cant;

    public WordViewModel(Application application) {
        super(application);
        mRepository = new WordRepository(application);
        mAllWords = mRepository.getAllWords();
        cant = mRepository.getWordA();
    }

    LiveData<List<Word>> getAllWords() {
        return mAllWords;
    }
    LiveData<Double> getCant(){return cant;}

    public void insert(Word word) {
        mRepository.insert(word);
    }

}
