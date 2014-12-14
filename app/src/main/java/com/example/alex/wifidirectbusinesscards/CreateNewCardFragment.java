package com.example.alex.wifidirectbusinesscards;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


public class CreateNewCardFragment extends Fragment {

    private String name;
    private String workPhoneNumber;
    private String mobilePhoneNumber;
    private String emailAddress;
    private String title;
    private String company;


    private OnCreateCardFragmentInteractionListener mListener;


    public CreateNewCardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_create_new_card, container, false);
        Button b = (Button) v.findViewById(R.id.submitCardInfo);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText temp = (EditText)((View)v.getParent()).findViewById(R.id.editName);
                name = temp.getText().toString();
                temp = (EditText)((View)v.getParent()).findViewById(R.id.editPhone1);
                workPhoneNumber = temp.getText().toString();
                temp = (EditText)((View)v.getParent()).findViewById(R.id.editPhoneCell);
                mobilePhoneNumber = temp.getText().toString();
                temp = (EditText)((View)v.getParent()).findViewById(R.id.editEmail);
                emailAddress = temp.getText().toString();
                temp = (EditText)((View)v.getParent()).findViewById(R.id.editJobTitle);
                title = temp.getText().toString();
                temp = (EditText)((View)v.getParent()).findViewById(R.id.editCompany);
                company = temp.getText().toString();

                mListener.onCardCreated(name, workPhoneNumber, mobilePhoneNumber, emailAddress, title, company);

            }
        });
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnCreateCardFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnCreateCardFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onCardCreated(String name, String workNumber, String cellNumber, String email, String title, String company);
    }

}
