package com.example.zemochat.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.zemochat.Repository.ProfileRepository;
import com.example.zemochat.UserModel;

public class ProfileViewModel extends ViewModel {

    ProfileRepository profileRepository = ProfileRepository.getInstance();

    public LiveData<UserModel> getUser() {
        return profileRepository.getUser();
    }

    //after user selecting his updated image take uri and store it in firebase storage after that i will getting uri from firebase storage and i will call this method and repo take uri and store it in firebase
    public void editImage(String uri) {
        profileRepository.editImage(uri);
    }

    //after user update his status i will call this method and path for it new status and the repo take new status and updating it in firebase
    public void editStatus(String status) {
        profileRepository.editStatus(status);
    }

    //after user update his name i will call this method and path for it new name and the repo take new name and updating it in firebase

    public void edtUsername(String name) {

        profileRepository.editUsername(name);
    }

}
