package com.skyrealm.brockyy.findmypeepsapp;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Register extends Activity implements OnClickListener {
    private EditText userreg, passreg, emailreg, verpasser, phone, code, carrier;
    GPSTracker gps = new GPSTracker(this);
    private Button bRegister, bverif;
    TabLayout setupLogin = new TabLayout();
    // Progress Dialog
    private ProgressDialog pDialog;
    int verif = 0;
    AlertDialog alert2;
    // JSON parser class
    JSONParser jsonParser = new JSONParser();
    private static final String LOGIN_URL = "http://skyrealmstudio.com/cgi-bin/newregister.py";
    private static final String VERIFY_URL = "http://skyrealmstudio.com/cgi-bin/verifyaccount.py";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    String user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        //Assign variables
        userreg = (EditText)findViewById(R.id.user);
        passreg = (EditText)findViewById(R.id.pass);
        emailreg = (EditText)findViewById(R.id.email);
        verpasser = (EditText) findViewById(R.id.verpass);
        phone = (EditText) findViewById(R.id.phone);
        carrier = (EditText) findViewById(R.id.carrier2);
        bRegister = (Button)findViewById(R.id.registerbutton);
        bRegister.setOnClickListener(this);
        carrier.setAlpha(0.0f);

        String pass = passreg.getText().toString();
        String Verify = verpasser.getText().toString();
        verpasser.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                if(passreg.getText().toString().equals( verpasser.getText().toString())) {
                    verpasser.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
                } else {
                    verpasser.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.registerbutton:
                if(gps.haveNetworkConnection()) {
                    new AttemptRegister().execute();
                }else{
                    gps.LoginAlert();
                }
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {

        Intent ii = new Intent(Register.this,Login.class);
        finish();
        startActivity(ii);
    }

    class AttemptRegister extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         * */
        boolean failure = false;

        //Get the mobile Carrier Name
        TelephonyManager manager = (TelephonyManager)
                getSystemService(Context.TELEPHONY_SERVICE);
        String carrierName = manager.getNetworkOperatorName();



        String p = passreg.getText().toString();
        String u = userreg.getText().toString();
        String v = verpasser.getText().toString();
        String e = emailreg.getText().toString();
        String ph = phone.getText().toString();
        String c = carrierName;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Register.this);
            pDialog.setMessage("Registering...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // here Check for success tag



            int success;

                String pass = p;
                user = u;
                String very = v;
                String email= e;
                String Phone = ph;
                String Carrier = c;


            if (pass.matches("") || user.matches("") || email.matches("") || very.matches("") || Phone.matches("") || Carrier.matches("")) {
                return "One or more fields are empty!";
            }else{

                if(p.equals(v)){
                try {

                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("user", user));
                    params.add(new BasicNameValuePair("pass", pass));
                    params.add(new BasicNameValuePair("email", email));
                    params.add(new BasicNameValuePair("phone", Phone));
                    params.add(new BasicNameValuePair("carry", Carrier));



                    JSONObject json = jsonParser.makeHttpRequest(
                            LOGIN_URL, "POST", params);

                    // checking  log for json response

                    // success tag for json
                    success = json.getInt(TAG_SUCCESS);
                    if (success == 1) {
                        verif = success;

                        //Intent ii = new Intent(Register.this, Login.class);
                        //finish();
                        // this finish() method is used to tell android os that we are done with current //activity now! Moving to other activity
                        //startActivity(ii);
                        return json.getString(TAG_MESSAGE);
                    } else {

                        return json.getString(TAG_MESSAGE);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                    return "Passwords must match!";
            }
            }
            return null;
        }
        /**
         * Once the background process is done we need to  Dismiss the progress dialog asap
         * **/
        protected void onPostExecute(String message) {

            pDialog.dismiss();
            if (message != null){

                if (verif == 1){
                    LayoutInflater layoutInflater = LayoutInflater.from(Register.this);
                    AlertDialog.Builder Built = new AlertDialog.Builder(Register.this);
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
                    Toast.makeText(Register.this, message, Toast.LENGTH_LONG).show();
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
            pDialog = new ProgressDialog(Register.this);
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
            String verify = coder;
            boolean failure = false;


            try {


                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", user));
                params.add(new BasicNameValuePair("verify", verify));


                JSONObject json = jsonParser.makeHttpRequest(
                        VERIFY_URL, "POST", params);

                // set up tags for json
                success = json.getInt(TAG_SUCCESS);


                if (success == 1)
                {
                    alert2.cancel();
                    Intent ii = new Intent(Register.this, Login.class);
                    finish();
                    startActivity(ii);
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
                    Toast.makeText(Register.this, "Verification timed out. Try again later.", Toast.LENGTH_LONG).show();
                }
            }, time);
        }

        protected void onPostExecute(String message) {
            long timeout = 8000;
            pDialog.dismiss();
            if (message != null) {
                Toast.makeText(Register.this, message, Toast.LENGTH_LONG).show();

            }


        }
    }
}

