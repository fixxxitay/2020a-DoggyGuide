package com.technion.doggyguide.loginScreen;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.technion.doggyguide.DogOwnerSignUp;
import com.technion.doggyguide.GoogleSignInActivity;
import com.technion.doggyguide.R;
import com.technion.doggyguide.homeActivity;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DogOwnerConnectionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DogOwnerConnectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DogOwnerConnectionFragment extends Fragment{
    static final int GOOGLE_SIGN_IN = 123;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private final String ORG_DOC_ID = "euHHrQzHbBKNZsvrmpbT";
    private final String MEMBERS_DOC_ID = "reference_to_members";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ProgressDialog mProgressDialog;

    private OnFragmentInteractionListener mListener;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference orgmembersRef;
    private GoogleSignInClient mGSC;
    private GoogleSignInOptions mGSO;

    private Button mLoginBtn;
    private Button mSignUpBtn;
    private Button mGoogleLoginBtn;
    private EditText emailtxt;
    private EditText pwdtxt;


    public static final String EXTRA_CREDINTIAL = "com.technion.doggyguide.EXTRA_CREDINTIAL";
    public static final String EXTRA_ACCOUNT = "com.technion.doggyguide.EXTRA_ACCOUNT";

    public DogOwnerConnectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DogOwnerConnectionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DogOwnerConnectionFragment newInstance(String param1, String param2) {
        DogOwnerConnectionFragment fragment = new DogOwnerConnectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

        }

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        orgmembersRef = db.collection("organizations").document(ORG_DOC_ID).collection(MEMBERS_DOC_ID);

        mProgressDialog = new ProgressDialog(getActivity());

        // Initialize Google Sign In Options
        mGSO = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.Web_Client_ID))
                .requestId()
                .requestEmail()
                .requestProfile()
                .build();

        // Build a GoogleSignInClient with the options specified by mGSO.
        mGSC = GoogleSignIn.getClient(getActivity(), mGSO);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_dog_owner_connection, container, false);
        mLoginBtn = view.findViewById(R.id.btnDogownerlogin);
        mSignUpBtn = view.findViewById(R.id.btnDogownersignup);
        mGoogleLoginBtn = view.findViewById(R.id.btnGoogleLogin);
        emailtxt = view.findViewById(R.id.home_dogowneremail);
        pwdtxt = view.findViewById(R.id.home_dogownerpassword);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog.setTitle("LogIn");
                mProgressDialog.setMessage("Please wait");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
                String email = emailtxt.getText().toString();
                String pwd = pwdtxt.getText().toString();
                if (!validateEmailAndPwd(email, pwd))
                    return;
                signInWithEmailAndPassword(email, pwd);
            }
        });

        mGoogleLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProgressDialog.setTitle("SignUp");
                mProgressDialog.setMessage("Please wait until we can register you");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(view, "Google sign in not fully integrated with database",
                                Snackbar.LENGTH_LONG).show();
                    }
                }, 500);

                Intent intent = mGSC.getSignInIntent();
                startActivityForResult(intent, GOOGLE_SIGN_IN);
            }
        });

        mSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), DogOwnerSignUp.class);
                startActivity(intent);

            }
        });
        return view;
    }

    private boolean validateEmailAndPwd(String email, String pwd) {
        if (email.isEmpty()) {
            emailtxt.setError("Please enter your email address.");
            emailtxt.requestFocus();
            return false;
        } else if (pwd.isEmpty()) {
            pwdtxt.setError("Please enter your password.");
            pwdtxt.requestFocus();
            return false;
        }
        return true;
    }

    private void signInWithEmailAndPassword(String email, String pwd) {
        mAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(getActivity(),
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (mAuth.getCurrentUser().isEmailVerified()) {
                                Intent intent = new Intent(getActivity(), homeActivity.class);
                                getActivity().finish();
                                startActivity(intent);
                            } else {
                                Toast.makeText(getActivity(),
                                        "This email is registered but hasn't been verified yet.\nPlease verify your email",
                                        Toast.LENGTH_LONG).show();
                            }

                        } else {
                            Toast.makeText(getActivity(), task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void signIWithGoogle(final GoogleSignInAccount account) {
        Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
        final AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        final CollectionReference dogowners = db.collection("dog owners");
        dogowners.whereEqualTo("email", account.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<DocumentSnapshot> docs = task.getResult().getDocuments();
                        if (docs.isEmpty()) {
                            Intent intent = new Intent(getActivity(), GoogleSignInActivity.class);
                            intent.putExtra(EXTRA_CREDINTIAL, credential);
                            intent.putExtra(EXTRA_ACCOUNT, account);
                            getActivity().finish();
                            startActivity(intent);
                        } else if (docs.size() == 1) {
                            GoogleSignIn(credential, account);
                        }
                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null)
                    signIWithGoogle(account);
            } catch (ApiException e) {
                Log.w("TAG", "signInResult:failed code=" + e.getStatusCode());
            }

        }
    }



    private void GoogleSignIn(AuthCredential credential, final GoogleSignInAccount account) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "signInWithCredential:success");
                            Intent intent = new Intent(getActivity(), homeActivity.class);
                            getActivity().finish();
                            startActivity(intent);
                        } else {
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
