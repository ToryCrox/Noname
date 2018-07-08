package com.tory.library;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private static final String TAG = "ExampleInstrumentedTest";

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.tory.preference.test", appContext.getPackageName());
    }

    @Test
    public void rxTest() throws Exception {
        Log.d(TAG, "main pid=" + Thread.currentThread().getId());
        Observable.create((ObservableOnSubscribe<String>) emitter -> {
                    Log.d(TAG, "create pid=" + Thread.currentThread().getId());
                    emitter.onNext("s");
                    emitter.onComplete();
              }).subscribeOn(Schedulers.io())
                .doOnNext(s -> Log.d(TAG, "doOnNext pid=" + Thread.currentThread().getId()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> Log.d(TAG, "subscribe onNext pid=" + Thread.currentThread().getId()));

        List<String> list = Arrays.asList("a", "b", "c");
        Observable.fromIterable(list)
                .map(new Function<String, Integer>() {
                    @Override
                    public Integer apply(String s) throws Exception {
                        return Integer.parseInt(s);
                    }
                }).toList();
    }
}
