/**
 * @file BluetoothReceiverList.java
 *
 * Activity that displays "Bluetooth Receivers" screen.
 * Loads any paired Bluetooth devices. Also scans for any new Bluetooth 
 * devices.Displays spinning progress bar, while the external Bluetooth
 * receiver is connected and identified.Initiates a background process 
 * to write and read data from the external Bluetooth  receiver, to obtain
 * GPS and EGNOS positions.
 *
 * Rev: 3.0.0
 * 
 * Author: DKE Aerospace Germany GmbH
 * 
 * Copyright 2012 DKE Aerospace Germany GmbH
 * 
 * Licensed under the EUPL, Version 1.1 only (the "Licence");
 * You may not use this work except in compliance with the 
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 * 
 * @mainpage The EGNOS SDK
 * <b>EGNOS</b> is the European satellite-based augmentation system (SBAS),
 * an infrastructure that consists of three geostationary satellites and a network 
 * of ground stations. The system improves, in Europe, the accuracy of the open 
 * public service offered by the Global Positioning System (GPS) by providing 
 * corrections of GPS satellites clocks and orbits, and of the error caused by 
 * the ionosphere. 
 * 
 * <b>SISNeT</b> is a server that provides over the Internet the same EGNOS 
 * corrections as if they would have been received from the satellites. 
 * 
 * The <b>EGNOS SDK</b> has been designed to allow application developers to 
 * take advantage of the EGNOS benefits, and to use them for the software they 
 * develop for mobile devices. The open-source library in the EGNOS SDK offers the 
 * possibility to include EGNOS corrections for a more accurate position, as well
 * as "Integrity". Integrity gives an estimation of the confidence in the calculation
 * of the position provided by the system along with alerts in real time (less than 
 * six seconds) of any shortcomings in the reliability of GPS positioning signals. 
 *  
 * The <b>Demonstration Application</b> shows the main features of the EGNOS SDK at
 * work, providing application developers with examples on how the EGNOS library can
 * be used and showing the benefits of the EGNOS corrections on positioning.
 * 
 * The <b>EGNOS SDK Interface</b> provides the necessary functionalities for interfacing 
 * the GUI with the software receiver. 
 * 
 * For additional information on the EGNOS SDK please visit
 *  <a>http://www.egnos-portal.eu/</a>
 */
package com.ec.egnossdk;

import java.util.Set;

import com.ec.R;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity that loads the "Bluetooth Receivers" screen.
 * Loads any paired Bluetooth devices. Also scans for any new Bluetooth 
 * devices.Displays spinning progress bar, while the external Bluetooth
 * receiver is connected and identified.Initiates a background process 
 * to write and read data from the external Bluetooth  receiver, to obtain
 * GPS and EGNOS positions.
 */
public class BluetoothReceiverList extends Activity {
  BluetoothAdapter bluetoothAdapter;
  ArrayAdapter<String> newReceiverAdapter;
  ArrayAdapter<String> pairedReceiverAdapter;
  String deviceName;
  String deviceAddress;
  public static final String RECEIVER_ADDRESS = "deviceaddress";
  private ProgressBarThread progressBarThread;
  private ProgressDialog progressBarDialog;
  private BluetoothConnect btConnect;
  private String bluetoothAddress;
  public static final int CONNECT_RECEIVER = 1;
  public static final int IDENTIFY_RECEIVER = 2;
  public static final int uBLOX_MESSAGE = 0;
  public static final String TOAST = "toast";
  private static final String TAG = "EGNOS-SDK";
  LogFiles log;

  /**
   * onCreate function 
   * 
   * Called on start of activity, displays scan button and a
   * any paired bluetooth devices.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.bluetoothreceiver);
    
    log = new LogFiles();
    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    pairedReceiverAdapter = new ArrayAdapter<String>(this,
        R.layout.bluetoothreceiver_name);
    newReceiverAdapter = new ArrayAdapter<String>(this,
        R.layout.bluetoothreceiver_name);

    // list view for paired devices.
    ListView pairedReceiverList = (ListView) this
        .findViewById(R.id.pairedReceiver_list);
    pairedReceiverList.setAdapter(pairedReceiverAdapter);
    pairedReceiverList.setOnItemClickListener(receiverClickListener);

    // list view for new devices.
    ListView newReceiverList = (ListView) this
        .findViewById(R.id.newReceiver_list);
    newReceiverList.setAdapter(newReceiverAdapter);
    newReceiverList.setOnItemClickListener(receiverClickListener);

    // register for a broadcast receiver to detect if any new devices are found.
    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    registerReceiver(mReceiver, filter);

    Set<BluetoothDevice> pairedReceivers = bluetoothAdapter.getBondedDevices();
    // Get any paired Bluetooth devices
    if (pairedReceivers.size() > 0) {
      findViewById(R.id.pairedReceiver_text).setVisibility(View.VISIBLE);
      for (BluetoothDevice device : pairedReceivers) {
        deviceName = device.getName();
        if (deviceName.equals("") || deviceName.equals(null))
          deviceName = "Unknown";
        deviceAddress = device.getAddress();
        pairedReceiverAdapter.add(deviceName + ":" + deviceAddress);
      }
    }

    Button scanButton = (Button) this.findViewById(R.id.scan_Button);
    // on click of Scan button.
    scanButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        scanForReceivers();
      }
    });
  }

  /**
   * scanForReceivers function 
   * Starts discovering any new bluetooth devices when scan button is clicked.
   */
  protected void scanForReceivers() {
    findViewById(R.id.newReceiver_text).setVisibility(View.VISIBLE);
    if (bluetoothAdapter.isEnabled()) {
      if (bluetoothAdapter.isDiscovering())
        bluetoothAdapter.cancelDiscovery();
      bluetoothAdapter.startDiscovery();
    } else {
      finish();
      Toast.makeText(this, "Turn on Bluetooth", Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * mReceiver BroadCastReceiver 
   * 
   * To indicate when any new bluetooth device is found in the near vicinity.
   * @return deviceName+deviceAddress          string to display in listView
   *                                           newReceiverAdapter.
   */
  protected final BroadcastReceiver mReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (BluetoothDevice.ACTION_FOUND.equals(action)) {
        try {
          BluetoothDevice device = intent
              .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
          if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            deviceName = device.getName();
            if (deviceName.equals("") || deviceName.equals(null))
              deviceName = "Unknown";

            deviceAddress = device.getAddress();

            if (checkDuplicate(deviceAddress) == false)
              newReceiverAdapter.add(deviceName + ":" + deviceAddress);
          }
        } catch (NullPointerException e) {
          Log.e(TAG, "Receiver | mReceiver (" + e.getMessage() + ")");
        }
      }
    }
  };

  /**
   * checkDuplicate function 
   * 
   * Check for any duplicate bluetooth devices before adding to the newly
   * discovered list of Bluetooth devices.
   * @param   address          address of bluetooth device.
   * @return  exist            TRUE if device address already exists 
   *                           in new devices list,otherwise FALSE.
   */
  private boolean checkDuplicate(String address) {
    int count = 0;
    boolean exist;
    for (int i = 0; i < newReceiverAdapter.getCount(); i++) {
      if (newReceiverAdapter.getItem(i).contains(address))
        count++;
    }
    if (count > 0)
      exist = true;
    else
      exist = false;
    return exist;
  }

  /**
   * receiverClickListener onItemClickListener. 
   * 
   * Listener when an item is clicked from paired device list
   * or new device list.
   * @return address           address of the Bluetooth device.
   */
  private OnItemClickListener receiverClickListener = new OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> av, View view, int id, long position) {
      bluetoothAdapter.cancelDiscovery();
      String info = ((TextView) view).getText().toString();
      bluetoothAddress = info.substring(info.length() - 17);
      if (GlobalState.getSocket() == null)
        showDialog(0);
      else {
        if (GlobalState.getSocket().getRemoteDevice().getAddress()
            .equals(bluetoothAddress))
          Toast.makeText(getBaseContext(), R.string.receiverConnectionExists,
              Toast.LENGTH_SHORT).show();
        else
          Toast.makeText(getBaseContext(),
              R.string.anotherReceiverConnectionExists, Toast.LENGTH_SHORT)
              .show();
      }
    }
  };

  /**
   * onCreateDialog Dialog. 
   * 
   * Creates a progress bar dialog based on the id.
   * Called when showDialog is called.
   * @param  id            id of the progress bar to be created.
   * @return Dialog        the progress bar dialog created.
   */
  protected Dialog onCreateDialog(int id) {
    switch (id) {
    case 0:// Spinner
      progressBarDialog = new ProgressDialog(this);
      progressBarDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
      progressBarDialog.setCancelable(false);
      progressBarDialog.setMessage(getBaseContext().getString(
          R.string.connectingToReceiver));
      progressBarThread = new ProgressBarThread(progressBarhandler);
      progressBarThread.start();
      return progressBarDialog;
    default:
      return null;
    }
  }

  /**
   * progressBarhandler Handler. 
   * 
   * Handler to take action on different messages.
   * Display or remove progress bar dialog while receiver is connecting to
   * device. Display or remove progress bar dialog while receiver is being
   * identified. Display Toast messages from a non UI thread in this case from
   * uBlox class.
   */
  final Handler progressBarhandler = new Handler() {
    /**
     * handleMessage Handler. 
     * 
     * Subclass to receive messages from a non UI thread.
     * @param message          The message received.
     */
    public void handleMessage(Message message) {
      switch (message.what) {
      case CONNECT_RECEIVER:
        boolean connectReceiver = message.getData().getBoolean(
            "ConnectReceiver");
        if (connectReceiver) {
          progressBarDialog.setMessage(getBaseContext().getString(
              R.string.identifyingReceiver));
          progressBarThread.setState(ProgressBarThread.IDENTIFY);
          connectReceiver = false;
        } else {
          Toast.makeText(getBaseContext(), R.string.unableToConnect,
              Toast.LENGTH_SHORT).show();
          removeDialog(0);
          progressBarThread.setState(ProgressBarThread.DONE);
          connectReceiver = false;
        }
        break;
      case IDENTIFY_RECEIVER:
        boolean identifyReceiver = message.getData().getBoolean(
            "IdentifyReceiver");
        if (identifyReceiver) {
          removeDialog(0);
          progressBarThread.setState(ProgressBarThread.DONE);
          Toast.makeText(getBaseContext(), R.string.uBloxReceiverconnected,
              Toast.LENGTH_SHORT).show();
          new PositionComputation().execute("");
          finish();
          identifyReceiver = false;
        } else {
          Toast.makeText(getBaseContext(), R.string.receiverUnIdentified,
              Toast.LENGTH_SHORT).show();
          removeDialog(0);
          progressBarThread.setState(ProgressBarThread.DONE);
          identifyReceiver = false;
        }
        break;
      case uBLOX_MESSAGE:
        Toast.makeText(getApplicationContext(),
            message.getData().getInt(TOAST), Toast.LENGTH_SHORT).show();
        break;
      }
    }
  };

  /**
   * ProgressBarThread Thread. 
   * 
   * Thread to connect to a Bluetooth receiver and
   * identify the connected Bluetooth receiver.
   */
  private class ProgressBarThread extends Thread {
    final static int DONE = 0;
    final static int RUNNING = 1;
    final static int IDENTIFY = 2;
    int mState;
    Handler mHandler;

    ProgressBarThread(Handler h) {
      mHandler = h;
      btConnect = new BluetoothConnect(getBaseContext(), bluetoothAddress);
      mState = RUNNING;
    }

    @Override
    public void run() {
      if (mState == RUNNING) {
        btConnect.connect();
        boolean connectReceiver = BluetoothConnect.RECEIVER_CONNECTED;
        Message msg = mHandler.obtainMessage(CONNECT_RECEIVER);
        Bundle b = new Bundle();
        b.putBoolean("ConnectReceiver", connectReceiver);
        msg.setData(b);
        mHandler.sendMessage(msg);
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Log.e(TAG, "Receiver | ThrdSleep (" + e.getMessage() + ")");
        e.printStackTrace();
      }
      if (mState == IDENTIFY) {
        btConnect.identifyReceiver();
        boolean identifyReceiver = BluetoothConnect.IDENTIFY_RECEIVER;
        Message msg = mHandler.obtainMessage(IDENTIFY_RECEIVER);
        Bundle b = new Bundle();
        b.putBoolean("IdentifyReceiver", identifyReceiver);
        msg.setData(b);
        mHandler.sendMessage(msg);
      }
    }

    // Set current state of thread.
    public void setState(int state) {
      mState = state;
    }
  }

  /**
   * PositionComputation class 
   * 
   * Initiates background process to write and read data from receiver. 
   * Parse the data and send data to SW Receiver to get
   * GPS and EGNOS coordinates.
   **/
  public class PositionComputation extends
      AsyncTask<String, Void, Integer> {

    /**
     * doInBackground function 
     * 
     * Initiates background process to write and read data to the receiver. 
     * Parse the data and send data to SW Receiver to get GPS and 
     * EGNOS coordinates.
     * @param  params           parameters of this task.
     * @return 1                indicating task was handled.
     **/
    @Override
    protected Integer doInBackground(final String... params) {
     try{
      uBlox ub = new uBlox(progressBarhandler, getBaseContext());
      ub.init();
     }catch(Exception e){
 		log.logError("Settings - unable to load EGNOS SDK core");	
 		Log.e(TAG, "EDAM | unable to load EGNOS SDK core " + e);
 	}
      return 1;
    }
  }

  /**
   * closePositionComputation function 
   * 
   * Close the PositionComputation  background process.
   **/
  public void closePositionComputation() {
    new PositionComputation().cancel(true);
  }

  /**
   * onDestroy function 
   * 
   * Cancels any bluetooth device discovery and unregister
   * any broadcast receiver when activity is destroyed.
   */
  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (bluetoothAdapter != null) {
      bluetoothAdapter.cancelDiscovery();
    }
    this.unregisterReceiver(mReceiver);
  }
}
