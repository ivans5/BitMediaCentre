package com.bitmediacentre.bitmediacentre.ui.main;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
//import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bitmediacentre.bitmediacentre.R;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;



public class MouseFragment extends Fragment {
    private class MyEvent
    {
        public float x,y;
        long timestamp;

        public MyEvent(float aX, float aY, long aTimestamp)
        {
            this.x = aX;
            this.y = aY;
            this.timestamp = aTimestamp;
        }
    }

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;

    private MyEvent lastActionDown;
    private float lastX, lastY;  //used for relative motion events
    private boolean isDragging = false;  //note this will always be set while cursor is in motion - not sure if needed?

    // Accumulated dx and dy values
    float accumulatedDx = 0;
    float accumulatedDy = 0;
    long lastSendTime = System.currentTimeMillis();

    //XXX NOT USED - private static final float MOVE_THRESHOLD = 2.0f; // Minimum movement before sending
    final long SEND_THRESHOLD = 30; // Send every 100ms *AND NOT* after a threshold of movement
    final long CLICK_THRESHOLD = 10; //10 units distance - minimal delta to trigger click event


    public static MouseFragment newInstance(int index) {
        MouseFragment fragment = new MouseFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment1_main, container, false);

        View clickableArea = root.findViewById(R.id.clickableArea);

        final Context finalContext = root.getContext();

        clickableArea.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //From ChatGPT:
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            lastActionDown = new MyEvent(event.getX(), event.getY(), System.currentTimeMillis());
                            lastX = event.getX();
                            lastY = event.getY();
                            isDragging = true;
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            if (isDragging) {
                                float dx = event.getX() - lastX;
                                float dy = event.getY() - lastY;

                                accumulatedDx += dx;
                                accumulatedDy += dy;

                                //NOTE: NNOT USING THIS,USING TIME-TICKER BASED APPROACH:  if (Math.abs(dx) > MOVE_THRESHOLD || Math.abs(dy) > MOVE_THRESHOLD) {

                                long currentTime = System.currentTimeMillis();
                                if (currentTime - lastSendTime >= SEND_THRESHOLD) {
                                    // Simulate mouse movement
                                    //if (dx != 0 || dy != 0) {  // Only show toast if there's actual movement
                                    //Toast.makeText(getContext(), "Mouse Move: dx=" + dx + ", dy=" + dy, Toast.LENGTH_SHORT).show();

                                    int port = 10000;
                                    String data = String.format("REL_XY:%f:%f", accumulatedDx, accumulatedDy);
                                    SendData(data, port);
                                    accumulatedDx = 0; // Reset accumulated values
                                    accumulatedDy = 0;
                                    lastSendTime = currentTime; // Update time of last send
                                }

                            }
                            lastX = event.getX();
                            lastY = event.getY();
                            return true;

                        case MotionEvent.ACTION_UP:
                            isDragging = false;
                            // Detect click
                            // MAYBE Need to compare ACTION_DOWN timestamp and this event's timestamp
                            if (Math.abs(event.getX() - lastActionDown.x) < CLICK_THRESHOLD && Math.abs(event.getY() - lastActionDown.y) < CLICK_THRESHOLD) {
                                //Toast.makeText(getContext(), "Mouse Click", Toast.LENGTH_SHORT).show();
                                Log.i("TAG","MOUSE CLICK");
                                //send to rc server:
                                int port = 10000;
                                String data = "LCLICK";
                                SendData(data, port);
                            }
                            return true;
                    }
                    return false;
            }
        });

        return root;
    }


    //TODO:De-deplucate this code
    private DatagramSocket UDPSocket;
    private InetAddress address;
    private int port=10000;


    InetAddress getBroadcastAddress2() throws java.net.SocketException {
        // This works both in tethering and when connected to an Access Point

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements())
        {
            NetworkInterface networkInterface = interfaces.nextElement();

            if (networkInterface.isLoopback())
                continue; // Don't want to broadcast to the loopback interface

            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses())
            {
                InetAddress broadcast = interfaceAddress.getBroadcast();

                // InetAddress ip = interfaceAddress.getAddress();
                // interfaceAddress.getNetworkPrefixLength() is another way to express subnet mask

                // Android seems smart enough to set to null broadcast to
                //  the external mobile network. It makes sense since Android
                //  silently drop UDP broadcasts involving external mobile network.
                if (broadcast != null)
                    return broadcast;

                //... // Use the broadcast
            }
        }
        return null;
    }


    /// Initialise une socket avec les parametres recupere dans l'interface graphique pour l'envoi des données
    public void Initreseau(InetAddress address) {
        try {
            this.UDPSocket = new DatagramSocket();
            //XXX
            this.UDPSocket.setBroadcast(true);
            this.address = address;
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }



    /// Envoi X fois la data
    public void SendData(final String Sdata , final int port)  {
        new Thread() {
            @Override
            public void run() {
                try {
                    //CAN I NOT TOAST FROM THIS THREAD??

                    Initreseau(getBroadcastAddress2());
                    byte[] data = Sdata.getBytes();
                    DatagramPacket packet = new DatagramPacket(data, data.length,
                            //address,
                            getBroadcastAddress2(),
                            port);
                    UDPSocket.send(packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }


}
