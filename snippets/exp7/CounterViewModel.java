package edu.birzeit.encs5150.exp7;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// The data the Fragments share: how many times the button was clicked
public class CounterViewModel extends ViewModel {

    private final MutableLiveData<Integer> count = new MutableLiveData<>(0);

    // Read-only for the Fragments that show the count
    public LiveData<Integer> getCount() {
        return count;
    }

    public void increment() {
        count.setValue(count.getValue() + 1);
    }
}
