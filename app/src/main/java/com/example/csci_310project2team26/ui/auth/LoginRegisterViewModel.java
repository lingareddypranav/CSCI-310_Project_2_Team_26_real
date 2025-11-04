package com.example.csci_310project2team26.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LoginRegisterViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public LoginRegisterViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

}
