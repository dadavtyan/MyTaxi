package com.davtyan.mytaxi.view.activitys;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.davtyan.mytaxi.R;
import com.davtyan.mytaxi.view.mapkit.activitys.YMapsActivity;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.auth.api.credentials.CredentialsOptions;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import com.rilixtech.Country;
import com.rilixtech.CountryCodePicker;

import java.util.concurrent.TimeUnit;

import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;


public class AuthActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";
    private static final int INITIALIZED = 1;
    private static final int CODE_SENT = 2;
    private static final int VERIFY_FAILED = 3;
    private static final int VERIFY_SUCCESS = 4;
    private static final int SIGN_IN_FAILED = 5;
    private static final int SIGN_IN_SUCCESS = 6;
    private  int TEMP_STATE = 0;

    private  String STATE_LOG = "STATE_LOG";

    private CountryCodePicker ccp;
    private AppCompatEditText mPhoneNumberField;
    private EditText mVerificationField;

    private ViewGroup sendNumberArea;
    private ViewGroup verificationArea;

    private boolean isInitialized  = false;
    private boolean mVerificationInProgress = false;
    private String mVerificationId;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;

    private static final int RESOLVE_HINT = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        initView();
        if (isNetwork()){
            onSelectedCountryItem();
            verificationCallback();
            mAuth = FirebaseAuth.getInstance();
            if (getIntent().getExtras() != null) {
                mAuth.signOut();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESOLVE_HINT) {
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                String number = getNationalNumber(credential.getId());
                mPhoneNumberField.setText(number);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isNetwork()){
            FirebaseUser currentUser = mAuth.getCurrentUser();
            isAccess(currentUser);

            if (mVerificationInProgress && validatePhoneNumber()) {
                sendCode(ccp.getNumber());
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sendButton:
                if (!validatePhoneNumber()) {
                    return;
                }
                sendCode(ccp.getNumber());
                break;
            case R.id.resendButton:
                resendVerificationCode(ccp.getNumber(), mResendToken);
                break;
            case R.id.verifyButton:
                verifyCode(mVerificationId);
                break;
        }
    }


    private void initView() {
        ccp = findViewById(R.id.ccp);
        mPhoneNumberField = findViewById(R.id.phone_number_edt);
        mVerificationField = findViewById(R.id.codeText);
        ccp.registerPhoneNumberTextView(mPhoneNumberField);

        findViewById(R.id.sendButton).setOnClickListener(this);
        findViewById(R.id.verifyButton).setOnClickListener(this);
        findViewById(R.id.resendButton).setOnClickListener(this);

        sendNumberArea = findViewById(R.id.phoneAuthFields);
        verificationArea = findViewById(R.id.signedInButtons);
    }

    private boolean isNetwork(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        if(cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            Toast.makeText(AuthActivity.this, "true", Toast.LENGTH_LONG).show();
            return true;
        }
        else {
            Toast.makeText(AuthActivity.this, "no Internet", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(AuthActivity.this,CallActivity.class);
            startActivity(intent);
            return false;
        }
    }

    private void requestHintPhoneNumber() {
        if (TEMP_STATE != SIGN_IN_SUCCESS){
            Log.i("temp","..." + TEMP_STATE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        requestHint();
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }, 1500);
        }

    }

    private void requestHint() throws IntentSender.SendIntentException {
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .setEmailAddressIdentifierSupported(false)
                .build();

        CredentialsOptions options = new CredentialsOptions.Builder()
                .forceEnableSaveDialog()
                .build();
        CredentialsClient mCredentialsClient = Credentials.getClient(this, options);

        PendingIntent intent = mCredentialsClient.getHintPickerIntent(hintRequest);
        startIntentSenderForResult(intent.getIntentSender(),
                RESOLVE_HINT, null, 0, 0, 0);
    }

    private String getNationalNumber(String fullNumber){
        long nationalNumber = 0;
        try {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.createInstance(this);
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(fullNumber, "");
            nationalNumber = numberProto.getNationalNumber();
            Log.i("code", "national number " + nationalNumber);
        } catch (NumberParseException e) {
            System.err.println("NumberParseException was thrown: " + e.toString());
        }

        return String.valueOf(nationalNumber);
    }

    private void onSelectedCountryItem() {
        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected(Country selectedCountry) {
                Toast.makeText(AuthActivity.this, "Name: " + selectedCountry.getName(), Toast.LENGTH_SHORT).show();

            }
        });
    }


    private void toMapsActivity(String phoneNumber) {
        Intent intent = new Intent(AuthActivity.this, YMapsActivity.class);
        intent.putExtra("Number",  phoneNumber);
        startActivity(intent);
    }


    private void sendCode(String number) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,               // Phone number to verify
                60,                     // Timeout duration
                TimeUnit.SECONDS,          // Unit of timeout
                this,               // Activity (for callback binding)
                verificationCallbacks);    // OnVerificationStateChangedCallbacks

        mVerificationInProgress = true;
    }

    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                verificationCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private void verifyCode(String verificationId) {

        String code = mVerificationField.getText().toString();
        if (TextUtils.isEmpty(code)) {
            mVerificationField.setError("Cannot be empty.");
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        signInWithPhoneAuthCredential(credential);
    }

    private void verificationCallback(){
        verificationCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                mVerificationInProgress = false;
                updateUI(VERIFY_SUCCESS, credential);
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                mVerificationInProgress = false;

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    mPhoneNumberField.setError("Invalid phone number.");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                }
                updateUI(VERIFY_FAILED);
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                updateUI(CODE_SENT);
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = task.getResult().getUser();
                            updateUI(SIGN_IN_SUCCESS, firebaseUser);
                            // [END_EXCLUDE]
                        } else {

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                                mVerificationField.setError("Invalid code.");
                            }

                            updateUI(SIGN_IN_FAILED);

                        }
                    }
                });
    }


    private void isAccess(FirebaseUser user) {
        if (user != null) {
            updateUI(SIGN_IN_SUCCESS, user);
        }
        else if(!isInitialized) {
            updateUI(INITIALIZED);
        }
    }

    private void updateUI(int uiState) {
        updateUI(uiState, mAuth.getCurrentUser(), null);
    }

    private void updateUI(int uiState, FirebaseUser user) {
        updateUI(uiState, user, null);
    }

    private void updateUI(int uiState, PhoneAuthCredential cred) {
        updateUI(uiState, null, cred);
    }




    private void updateUI(int uiState, FirebaseUser user, PhoneAuthCredential cred) {
        switch (uiState) {
            case INITIALIZED:
                Log.i(STATE_LOG,"INITIALIZED: " + INITIALIZED);
                requestHintPhoneNumber();
                isInitialized = true;
                changeVisibility(View.VISIBLE,View.GONE);
                break;
            case CODE_SENT:
                Log.i(STATE_LOG,"CODE_SENT: " + CODE_SENT);
                changeVisibility(View.GONE,View.VISIBLE);
                break;
            case VERIFY_FAILED:
                Log.i(STATE_LOG,"VERIFY_FAILED: " + VERIFY_FAILED);
                break;
            case VERIFY_SUCCESS:
                if (cred != null) {
                    if (cred.getSmsCode() != null)
                        mVerificationField.setText(cred.getSmsCode());
                    Log.i(STATE_LOG,"VERIFY_SUCCESS: " + VERIFY_SUCCESS);
                }
                break;
            case SIGN_IN_FAILED:
                Log.i(STATE_LOG,"SIGN_IN_FAILED: " + SIGN_IN_FAILED);
                break;
            case SIGN_IN_SUCCESS:

                Log.i(STATE_LOG,"SIGN_IN_SUCCESS: " + SIGN_IN_SUCCESS);
                break;
        }

        if (user != null) toMapsActivity(user.getPhoneNumber());
        TEMP_STATE = uiState;

    }

    private void changeVisibility(int visibility_1 ,int visibility_2) {
        sendNumberArea.setVisibility(visibility_1);
        verificationArea.setVisibility(visibility_2);
    }

    private boolean validatePhoneNumber() {
        String phoneNumber = mPhoneNumberField.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            mPhoneNumberField.setError("Invalid phone number.");
            return false;
        }
        return true;
    }
}