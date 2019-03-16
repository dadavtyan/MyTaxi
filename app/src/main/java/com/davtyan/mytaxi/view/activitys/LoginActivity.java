package com.davtyan.mytaxi.view.activitys;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.davtyan.mytaxi.App;
import com.davtyan.mytaxi.R;
import com.davtyan.mytaxi.api.Constants;
import com.davtyan.mytaxi.model.Message;
import com.davtyan.mytaxi.model.User;
import com.davtyan.mytaxi.session.MySession;
import com.davtyan.mytaxi.view.mapkit.activitys.YMapsActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEmailView;
    private EditText mPasswordView;
    private MySession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    private void initView() {
        mEmailView = findViewById(R.id.email);
        mPasswordView = findViewById(R.id.password);

        findViewById(R.id.to_sign_up).setOnClickListener(this);
        findViewById(R.id.login_button).setOnClickListener(this);
        session = new MySession(getBaseContext());
//        mProgressView = findViewById(R.id.login_progress);

    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_button:
                attemptRegister();
                break;
            case R.id.to_sign_up:
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                break;
        }
    }

    private void attemptRegister() {
        boolean cancel = false;
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

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


        if (!cancel) {
           User user = new User(email,password,"");
           login(user);
        }
    }



    private boolean isEmailValid(String email) {
        return true;
    }

    private boolean isPasswordValid(String password) {
        return true;
    }

    private void login(User user) {
        App.getApi().login(Constants.CONTENT_TYPE_VALUE, user).enqueue(new Callback<Message>() {
            @Override
            public void onResponse(@NonNull Call<Message> call, @NonNull Response<Message> response) {
                if (response.body() == null) return;
                if (response.body().getMessage().equals("OK")){
                    session.setLogin(true);
                    startActivity(new Intent(LoginActivity.this, YMapsActivity.class));
                }
                Log.i("myLog", "onResponse: " + response.body().getMessage());

            }

            @Override
            public void onFailure(@NonNull Call<Message> call, @NonNull Throwable throwable) {
                Log.i("myLog", "onResponse: " + throwable.getMessage());
            }
        });
    }
}
