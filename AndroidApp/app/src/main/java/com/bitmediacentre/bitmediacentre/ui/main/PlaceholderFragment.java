package com.bitmediacentre.bitmediacentre.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private static final int[] the_ids = {R.id.esc,R.id.f2,R.id.alty,R.id.up,
        R.id.down,R.id.enter,R.id.space,R.id.q};

    private PageViewModel pageViewModel;

    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
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

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        /*
        final TextView textView = root.findViewById(R.id.section_label);
        pageViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
*/

        final Context finalContext = root.getContext();

        for (int id : the_ids) {
            root.findViewById(id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String data = (String) v.getTag();
                    if (data != null) {
                        int port = 10000;
                        SendData(data, port);
                    } else {
                        Toast.makeText(finalContext, "error: data=" + data, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        return root;
    }

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

    private DatagramSocket UDPSocket;
    private InetAddress address;
    private int port=10000;

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