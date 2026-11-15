package edu.birzeit.encs5150.exp7;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

public class MessageFragment extends LoggedFragment {

    public MessageFragment() {
        super(R.layout.fragment_message);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CounterViewModel counter = new ViewModelProvider(requireActivity()).get(CounterViewModel.class);
        TextView messageTextView = view.findViewById(R.id.textView_message);

        // Runs now with the current count, then again every time it changes
        counter.getCount().observe(getViewLifecycleOwner(), count ->
                messageTextView.setText("The button was clicked " + count + " times"));
    }
}
