package lagsit.smsserver;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Query;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {
    public static final String FIREBASE_URL = "https://moblert-b1226.firebaseio.com/";
    Button btnSendSMS;
    TextView txtDesc;

    static List<Subscription>  subscriptionItems = new ArrayList<Subscription>();
    Boolean isLoading =true;
    static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(this);
        context =getApplicationContext();

        isLoading =true;
        getSubscriptionList();
        getLatestFeed();

        txtDesc = (TextView) findViewById(R.id.txtDesc);
        btnSendSMS = (Button) findViewById(R.id.btnSendSMS);

        btnSendSMS.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
            sendSMS("639487143620", "test");

            }
        });

    }
/*
    //---sends an SMS message to another device---
    private void sendSMS(String phoneNumber, String message)
    {
        PendingIntent pi = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, pi, null);

    }
*/
    private static void sendReply(String phoneNumber, String message)
    {
        PendingIntent pi = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, pi, null);

    }
    public static void validateCode(String phoneNumber,String code){
        for (Subscription s : subscriptionItems) {
            if (s.getCode().equals(code.toLowerCase())){
                Firebase ref = new Firebase(FIREBASE_URL);
                ref.child("subscriber/").push().setValue(new Subscriber(s.getCode(),phoneNumber,s.getEmail()));
                sendReply(phoneNumber,"Your subscription on "+s.getName()+" has been activated.");
            }
            String stopKeyword =s.getCode()+"stop";
            if (stopKeyword.equals(code.toLowerCase())){
                System.out.println(stopKeyword);
                unSubscribe(phoneNumber,s.getCode());
                sendReply(phoneNumber,"Your subscription on "+s.getName()+" has been deactivated. Thank you for subscribing ");
            }
        }
    }
    private static void unSubscribe(final String PhoneNo, String code){
        Firebase ref = new Firebase(FIREBASE_URL);
        Query subsRef = ref.child("subscriber").orderByChild("code").equalTo(code);
        subsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChildren()) {
                    for (DataSnapshot codeSnapshot : snapshot.getChildren()) {
                        String number = codeSnapshot.child("number").getValue().toString();
                        if (PhoneNo.equals(number)) {
                            codeSnapshot.getRef().removeValue();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(FirebaseError arg0) {
            }
        });
    }
    public void getLatestFeed() {
              Firebase ref = new Firebase(FIREBASE_URL);
              Query subsRef = ref.child("feed")
                      .orderByChild("edate")
                      .startAt((new Date().getTime() - (1000 * 60 * 60 * 24)))
                      .limitToLast(1);

              subsRef.addChildEventListener(new ChildEventListener() {
                  @Override
                  public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                      System.out.println("onChildAdded isLoading "+isLoading);
                      System.out.println("DataSnapshot  "+dataSnapshot);
                      if (!isLoading) {
                          isLoading =false;
                          String msgType =dataSnapshot.child("msgtype").getValue().toString();
                          String email = dataSnapshot.child("email").getValue().toString();
                          String msg;
                          if (msgType.equals("fa")){
                              String time_str =dataSnapshot.child("time_str").getValue().toString();
                              String level=dataSnapshot.child("level").getValue().toString();
                              String name =dataSnapshot.child("name").getValue().toString();
                              msg = "Attention "+name+" "+level+" "+time_str;
                          }else{
                              String gradeLevel =dataSnapshot.child("level").getValue().toString();
                              String sector = dataSnapshot.child("sector").getValue().toString();
                              String place =dataSnapshot.child("place").getValue().toString();
                              String name =dataSnapshot.child("name").getValue().toString();
                              String edate =dataSnapshot.child("edate").getValue().toString();
                              String url = gradeLevel+" Level "+sector+" Sector at "+place;
                              edate = getFormattedDate(MainActivity.this,Long.valueOf(edate));
                              msg ="WALANG PASOK "+edate +" "+url+"\nfrom:"+name+" "+email;
                          }

                          checkIfSubscriberExists(email,msg);
                      }
                      isLoading =false;
                  }

                  @Override
                  public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                  }

                  @Override
                  public void onChildRemoved(DataSnapshot dataSnapshot) {

                  }

                  @Override
                  public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                  }

                  @Override
                  public void onCancelled(FirebaseError arg0) {
                  }
              });


    }
    public void checkIfSubscriberExists(final String email,final String msg) {
        Firebase ref = new Firebase(FIREBASE_URL);
        Query subsRef = ref.child("subscriber").orderByChild("subscribeTo")
                           .startAt(email).endAt(email);

        subsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                List<Subscriber> list = new ArrayList();
                if (snapshot.hasChildren()) {
                    for (DataSnapshot masterSnapshot : snapshot.getChildren()) {
                        String num = masterSnapshot.child("number").getValue().toString();
                        String subscribeTo = masterSnapshot.child("subscribeTo").getValue().toString();
                        list.add(new Subscriber(num,subscribeTo));
                    }
                    //Removing Duplicates;
                    Set<Subscriber> subscribersSet= new HashSet<Subscriber>();
                    subscribersSet.addAll(list);

                    List<Subscriber> subscriberslist = new ArrayList();

                    subscriberslist = new ArrayList<Subscriber>();
                    subscriberslist.addAll(subscribersSet);


                    for (int i = 0; i < subscriberslist.size(); i++) {
                         sendSMS(subscriberslist.get(i).getNumber(),msg);

                    }

                }
            }
            @Override
            public void onCancelled(FirebaseError arg0) {
            }
        });
    }
    public void addNumber(String keyfield, final String phoneNo,final String code) {
        Firebase fieldRef = new Firebase(FIREBASE_URL + keyfield);
        fieldRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(final MutableData currentData) {
                if (currentData.getValue() == null) {
                    currentData.setValue(1);
                } else {
                    currentData.setValue((Long) currentData.getValue() + 1);
                }

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot currentData) {
                if (firebaseError != null) {
                    System.out.println("Firebase counter increment failed.");
                } else {
                    String id =currentData.getValue().toString();
                    Firebase ref = new Firebase(FIREBASE_URL);
                    ref.child("subscriber/"+id).setValue(new Subscriber(phoneNo,code));//custom id
                   // ref.child("subscriber/").push().setValue(new Subscriber(id,phoneNo));//push
                }
            }
        });
    }

    private void getSubscriptionList(){ //before adding the subscriber
        Firebase ref = new Firebase(FIREBASE_URL);
        Query subsRef = ref.child("subscription").orderByChild("code");
        subsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
            //    System.out.println(snapshot);
                subscriptionItems.clear();
                if (snapshot.hasChildren()) {
                    String txt="Subscription Code \n";
                    for (DataSnapshot masterSnapshot : snapshot.getChildren()) {
                        String code = masterSnapshot.child("code").getValue().toString();
                        String name = masterSnapshot.child("name").getValue().toString();
                        String email =masterSnapshot.child("email").getValue().toString();
                        txt =txt+code+' '+name+"\n";
                           subscriptionItems.add(new Subscription(code,name, email));
                    }
                    txtDesc.setText(txt);
                }
            }
            @Override
            public void onCancelled(FirebaseError arg0) {
            }
        });
    }


    public String getFormattedDate(Context context, long smsTimeInMilis) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(smsTimeInMilis);

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "h:mm aa";
        final String dateTimeFormatString = "EEEE, MMMM d, h:mm aa";
        final String longformat ="MMM dd, yyyy";
        final long HOURS = 60 * 60 * 60;
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE) ) {
            return "Today " + DateFormat.format(longformat, smsTime);
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1  ){
            return "Yesterday " ;
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            return DateFormat.format(longformat, smsTime).toString();
        } else {
            return DateFormat.format("MMMM dd yyyy, h:mm aa", smsTime).toString();
        }
    }


    //---sends an SMS message to another device---
    private void sendSMS(String phoneNumber, String message)
    {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }


}
