package com.example.hunter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import android.util.Log;

public class LoginFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "login";
    private SignInButton googleLogin;
    private static final int RC_SIGN_IN = 1;
    private GoogleSignInClient mGoogleSignInClient;
    private String username;
    private String email;
    private String photo;
    private String userId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private Context context;


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myLayout = inflater.inflate(R.layout.fragment_login, container, false);
        context = myLayout.getContext();
//        return inflater.inflate(R.layout.login_fragment, container, false);
        mAuth = FirebaseAuth.getInstance();

        googleLogin = myLayout.findViewById(R.id.googleLogin);
        googleLogin.setSize(SignInButton.SIZE_STANDARD);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestEmail()
//                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
        googleLogin.setOnClickListener(this);

        return myLayout;
    }

    //GOOGLE SIGN IN
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        Log.d(TAG, "intent:success");

    }
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
//        if (requestCode == RC_SIGN_IN) {
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            try {
//                // Google Sign In was successful, authenticate with Firebase
//                GoogleSignInAccount account = task.getResult(ApiException.class);
//                firebaseAuthWithGoogle(account);
//            } catch (ApiException e) {
//                // Google Sign In failed, update UI appropriately
//                Log.w(TAG, "Google sign in failed", e);
//                // ...
//            }
//        }
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    //    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
//        try {
//            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
//            Log.d(TAG, "ohno:"+account.getId());
//
//            DocumentReference userDoc =db.collection("users").document(account.getId());
//            userDoc.get().addOnCompleteListener(task1 -> {
//                if (task1.isSuccessful()) {
//                    DocumentSnapshot documentSnapshot = task1.getResult();
//                    if (documentSnapshot.exists()) {
//                        userId = account.getId();
//                        updateUI(account);
//                        Log.d(TAG, "got:success");
//                    } else {
//                        email = account.getEmail();
//                        photo = account.getPhotoUrl().toString();
//                        userId = account.getId();
//                        User newUser = new User();
//                        newUser.setEmail(email);
//                        newUser.setUsername(username);
//                        newUser.setPhoto(photo);
//                        newUser.setUid(userId);
//                        userDoc.set(newUser);
//                        Log.d(TAG, "nogot:success");
//
//                        updateUI(account);
//                    }
//                }
//
//            });
//            // Signed in successfully, show authenticated UI.
////            updateUI(account);
//        } catch (ApiException e) {
//            // The ApiException status code indicates the detailed failure reason.
//            // Please refer to the GoogleSignInStatusCodes class reference for more information.
//            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
//            updateUI(null);
//        }
//    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            DocumentReference userDoc = db.collection("users").document(user.getUid());

                            userDoc.get().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot = task1.getResult();
                                    if (documentSnapshot.exists()) {
                                        userId = user.getUid();
                                        updateUI(user);
                                        Log.d(TAG, "got:success");
                                    } else {
                                        email = user.getEmail();
                                        photo = user.getPhotoUrl().toString();
                                        userId = user.getUid();
                                        User newUser = new User();
                                        newUser.setEmail(email);
                                        newUser.setUsername(username);
                                        newUser.setPhoto(photo);
                                        newUser.setUid(userId);
                                        userDoc.set(newUser).addOnCompleteListener(task2 -> {
                                            updateUI(user);
                                            Log.d(TAG, "nogot:success");


                                        });

                                    }
                                }

                            });
                        }

                        // ...
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().signOut();
        FirebaseUser currentUser = mAuth.getCurrentUser();
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        if (user == null) {
            Toast.makeText(getActivity(), "Login failed", Toast.LENGTH_LONG).show();
        } else {
            Intent intent = new Intent(getActivity(), FeedActivity.class);
            intent.putExtra("userId", userId);
            Log.d(TAG, "nogot:success" + user.getUid());
            startActivity(intent);

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.googleLogin:
                signIn();
                break;
        }
    }


}
