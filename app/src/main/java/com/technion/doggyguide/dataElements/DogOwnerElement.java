package com.technion.doggyguide.dataElements;

import java.util.List;

public class DogOwnerElement {
    private final String mOrgId = "bayet.ovid@igdcb.org";
    private String mName;
    private String mEmail;
    private String mDogName;
    private String mDogBreed;
    private String mImageUrl;
    private String mDogImageUrl;
    private String mStatus;
    private String mAboutDog;
    private List<String> mTokens;



    public DogOwnerElement() {
        //Need an Empty Constructor
    }

    public DogOwnerElement(String mName, String mEmail, String mDogName,
                           String mDogBreed, String mImageUrl, String mStatus, List<String> mTokens) {
        this.mName = mName;
        this.mEmail = mEmail;
        this.mDogName = mDogName;
        this.mDogBreed = mDogBreed;
        this.mImageUrl = mImageUrl;
        this.mStatus = mStatus;
        this.mTokens = mTokens;
        this.mAboutDog = "My name is " + mDogName + " and I am a " + mDogBreed;
    }

    public String getmOrgId() {
        return mOrgId;
    }

    public String getmName() {
        return mName;
    }

    public String getmEmail() {
        return mEmail;
    }

    public String getmDogName() {
        return mDogName;
    }

    public String getmDogBreed() {
        return mDogBreed;
    }

    public String getmImageUrl() {
        return mImageUrl;
    }

    public String getmStatus() {
        return mStatus;
    }

    public List<String> getmTokens() {
        return mTokens;
    }

    public String getmAboutDog() {
        return mAboutDog;
    }

    public void setmStatus(String mStatus) {
        this.mStatus = mStatus;
    }

    public String getmDogImageUrl() { return mDogImageUrl; }
}
