package com.bitmediacentre.bitmediacentre.ui.main;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bitmediacentre.bitmediacentre.MainActivity;
import com.bitmediacentre.bitmediacentre.R;


public class SettingsFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;


    public static SettingsFragment newInstance(int index) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment3_main, container, false);

        EditText et = root.findViewById(R.id.machineid);
        //show the value from the preferences:
        SharedPreferences sp = ((MainActivity) getActivity()).sharedPreferences;
        et.setText(sp.getString("machine-id","SETME"));

        et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    SharedPreferences sp = ((MainActivity) getActivity()).sharedPreferences;
                    String value = ((EditText)v).getText().toString();

                    //Save to the preferences...
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("machine-id", value);
                    editor.apply();
                }
            }
        });

        return root;
    }


}
