package edu.birzeit.encs5150.exp7;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

public class NumberFragment extends LoggedFragment {

    public NumberFragment() {
        super(R.layout.fragment_number);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CounterViewModel counter = new ViewModelProvider(requireActivity()).get(CounterViewModel.class);
        TextView numberTextView = view.findViewById(R.id.textView_number);

        counter.getCount().observe(getViewLifecycleOwner(), count ->
                numberTextView.setText(String.valueOf(count)));
    }
}
