package com.skyrealm.brockyy.findmypeepsapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

/**
 * Created by RockyFish on 10/27/15.
 */
public class Status_Fragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    String user;
    String Number;
    private static Timer timer;
    int seconds;
    int newSeconds;
    private Double latitude;
    private Double longitude;
    private String lastUpdated;
    android.os.Handler mainHandler;
    GPSTracker gps;
    SwipeRefreshLayout swipeLayout;
    ListView notificationList;
    ArrayList<String> notifications = new ArrayList<String>();
    ArrayList<Bitmap> userIcons = new ArrayList<Bitmap>();
    ArrayList<String> usernameArrayList = new ArrayList<String>();
    ArrayList<String> dateArrayList = new ArrayList<String>();
    ArrayList<String> timeArrayList = new ArrayList<String>();
    AdView mAdView;
    getNotifications notifcations;
    ListView notificationListview;
    CustomAdapter adapter;
    SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.status_tab, container, false);
        sharedPreferences = getActivity().getSharedPreferences("com.skyrealm.brocky.findmypeepsapp", Context.MODE_PRIVATE);
        mAdView = (AdView)  v.findViewById(R.id.adView);
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        notificationListview = (ListView) v.findViewById(R.id.notificationsListView);



        TabLayout activity = (TabLayout) getActivity();
        user = activity.get_username();
        Number = activity.get_number();

        int size = sharedPreferences.getInt(user.toLowerCase() + "Size", 0);
        if(size > 0)
        {
            for(int i = 0; i < size; i++)
            {
                String username = sharedPreferences.getString(user.toLowerCase() + String.valueOf(i) + "Username", null);
                usernameArrayList.add(username);
                String date = sharedPreferences.getString(user.toLowerCase() + String.valueOf(i) + "Date", null);
                dateArrayList.add(date);
                String time = sharedPreferences.getString(user.toLowerCase() + String.valueOf(i) + "Time", null);
                timeArrayList.add(time);
                String notification = sharedPreferences.getString(user.toLowerCase() + String.valueOf(i) + "Notification", null);
                notifications.add(notification);
                String userIcon = sharedPreferences.getString(user.toLowerCase() + String.valueOf(i) + "UserIcon", null);
                if(userIcon != null) {
                    byte[] encodeByte = Base64.decode(userIcon, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,
                            encodeByte.length);
                    if (bitmap != null) {
                        userIcons.add(bitmap);
                    }
                }
            }
            if(userIcons.size() > 0) {
                adapter = new CustomAdapter(getActivity(), notifications, usernameArrayList, dateArrayList, timeArrayList, userIcons, user);
            } else {
                adapter = new CustomAdapter(getActivity(), notifications, usernameArrayList, dateArrayList, timeArrayList, user);

            }
            notificationListview.setAdapter(adapter);
        }

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);


        //set a swipe refresh layout
        swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


       notifcations = new getNotifications();
        notifcations.execute();


        //only allows the swipe if it is at the top of the list
        notificationListview.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                boolean enable = false;
                if (notificationListview != null && notificationListview.getChildCount() > 0) {
                    // check if the first item of the list is visible
                    boolean firstItemVisible = notificationListview.getFirstVisiblePosition() == 0;
                    // check if the top of the first item is visible
                    boolean topOfFirstItemVisible = notificationListview.getChildAt(0).getTop() == 0;
                    // enabling or disabling the refresh layout
                    enable = firstItemVisible && topOfFirstItemVisible;
                }
                swipeLayout.setEnabled(enable);
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.destroy();
            mAdView.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        super.onResume();
        if(notifcations.getStatus() == AsyncTask.Status.RUNNING)
        {
            swipeLayout.setRefreshing(false);
            notifcations.cancel(true);
        }
        if (mAdView != null) {
            mAdView.destroy();
            mAdView.pause();

        }

        if(adapter != null)
        {
            if(adapter.getImageId() != null)
            {
                for(int i = 0; i < adapter.getImageId().size(); i++)
                {
                    adapter.getImageId().get(i).cancel(true);
                }
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        super.onResume();
        if(adapter != null)
        {
            if(adapter.getImageId() != null)
            {
                for(int i = 0; i < adapter.getImageId().size(); i++)
                {
                    adapter.getImageId().get(i).cancel(true);
                }
            }
        }
        if(notifcations.getStatus() == AsyncTask.Status.RUNNING)
        {
            notifcations.cancel(true);
        }
        if (mAdView != null) {
            mAdView.destroy();
        }
        swipeLayout.setRefreshing(false);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mAdView != null) {
            mAdView.destroy();
        }
    }

    @Override
    public void onRefresh() {
        notifications.clear();
        notificationListview.setAdapter(null);
        notifcations = new getNotifications();
        notifcations.execute();
    }

    class getNotifications extends AsyncTask<String, Void, Void> {
        protected void onPreExecute() {
            super.onPreExecute();
            notifications = new ArrayList<>();
            userIcons = new ArrayList<Bitmap>();
            usernameArrayList = new ArrayList<String>();
            dateArrayList = new ArrayList<String>();
            timeArrayList = new ArrayList<String>();
            swipeLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeLayout.setRefreshing(true);
                }
            });
        }

        public Bitmap getCroppedBitmap(Bitmap bitmap) {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                    bitmap.getWidth() / 2, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
            //return _bmp;
            return output;
        }

        @Override
        protected Void doInBackground(String... strings) {
            HttpResponse response;
            String responseStr = null;
            String notification;
            String usernames;
            JSONObject obj = null;
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://www.skyrealmstudio.com/cgi-bin/GetNotifications.py");
            JSONArray json = null;


            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("username", user));
                nameValuePairs.add(new BasicNameValuePair("Number", Number));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                response = httpclient.execute(httppost);
                responseStr = EntityUtils.toString(response.getEntity());

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
            System.out.println(responseStr);
            try {
                json = new JSONArray(responseStr);
                if (json.length() > 0) {
                    for (int counter = 0; counter < json.length(); counter++)
                        try {
                            notification = json.getJSONObject(counter).getString("notification");
                            usernames = json.getJSONObject(counter).getString("username");
                            char secondNum = notification.charAt(11);
                            char firstNum = notification.charAt(12);
                            String numberDone = String.valueOf(secondNum) + String.valueOf(firstNum);
                            String finalText;
                            String date;
                            String time;
                            if (Integer.parseInt(numberDone) > 12) {
                                int numberTesting = Integer.parseInt(numberDone);
                                int finalNum;
                                finalNum = numberTesting - 12;
                                if (finalNum >= 10) {
                                    finalText = String.valueOf(finalNum);
                                } else {
                                    finalText = "0" + String.valueOf(finalNum);
                                }

                                String finalNotification;
                                finalNotification = notification.substring(0, 11) + finalText + notification.substring(13);
                                notification = finalNotification;
                            }
                            SimpleDateFormat todaysDateC = new SimpleDateFormat("MM-dd");
                            date = notification.substring(5, 10);
                            time = notification.substring(11, 22);
                            String todaysDate = todaysDateC.format(new Date());
                            usernameArrayList.add(usernames);
                            int length = usernames.length();
                            notification = notification.substring(24 + length + 1);
                            notifications.add(notification);
                            dateArrayList.add(date);
                            String tempTime;
                            tempTime = time.substring(0, 1) + time.substring(1, 2);
                            if (Integer.parseInt(tempTime) < 10) {
                                time = time.substring(1, 5) + time.substring(8);
                            } else {
                                time = time.substring(0, 5) + time.substring(8);
                            }
                            timeArrayList.add(time);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {
            adapter = new CustomAdapter(getActivity(), notifications, usernameArrayList, dateArrayList, timeArrayList, user);
            notificationListview.setAdapter(adapter);

            if(notifications.size() > 0)
            {
                sharedPreferences.edit().putInt(user.toLowerCase() + "Size", notifications.size()).apply();

            }
            for(int i = 0; i < notifications.size(); i++)
            {
                sharedPreferences.edit().putString(user.toLowerCase() + String.valueOf(i) + "Username", usernameArrayList.get(i).toLowerCase()).apply();
                sharedPreferences.edit().putString(user.toLowerCase() + String.valueOf(i) + "Date", dateArrayList.get(i)).apply();
                sharedPreferences.edit().putString(user.toLowerCase() + String.valueOf(i) + "Time", timeArrayList.get(i)).apply();
                sharedPreferences.edit().putString(user.toLowerCase() + String.valueOf(i) + "Notification", notifications.get(i)).apply();
            }
            swipeLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeLayout.setRefreshing(false);
                }
            });
        }

    }
}
