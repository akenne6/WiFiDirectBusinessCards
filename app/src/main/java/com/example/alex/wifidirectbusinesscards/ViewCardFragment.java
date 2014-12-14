package com.example.alex.wifidirectbusinesscards;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import ezvcard.Ezvcard;
import ezvcard.VCard;


public class ViewCardFragment extends Fragment {

    private String POSITION;
    private String FILE_NAME;
    private List<VCard> vcardList;
    private int filePosition;

    private OnViewCardFragmentInteractionListener mListener;


    public ViewCardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
           POSITION = getArguments().getString("POSITION");
           FILE_NAME = getArguments().getString("FILE_NAME");
        }
        File tempFile = new File(getActivity().getFilesDir()+"//"+FILE_NAME);
        try {
            vcardList = Ezvcard.parse(tempFile).all();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        if (POSITION.equals("GO_TO_END"))
        {
            filePosition = vcardList.size()-1;
        }
        else
        {
            filePosition = Integer.parseInt(POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_view_card, container, false);
        TextView t = (TextView)v.findViewById(R.id.cardEmail);
        t.setText(vcardList.get(filePosition).getEmails().get(0).getValue());
        t = (TextView)v.findViewById(R.id.cardName);
        t.setText(vcardList.get(filePosition).getFormattedName().getValue());
        t = (TextView)v.findViewById(R.id.cardTitle);
        t.setText(vcardList.get(filePosition).getTitles().get(0).getValue());
        t = (TextView)v.findViewById(R.id.cardCompany);
        t.setText(vcardList.get(filePosition).getOrganization().getValues().get(0));
        t = (TextView)v.findViewById(R.id.cardWork);
        t.setText(vcardList.get(filePosition).getTelephoneNumbers().get(0).getText()+"(Work)");
        t = (TextView)v.findViewById(R.id.cardCell);
        t.setText(vcardList.get(filePosition).getTelephoneNumbers().get(1).getText()+"(Cell)");

        return v;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
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
    public interface OnViewCardFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onViewCardFragmentInteraction(Uri uri);
    }

}
