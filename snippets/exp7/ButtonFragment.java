package edu.birzeit.encs5150.exp7;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

public class ButtonFragment extends LoggedFragment {

    public ButtonFragment() {
        super(R.layout.fragment_button);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // The Activity's ViewModel, shared with every Fragment in it
        CounterViewModel counter = new ViewModelProvider(requireActivity()).get(CounterViewModel.class);

        Button countButton = view.findViewById(R.id.button_count);
        countButton.setOnClickListener(v -> counter.increment());
    }
}
