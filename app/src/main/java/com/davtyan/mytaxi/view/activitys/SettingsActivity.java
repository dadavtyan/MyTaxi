package com.davtyan.mytaxi.view.activitys;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.davtyan.mytaxi.App;
import com.davtyan.mytaxi.R;
import com.davtyan.mytaxi.api.Constants;
import com.davtyan.mytaxi.model.Message;
import com.davtyan.mytaxi.model.User;
import com.davtyan.mytaxi.session.MySession;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private MySession session;
    private LinearLayout view;
    private int viewId;

    private String mEmail;
    private String mPassword;

    private EditText mPasswordEdit;
    private EditText mNewPasswordEdit;
    private EditText mConformPasswordEdit;
    private Button submitButton;
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        session = new MySession(getBaseContext());
        findViewById(R.id.change_password).setOnClickListener(this);
        findViewById(R.id.change_language).setOnClickListener(this);
        findViewById(R.id.change_name).setOnClickListener(this);

        mEmail = session.getEmail();
        mPassword = session.getPassword();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_password:
                showDialog(1);
                break;
            case R.id.change_language:
                showDialog(2);
                break;
            case R.id.change_name:
                showDialog(3);
                break;
        }
        viewId = v.getId();
    }

    @SuppressLint("InflateParams")
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 1:
                view = getDialogView(R.layout.dialog_change_password);
                break;
            case 2:
                view = getDialogView(R.layout.dialog_change_language);
                break;
            case 3:
                view = getDialogView(R.layout.change_name);
                break;
        }
        builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setView(view);
        builder.setCancelable(false);
        alertDialog = builder.create();
        return alertDialog;
    }

    private LinearLayout getDialogView(@LayoutRes int resource) {
        return (LinearLayout) getLayoutInflater().inflate(resource, null);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        switch (id) {
            case 1:
                changePassword(dialog);
                break;
            case 2:
                changeLanguage();
                break;
            case 3:
                changeName();
                break;
        }
        onMyBackPressed();
    }

    private void changePassword(Dialog dialog) {
        submitButton = dialog.findViewById(R.id.submit_button);
        mPasswordEdit = dialog.findViewById(R.id.change_password);
        mNewPasswordEdit = dialog.findViewById(R.id.new_password);
        mConformPasswordEdit = dialog.findViewById(R.id.confirm_password);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPassword();
            }
        });
    }

    private void requestChangePassword(String newPassword) {

        User user = new User(mEmail, newPassword, "");
        App.getApi().changePassword(Constants.CONTENT_TYPE_VALUE, user).enqueue(new Callback<Message>() {
            @Override
            public void onResponse(@NonNull Call<Message> call, @NonNull Response<Message> response) {
                assert response.body() != null;
                Log.i("myLog", "onResponse: " + response.body().getMessage());
            }

            @Override
            public void onFailure(@NonNull Call<Message> call, @NonNull Throwable throwable) {
                Log.i("myLog", "onResponse: " + throwable.getMessage());
            }
        });
    }

    private void checkPassword() {
        boolean cancel = false;
        // Reset errors.
        mPasswordEdit.setError(null);
        mNewPasswordEdit.setError(null);
        mConformPasswordEdit.setError(null);

        String password = mPasswordEdit.getText().toString();
        String newPassword = mNewPasswordEdit.getText().toString();
        String conformPassword = mConformPasswordEdit.getText().toString();

        if (TextUtils.isEmpty(newPassword)) {
            mNewPasswordEdit.setError(getString(R.string.error_field_required));
            mNewPasswordEdit.requestFocus();
            cancel = true;
        }
        if (TextUtils.isEmpty(conformPassword)) {
            mConformPasswordEdit.setError(getString(R.string.error_field_required));
            mConformPasswordEdit.requestFocus();
            cancel = true;
        } else if (!conformPassword.equals(newPassword)) {
            mConformPasswordEdit.setError(getString(R.string.passwords_do_not_match));
            mConformPasswordEdit.requestFocus();
            mConformPasswordEdit.setText("");
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            mPasswordEdit.setError(getString(R.string.error_field_required));
            mPasswordEdit.requestFocus();
            cancel = true;
        } else if (!password.equals(mPassword)) {
            mPasswordEdit.setError(getString(R.string.password_is_incorrect));
            Log.i("vADBDSVDD","fff: " + mPassword);
            Log.i("vADBDSVDD","fff: " + password);
            mPasswordEdit.requestFocus();
            mPasswordEdit.setText("");
            cancel = true;
        }


        if (!cancel) {
            requestChangePassword(newPassword);
            alertDialog.cancel();
            Log.i("vADBDSVDD","fff" + newPassword);
            // inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    private void changeLanguage() {
    }

    private void changeName() {
    }

    public void onMyBackPressed() {

        alertDialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    Log.i("vADBDSVDD","fff" + "onBackPressed");
                    alertDialog.cancel();
                }
                return true;
            }
        });
    }


}
