package au.edu.anu.cs.sparkee.ui.participation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ParticipationViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ParticipationViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}