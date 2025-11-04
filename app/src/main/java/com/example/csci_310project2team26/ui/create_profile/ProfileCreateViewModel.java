package com.example.csci_310project2team26.ui.create_profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProfileCreateViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ProfileCreateViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
