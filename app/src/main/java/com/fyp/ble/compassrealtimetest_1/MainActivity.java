package com.fyp.ble.compassrealtimetest_1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fyp.ble.compassrealtimetest_1.BLE.BLTE_Device;
import com.fyp.ble.compassrealtimetest_1.BLE.Scanner_BLTE;
import com.fyp.ble.compassrealtimetest_1.ML.Classification;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

    private boolean toScan;
    private int requiredDegree = 0;
    Button button;
    int sensorCount = 0;
    private SensorManager mSensorManager;
    TextToSpeech tts;

    Button buttonproxy;

    //KalmanFilter
    public KalmanFilter kalmanFilter;
    //StepCounter
    List<String[]> list1;
    List<String[]> list2;
    private double xValue;
    private double yValue;
    private double zValue;
    public double accTh = 10.60;
    //private final SensorManager sensorManager;
    //private Sensor accelerometer;
    public String flag = null;
    public long preIndex = 0;
    public double maxPeak = 0;
    public Dictionary<Long> overTH;
    public boolean flag1 = false;
    public int timeTH = 700;
    public int step = 0;
    //public TextView mStepCounterTextView ;




    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    Queue<Integer> queue = new LinkedList<>();

    //BLE

    //BLE
    private HashMap<String, BLTE_Device> mBTDevicesHashMap;
    private ArrayList<BLTE_Device> mBTDevicesArrayList;
    //    ListAdapter_BTLE_Devices adapter;
    private Scanner_BLTE mBTLeScanner;

    public String[] macList2;
    public String[] descriptionList;
    public Map<String, String> destinations;

    public boolean activateButton = false;


    public String locatedInitialMAC;

    boolean isCorrectDir = false;
    boolean isArrived = false;
    public String nextBeaconMAC = "";
    TextView needed;
    TextView current;
    TextView now;


    public final String MAC_LIST = "[{\n" +
            "  \"MAC\":\"D5:B7:DC:69:CA:AE\",\n" +
            "  \"description\":\"N\",\n" +
            "  \"angle\":270,\n" +
            "  \"isStaircase\":0,\n" +
            "  \"stairs\":0\n" +
            "},\n" +
            "{\n" +
            "  \"MAC\":\"D4:32:FC:B5:F0:B5\",\n" +
            "  \"description\":\"H\",\n" +
            "  \"angle\":280,\n" +
            "  \"isStaircase\":0,\n" +
            "  \"stairs\":0\n" +
            "},\n" +
            "{\n" +
            "  \"MAC\":\"E4:E0:0A:AE:FD:E2\",\n" +
            "  \"description\":\"I\",\n" +
            "  \"angle\":290,\n" +
            "  \"isStaircase\":1,\n" +
            "  \"stairs\":5\n" +
            "},\n" +
            "{\n" +
            "  \"MAC\":\"E9:3C:4A:34:13:FB\",\n" +
            "  \"description\":\"P\",\n" +
            "  \"angle\":270,\n" +
            "  \"isStaircase\":0,\n" +
            "  \"stairs\":0\n" +
            "}]";

    public JSONArray array;
    private boolean isTopofStair = false;
    private int numberOfStairs = 0;
    private boolean toStepCount = false;
    private boolean isinProximity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBTDevicesHashMap = new HashMap<>();
        mBTDevicesArrayList = new ArrayList<>();

        buttonproxy = (Button)findViewById(R.id.buttonProxy);

        buttonproxy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isinProximity = true;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBTLeScanner = new Scanner_BLTE(this, 180000, -100);
        }

        toScan = false;
        kalmanFilter = new KalmanFilter(0.008,1);
        needed = (TextView)findViewById(R.id.textViewNeeded);
        current = (TextView)findViewById(R.id.textViewCurrent);
        now = (TextView)findViewById(R.id.textViewNow);

        //Permission check
        if (!checkPermission()) {
            openActivity();
        } else {
            if (checkPermission()) {
                requestPermissionAndContinue();
            } else {
                openActivity();
            }
        }

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                Toast.makeText(this, "The permission to get BLE location data is required", Toast.LENGTH_SHORT).show();
            }else{
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }else{
            Toast.makeText(this, "Location permissions already granted", Toast.LENGTH_SHORT).show();
        }
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            System.out.println("BLE NOT SUPPORTED");
            finish();
        }

        //stepcounter
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        list1 = new ArrayList<>();
        list2 = new ArrayList<>();

        overTH = new Dictionary();

        String[] heading = new String[2];
        heading[0]= "count";
        heading[1] = "Value";

        list1.add(heading);
        list2.add(heading);

        //Configure text to speech
        tts=new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    int result=tts.setLanguage(Locale.US);
                    tts.setSpeechRate((float) 0.92);
//                    tts.speak("Indoor Navigation Application Opened. Tap Anywhere on the screen to begin initializing", TextToSpeech.QUEUE_FLUSH, null);

                    if(result==TextToSpeech.LANG_MISSING_DATA ||
                            result==TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("error", "This Language is not supported");
                    }
                }
                else
                    Log.e("error", "Initilization Failed!");
            }
        });




        button = (Button)findViewById(R.id.button);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        if (toScan){
            button.setText("Scan");
        }


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toScan){
                    toScan = false;
                    button.setText("Scan");
                }else {Button button2;
                    toScan = true;
                    button.setText("Stop");
                }
            }
        });

//        button2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                nodeNumber++;
//            }
//        });

        try {
            array =  new JSONArray(MAC_LIST);
            JSONObject jsonObject = array.getJSONObject(1);
            System.out.println(jsonObject.get("description"));
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("test",e.getMessage());
        }



        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i =0;i<array.length();i++){
                    try {
                        isinProximity = false;
                        JSONObject nextBeacon = array.getJSONObject(i);
                        String description = nextBeacon.getString("description");
                        int isStaircase = nextBeacon.getInt("isStaircase");
                        if (isStaircase==0)
                        {
                            now.setText("Currently Correcting Angle");
                            int angle = nextBeacon.getInt("angle");
                            requiredDegree = angle;
                            needed.setText("Needed Angle = "+Integer.toString(angle));
                            isCorrectDir = false;

                            toScan = true;

                            while (!isCorrectDir){
                                Log.d("test",".");
                            }

                            nextBeaconMAC = nextBeacon.getString("MAC");

//                          BLE Scanning
                            startScan();
                            while (!isinProximity){
                                Thread.sleep(50);
                                //kalman
                                //ML
                                //result = 1,0
                                //break
                            }
                            Log.d("rush","found direction of node "+ i);
                            toScan=false;

                        }
                        else
                        {
                            now.setText("Currently Counting Steps");
                            Log.d("test","inelse");
                            isTopofStair = false;
                            toStepCount = true;
                            numberOfStairs = nextBeacon.getInt("stairs");
                            needed.setText("Needed Steps = "+numberOfStairs);
                            current.setText("Current Steps = ");
                            // step counting
                            while (!isTopofStair){
                                Log.d("test","....");
                            }
                            toStepCount = false;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("test",e.getMessage());
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                        Log.d("test",e.getMessage());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                beep();
            }
        });

        t.start();
        //beginning of navigation
        //start looping the array
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //compass
        if (toScan && event.sensor.getType() == Sensor.TYPE_ORIENTATION){
            float degree = Math.round(event.values[0]);
            int degreeInt = (int)degree;
            Log.d("test", Integer.toString(degreeInt));

            current.setText("Current degree = "+Integer.toString(degreeInt));

            if (sensorCount==150){
                if (this.requiredDegree<degreeInt+5 && this.requiredDegree>degreeInt-5){
                    Log.d("test","correct path");
                    isCorrectDir = true;
                  convertTextToSpeech("go straight");


                }else if (this.requiredDegree<degreeInt-5){
                    Log.d("test","turn left");
                    isCorrectDir = false;
                  convertTextToSpeech("turn left");
//step counting

                }else if (this.requiredDegree>degreeInt+5){
                    Log.d("test","turn right");
                    isCorrectDir = false;
                  convertTextToSpeech("turn right");
                }
                sensorCount=0;
            }else {
                sensorCount++;
            }
        }

        //stepCounting
        if (toStepCount && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
//            Log.d("test","Step Counting");


            //String[] temperory1 = new String[2];
            String[] temperory2 = new String[2];


            temperory2[0] = Long.toString(System.currentTimeMillis());
            temperory2[1] = Float.toString (event.values[2]);


            //boolean add1 = list1.add(temperory1);
            boolean add2 =list2.add(temperory2);


     /*  if (sensorEvent.values[2] > UT) {
        flag1 = true;
        }


       if (sensorEvent.values[2] < LT) {
          flag2 = true;
       }


       if (flag1 && flag2) {
          mStepDetectCounter++;
           flag1 = false ;
           flag2 = false ;
       }*/
            double accZ = event.values[2];
            if (accZ > accTh) {
                overTH.insert(System.currentTimeMillis(),
                        accZ);
                flag1 = true;
            } else {
                if (flag1) {
                    Entry<Long, Double> maxEntry = overTH.findMax();
                    double max = maxEntry.value();
                    long index = maxEntry.key();
                    System.out.println(max);
                    if (index - preIndex > timeTH) {
                        step++;
                        preIndex = index;
                    } else {
                        if (max >= maxPeak) {
                            maxPeak = max;
                        }
                    }
                    flag1=false;
                    overTH=new Dictionary<>();
                }
            }

            current.setText("Current Steps = " +step);

            if (numberOfStairs == step){
                isTopofStair = true;
            }


        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    //BLE Scan Methods
    public void startScan() {
        mBTDevicesHashMap.clear();
        mBTDevicesArrayList.clear();
        mBTLeScanner.start();
    }

    public synchronized void stopScan() {
        mBTLeScanner.stop();
    }

    public synchronized void addDevice(BluetoothDevice device, int rssi) {
        //set a queue to form a moving average
        // add each one and after adding  run through filter, run through ML model
        // add condition to break the infinite while loop

        String address = device.getAddress();

        String ADD = "C4:52:32:5C:31:E7";
        if (!mBTDevicesHashMap.containsKey(address)) {
            BLTE_Device btleDevice = new BLTE_Device(device);
            btleDevice.setRSSI(rssi);
            mBTDevicesHashMap.put(address, btleDevice);
            mBTDevicesArrayList.add(btleDevice);

            queue.poll();
            queue.add(rssi);
            double convertedValue = kalmanFilter.filter(rssi);
            Random n = new Random();
            if (address.equals(ADD)){
                Log.d("rush","found found");
            }

//            isinProximity = n.nextBoolean();

        } else {
            queue.poll();
            queue.add(rssi);
            double convertedValue = kalmanFilter.filter(rssi);
            Random n = new Random();
            if (address.equals(ADD)){
                Log.d("rush","found found");
            }

//            isinProximity = n.nextBoolean();

            mBTDevicesHashMap.get(address).setRSSI(rssi);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        tts.shutdown();
    }

    private void convertTextToSpeech(String s) {
        tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void beep(){
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP,500);
    }

    private void requestPermissionAndContinue() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("permission_necessary");
                alertBuilder.setMessage("storage_permission_is_encessary_to_wrote_event");
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE
                                , READ_EXTERNAL_STORAGE}, 200);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
                Log.e("", "permission denied, show dialog");
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE}, 200);
            }
        } else {
            openActivity();
        }
    }

    private boolean checkPermission() {

        return ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ;
    }

    private void openActivity() {
        //add your further process after giving permission or to download images from remote server.
    }

    public class Dictionary<Long> {
        private List<Entry<Long, Double>> set;

        public Dictionary() {
            this.set = new LinkedList<Entry<Long, Double>>();
        }

        public Entry<Long, Double> find(Long key) {
            for (Entry<Long, Double> entry : set) {
                if (entry.key() == key) {
                    return entry;
                }
            }
            return null;
        }

        public Entry<Long, Double> findMax() {
            Entry<Long, Double> maxEntry = null;
            for (Entry<Long, Double> entry : set) {
                if (maxEntry == null) {
                    maxEntry = entry;
                }
                if (maxEntry.value() < entry.value()) {
                    maxEntry = entry;
                }
            }
            return maxEntry;
        }

        public void insert(Long key, Double value) {
            set.add(new Entry<Long, Double>(key, value));

        }

        public void remove(Long key) {
            for (Entry<Long, Double> entry : set) {
                if (entry.key() == key) {
                    set.remove(entry);
                }
            }
        }

    }


    public class Entry<K, V> {

        private K key;
        private V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K key() {
            return key;
        }

        public V value() {
            return value;
        }
    }
}
