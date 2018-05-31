package com.example.nesto.myapplication;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.nesto.myapplication.api.data.GithubAPI;
import com.example.nesto.myapplication.api.data.GithubRepo;
import com.example.nesto.myapplication.api.data.GithubRepoDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class WordRepository {

    private WordDao mWordDao;
    private LiveData<List<Word>> mAllWords;
    private LiveData<Double> cant;
    private GithubAPI githubAPI;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    WordRepository(Application application) {
        WordRoomDatabase db = WordRoomDatabase.getDatabase(application);
        mWordDao = db.wordDao();
        mAllWords = mWordDao.getAllWords();
        cant = mWordDao.getCantWord();
        createGithubAPI();
    }

    LiveData<List<Word>> getAllWords() {
        compositeDisposable.add(githubAPI.getRepos()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getRepositoriesObserver()));

        return mAllWords;
    }

    LiveData<Double> getWordA(){return cant; }

    public void insert(Word word) {
        new insertAsyncTask(mWordDao).execute(word);
    }

    private static class insertAsyncTask extends AsyncTask<Word, Void, Void> {

        private WordDao mAsyncTaskDao;

        insertAsyncTask(WordDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Word... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private void createGithubAPI() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .registerTypeAdapter(GithubRepo.class, new GithubRepoDeserializer())
                .create();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request originalRequest = chain.request();

                        Request.Builder builder = originalRequest.newBuilder().header("Authorization",
                                Credentials.basic("mail", "password")); // TODO: Cambiar por una forma más adecuada de obtener

                        Request newRequest = builder.build();
                        return chain.proceed(newRequest);
                    }
                }).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GithubAPI.ENDPOINT)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        githubAPI = retrofit.create(GithubAPI.class);
    }


    private DisposableSingleObserver<List<GithubRepo>> getRepositoriesObserver() {
        return new DisposableSingleObserver<List<GithubRepo>>() {
            @Override
            public void onSuccess(List<GithubRepo> repos) {
                if (!repos.isEmpty()) {
                    for(GithubRepo repo: repos)
                        insert(new Word(repo.getName() + " " + repo.getOwner()));
                }
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                // Toast.makeText(, "Can not load repositories", Toast.LENGTH_SHORT).show();

            }
        };
    }


}