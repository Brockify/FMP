package com.skyrealm.brockyy.findmypeepsapp;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class Login extends AppCompatActivity implements OnClickListener {
    //Declare all variables and classes
    private EditText users, pass, code, usaname;
    private Button bLogin, bRegister, bForgot, bverif, resphone,resemail;
    private TextView useremailforg;
    private TextView tvforgot, resendverif;
    private boolean Regist = false;
    int verify = 1;
    // Progress Dialog
    private ProgressDialog pDialog;
    TabLayout setupLogin = new TabLayout();
    // JSON parser class declared
    JSONParser jsonParser = new JSONParser();
    private static final String LOGIN_URL = "http://www.skyrealmstudio.com/cgi-bin/new_login.py";
    private static final String VERIFY_URL = "http://skyrealmstudio.com/cgi-bin/verifyaccount.py";
    private static final String Forgot_URL = "http://skyrealmstudio.com/cgi-bin/forgot.py";
    private static final String Email_URL = "http://skyrealmstudio.com/cgi-bin/resendemail.py";
    private static final String Phone_URL = "http://skyrealmstudio.com/cgi-bin/resendphone.py";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_ID = "number";
    private static final String TAG_noverif = "noverify";
    private static final String TAG_username = "username";
    private static final String TAG_FIRSTLOG = "firstlog";
    private String Number;
    private String First;
    AlertDialog alert, alert2, alert3;
    private String username;
    private String password;
    private Toast backtoast;
    GPSTracker gps = new GPSTracker(this);
    private CheckBox saveLoginCheckBox;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;
    public int checks;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //set variables to xml fields
        users = (EditText) findViewById(R.id.username);
        pass = (EditText) findViewById(R.id.password);
        bLogin = (Button) findViewById(R.id.login);
        final CheckBox checkBox = (CheckBox) findViewById(R.id.remember);
        bLogin.setOnClickListener(this);
        bRegister = (Button) findViewById(R.id.registerlog);
        bRegister.setOnClickListener(this);
        tvforgot = (TextView) findViewById(R.id.forgotbut);
        resendverif = (TextView) findViewById(R.id.rsndverification);


        resendverif.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflater = LayoutInflater.from(Login.this);
                AlertDialog.Builder build = new AlertDialog.Builder(Login.this);
                View promptView = layoutInflater.inflate(R.layout.popout_resendverification, null);
                resphone = (Button) promptView.findViewById(R.id.VerifPhone);
                resemail = (Button) promptView.findViewById(R.id.VerifEmail);
                usaname = (EditText) promptView.findViewById(R.id.Reuser);
                resphone.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new Attemptphone().execute();
                    }
                });
                resemail.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new Attemptemail().execute();
                    }
                });

                build.setView(promptView);
                alert3 = build.create();
                alert3.show();
            }
        });

        tvforgot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflater = LayoutInflater.from(Login.this);
                AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                View promptView = layoutInflater.inflate(R.layout.activity_forgot, null);
                bForgot = (Button) promptView.findViewById(R.id.forgotbutton);
                useremailforg = (EditText) promptView.findViewById(R.id.forgotuser);
                bForgot.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new Attemptreset().execute();
                    }
                });

                builder.setView(promptView);
                alert = builder.create();
                alert.show();
            }
        });

        saveLoginCheckBox = (CheckBox) findViewById(R.id.remember);
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();
        //looks if logged out
        Boolean LoggedOut = getIntent().getBooleanExtra("LoggedOut", false);

        //Handles "remember me"
        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin == true) {
            users.setText(loginPreferences.getString("username", ""));
            pass.setText(loginPreferences.getString("password", ""));
            saveLoginCheckBox.setChecked(true);
        }
        if (saveLoginCheckBox.isChecked() && !LoggedOut)
        {
            username = users.getText().toString();
            password = pass.getText().toString();

            if (saveLoginCheckBox.isChecked()) {
                loginPrefsEditor.putBoolean("saveLogin", true);
                loginPrefsEditor.putString("username", username);
                loginPrefsEditor.putString("password", password);
                loginPrefsEditor.commit();
            } else {
                loginPrefsEditor.clear();
                loginPrefsEditor.commit();
            }
            if(gps.haveNetworkConnection())
            {
                new AttemptLogin().execute();
            }else
            {
                gps.LoginAlert();
            }
        } else {
            saveLoginCheckBox.setChecked(false);
        }
    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            //Handles login button
            case R.id.login:
                username = users.getText().toString();
                password = pass.getText().toString();

                if (saveLoginCheckBox.isChecked()) {
                    loginPrefsEditor.putBoolean("saveLogin", true);
                    loginPrefsEditor.putString("username", username);
                    loginPrefsEditor.putString("password", password);
                    loginPrefsEditor.commit();
                } else {
                    loginPrefsEditor.clear();
                    loginPrefsEditor.commit();
                }
                if(gps.haveNetworkConnection())
                {
                    new AttemptLogin().execute();
                }else
                {
                    gps.LoginAlert();
                }


                break;
            //Handles register button
            case R.id.registerlog:

                Intent ii = new Intent(Login.this, Register.class);
                finish();
                startActivity(ii);

                break;
            default:
                break;
        }
    }


    @Override
    public void onBackPressed() {

        if (backtoast != null && backtoast.getView().getWindowToken() != null) {
            finish();
        } else {
            backtoast = Toast.makeText(this, "Press back to exit", Toast.LENGTH_SHORT);
            backtoast.show();
        }

    }

    class Attemptreset extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         * */
        boolean failure = false;
        String use = useremailforg.getText().toString();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Login.this);
            pDialog.setMessage("Resetting...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // here Check for success tag


            int success;

            String ue = use;


            try {

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("useremail", ue));

                JSONObject json = jsonParser.makeHttpRequest(
                        Forgot_URL, "POST", params);

                // checking  log for json response

                // success tag for json
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    alert.cancel();
                    return json.getString(TAG_MESSAGE);
                }else{

                    return json.getString(TAG_MESSAGE);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
        /**
         * Once the background process is done we need to  Dismiss the progress dialog asap
         * **/
        protected void onPostExecute(String message) {

            pDialog.dismiss();
            if (message != null){
                Toast.makeText(Login.this, message, Toast.LENGTH_LONG).show();
            }
        }
    }



    class Attemptphone extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         * */
        boolean failure = false;
        String usernames = usaname.getText().toString();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Login.this);
            pDialog.setMessage("Resending...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // here Check for success tag


            int success;

            String ue = usernames;


            try {

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("user", ue));

                JSONObject json = jsonParser.makeHttpRequest(
                        Phone_URL, "POST", params);

                // checking  log for json response

                // success tag for json
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    alert3.cancel();
                    return json.getString(TAG_MESSAGE);
                }else{

                    return json.getString(TAG_MESSAGE);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
        /**
         * Once the background process is done we need to  Dismiss the progress dialog asap
         * **/
        protected void onPostExecute(String message) {

            pDialog.dismiss();
            if (message != null){
                Toast.makeText(Login.this, message, Toast.LENGTH_LONG).show();
            }
        }
    }


    class Attemptemail  extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         * */
        boolean failure = false;
        String usernames = usaname.getText().toString();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Login.this);
            pDialog.setMessage("Resending...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // here Check for success tag


            int success;

            String ue = usernames;


            try {

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("user", ue));

                JSONObject json = jsonParser.makeHttpRequest(
                        Email_URL, "POST", params);

                // checking  log for json response

                // success tag for json
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    alert3.cancel();
                    return json.getString(TAG_MESSAGE);
                }else{

                    return json.getString(TAG_MESSAGE);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
        /**
         * Once the background process is done we need to  Dismiss the progress dialog asap
         * **/
        protected void onPostExecute(String message) {

            pDialog.dismiss();
            if (message != null){
                Toast.makeText(Login.this, message, Toast.LENGTH_LONG).show();
            }
        }
    }


    //class sends post and recieves response
    class AttemptLogin extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Login.this);
            pDialog.setMessage("Attempting to login...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

        }

        @Override
        protected String doInBackground(String... args) {

            // create tags
            int success;

            boolean failure = false;


            try {

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("password", password));


                JSONObject json = jsonParser.makeHttpRequest(
                        LOGIN_URL, "POST", params);

                if(json != null)
                {
                    // set up tags for json
                    success = json.getInt(TAG_SUCCESS);
                    verify = json.getInt(TAG_noverif);
                } else {
                    success = 0;
                    verify = 1;
                }

                if (success == 1 && verify == 1)
                {
                    username = json.getString(TAG_username);
                    First = json.getString(TAG_FIRSTLOG);
                    Number = json.getString(TAG_ID);
                    Intent ii = new Intent(Login.this, TabLayout.class);
                    ii.putExtra("username", username);
                    ii.putExtra("Number", Number);
                    ii.putExtra("First",First);

                    finish();

                    startActivity(ii);

                    return json.getString(TAG_MESSAGE);
                } else
                {

                    return "Error: Could not login";

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        public void timerDelayRemoveDialog(long time, final Dialog d){
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    d.dismiss();
                    Toast.makeText(Login.this, "Login timed out. Try again later.", Toast.LENGTH_LONG).show();
                }
            }, time);
        }

        protected void onPostExecute(String message) {
            long timeout = 8000;
            pDialog.dismiss();
            if (message != null) {
                if(verify != 1)
                {
                    LayoutInflater layoutInflater = LayoutInflater.from(Login.this);
                    AlertDialog.Builder Built = new AlertDialog.Builder(Login.this);
                    View promptViewer = layoutInflater.inflate(R.layout.popout_verify, null);
                    bverif = (Button) promptViewer.findViewById(R.id.BVerify);
                    code = (EditText) promptViewer.findViewById(R.id.VerifyCode);



                    bverif.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new AttemptVerify().execute();
                        }
                    });
                    Built.setView(promptViewer);
                    alert2 = Built.create();
                    alert2.show();
                }else{
                    Toast.makeText(Login.this, message, Toast.LENGTH_LONG).show();
                }

            }


        }
    }




    //class sends post and recieves response
    class AttemptVerify extends AsyncTask<String, String, String> {
        int verify;
        String coder;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Login.this);
            pDialog.setMessage("Attempting to Verify...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            coder = code.getText().toString();
            pDialog.show();

        }

        @Override
        protected String doInBackground(String... args) {

            // create tags
            int success;
            String ver = coder;
            boolean failure = false;


            try {


                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("verify", ver));


                JSONObject json = jsonParser.makeHttpRequest(
                        VERIFY_URL, "POST", params);

                // set up tags for json
                success = json.getInt(TAG_SUCCESS);


                if (success == 1)
                {
                    alert2.cancel();
                    return json.getString(TAG_MESSAGE);
                } else
                {

                    return json.getString(TAG_MESSAGE);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        public void timerDelayRemoveDialog(long time, final Dialog d){
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    d.dismiss();
                    Toast.makeText(Login.this, "Verification timed out. Try again later.", Toast.LENGTH_LONG).show();
                }
            }, time);
        }

        protected void onPostExecute(String message) {
            pDialog.dismiss();
            if (message != null) {
                Toast.makeText(Login.this, message, Toast.LENGTH_LONG).show();

            }


        }
    }
}