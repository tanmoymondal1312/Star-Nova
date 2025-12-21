package com.mediaghor.starnova.Models;// BottomNavViewModel.java

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BottomNavViewModel extends ViewModel {
    private final MutableLiveData<Integer> navHeight = new MutableLiveData<>(0);

    public LiveData<Integer> getNavHeight() {
        return navHeight;
    }

    public void setNavHeight(int height) {
        // Corrected comparison: Use != for primitive int comparison
        if (height > 0 && navHeight.getValue() != null && height != navHeight.getValue()) {
            navHeight.setValue(height);
        }
        // Simplified approach (Recommended): Just set the value if positive.
        // if (height > 0) {
        //     navHeight.setValue(height);
        // }
    }
}