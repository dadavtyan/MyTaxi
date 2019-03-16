package com.davtyan.mytaxi.view.activitys;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.davtyan.mytaxi.App;
import com.davtyan.mytaxi.api.Constants;
import com.davtyan.mytaxi.R;
import com.davtyan.mytaxi.session.MySession;
import com.davtyan.mytaxi.model.Message;
import com.davtyan.mytaxi.model.User;
import com.davtyan.mytaxi.view.mapkit.activitys.YMapsActivity;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.auth.api.credentials.CredentialsOptions;
import com.google.android.gms.auth.api.credentials.HintRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends AppCompatActivity
        implements OnClickListener {
    private static final int RESOLVE_HINT = 1001;
    private MySession session;
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mCodeView;
    private View mProgressView;
    private View mLoginFormView;
    private View mVerifyFormView;
    private TextView mNameView;
    private InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_email);

        session = new MySession(getBaseContext());
        if (session.isLogin()) {
            startActivity(new Intent(this, YMapsActivity.class));
        } else {
            initView();
            requestHintEmail();
        }
    }

    private void initView() {
        // Set up the login form.
        mEmailView = findViewById(R.id.email);
        mPasswordView = findViewById(R.id.password);
        mNameView = findViewById(R.id.user_name);
        mCodeView = findViewById(R.id.codeText);


        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.verifyButton).setOnClickListener(this);
        findViewById(R.id.to_sign_in).setOnClickListener(this);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        mLoginFormView = findViewById(R.id.login_form);
        mVerifyFormView = findViewById(R.id.verify_form);
        mProgressView = findViewById(R.id.login_progress);

    }


    private void attemptLogin() {
        boolean cancel = false;
        // Reset errors.
        mNameView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);
        String name = mNameView.getText().toString();

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            mPasswordView.requestFocus();
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            mPasswordView.requestFocus();
            cancel = true;
        }
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            mEmailView.requestFocus();
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            mEmailView.requestFocus();
            cancel = true;
        }
        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            mNameView.requestFocus();
            cancel = true;
        }

        if (!cancel) {
            // inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            User user = new User(email, password, name);
            addUser(user);
        }
    }

    private boolean isPasswordValid(String password) {
        for (int i = 0; i < password.length(); i++) {
            if (!(password.charAt(i) > 47 && password.charAt(i) < 58) &&
                            !(password.charAt(i) > 64 && password.charAt(i) < 91) &&
                            !(password.charAt(i) > 96 && password.charAt(i) < 123)) {
                return false;
            }
        }
        return password.length() > 4;
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@") && email.length() > 4;
    }


    private void showProgress() {
        mLoginFormView.setVisibility(View.GONE);
        mProgressView.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mVerifyFormView.setVisibility(View.VISIBLE);
                mProgressView.setVisibility(View.GONE);
            }
        }, 1500);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.email_sign_in_button:
                attemptLogin();
                break;
            case R.id.verifyButton:
                attemptVerify();
                break;
            case R.id.to_sign_in:
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                break;
        }
    }

    private void attemptVerify() {
        String code = mCodeView.getText().toString();
        if (!TextUtils.isEmpty(code)) {
            resendVerificationCode(code);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESOLVE_HINT) {
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                mEmailView.setText(credential.getId());

            }
        }
    }

    private void requestHintEmail() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    requestHint();
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        }, 1000);

    }

    private void requestHint() throws IntentSender.SendIntentException {
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(false)
                .setEmailAddressIdentifierSupported(true)
                .build();

        CredentialsOptions options = new CredentialsOptions.Builder()
                .forceEnableSaveDialog()
                .build();
        CredentialsClient mCredentialsClient = Credentials.getClient(this, options);

        PendingIntent intent = mCredentialsClient.getHintPickerIntent(hintRequest);
        startIntentSenderForResult(intent.getIntentSender(),
                RESOLVE_HINT, null, 0, 0, 0);
    }


    private void addUser(User user) {
        App.getApi().addUser(Constants.CONTENT_TYPE_VALUE, user).enqueue(new Callback<Message>() {
            @Override
            public void onResponse(@NonNull Call<Message> call, @NonNull Response<Message> response) {

                assert response.body() != null;
                if (response.body().getMessage().equals("changePassword") ||
                        response.body().getMessage().equals("createUser"))
                    showProgress();
                else resetData(response.body().getMessage());
                Log.i("myLog", "onResponse: " + response.body());
            }

            @Override
            public void onFailure(@NonNull Call<Message> call, @NonNull Throwable throwable) {
                Log.i("myLog", "onResponse: " + throwable.getMessage());
            }
        });
    }

    private void resetData(String message) {
        mNameView.setError(getString(R.string.error_field_required));
        mPasswordView.setError(getString(R.string.error_field_required));
        mEmailView.setError(message);
        mEmailView.requestFocus();
        mNameView.setText("");
        mPasswordView.setText("");
        mEmailView.setText("");
    }

    private void resendVerificationCode(String codeStr) {
        User.Code code = new User.Code(codeStr);
        App.getApi().verification(Constants.CONTENT_TYPE_VALUE, code).enqueue(new Callback<Message>() {
            @Override
            public void onResponse(@NonNull Call<Message> call, @NonNull Response<Message> response) {
                assert response.body() != null;
                if (response.body().getMessage().equals("OK")) {
                    session.setLogin(true);
                    session.setEmail(mEmailView.getText().toString());
                    session.setPassword(mPasswordView.getText().toString());
                    startActivity(new Intent(RegisterActivity.this, YMapsActivity.class));
                } else {
                    mCodeView.setError("");
                }


                Log.i("myLog", "onResponse: " + response.body().getMessage());
            }

            @Override
            public void onFailure(@NonNull Call<Message> call, @NonNull Throwable throwable) {
                Log.i("myLog", "onResponse: " + throwable.getMessage());
            }
        });
    }


    @Override
    public void onBackPressed() {
    }
}

