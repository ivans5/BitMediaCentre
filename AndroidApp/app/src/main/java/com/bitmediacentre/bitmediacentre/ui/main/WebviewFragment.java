package com.bitmediacentre.bitmediacentre.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
//import android.renderscript.Sampler;
import android.text.Editable;
//import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bitmediacentre.bitmediacentre.MainActivity;
import com.bitmediacentre.bitmediacentre.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;



/**
 * WebviewFragment
 */
public class WebviewFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private static final String CLOUD_SERVER_HOSTNAME = "api.bitmediacentre.club";

    private PageViewModel pageViewModel;

    private String theDomain = "";  //crappy popup blocker...

    AutoCompleteTextView theEt = null;


    //TODO: figure out a better way to communicate:
    CountDownLatch latch;
    String resultText = "";

    byte[] publicKeyBytes = null;

    public static WebviewFragment newInstance(int index) {
        WebviewFragment fragment = new WebviewFragment();
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

        //XXX
        final View root = inflater.inflate(R.layout.fragment2_main, container, false);

        final WebView myWebView = (WebView) root.findViewById(R.id.webView);
        final WebView wv = myWebView;
        AutoCompleteTextView editText = (AutoCompleteTextView) root.findViewById(R.id.et);
        final AutoCompleteTextView et = editText;
        theEt = editText;

        final Button b = (Button) root.findViewById(R.id.scan);

        //Scan the DOM for `magnet:` A-HREF links in case their popup-code is causing problems
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String javascriptCode = "var retval='NONE_FOUND'; var elems = document.getElementsByTagName('a');" +
                "for (var i = 0, n = elems.length; i < n; i++) {" +
                "if (elems[i].href.startsWith('magnet:')) { "+
                "        if (retval != 'NONE_FOUND' && elems[i].href != retval)  {retval= 'AMBIGUOUS'} else {" +
                "retval = elems[i].href } } }" +
                "retval;";

                wv.evaluateJavascript(javascriptCode, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        System.err.println("APP_SIDE: value="+value);

                        if (value != null && !value.isEmpty() && value.length() > 2)  {
                            String value2= value.substring(1, value.length()-1);  //remove the double-quotes

                            if (value2.equals("NONE_FOUND"))  {
                                latch = new CountDownLatch(1);
                                resultText = value2 + ": No magnets scanned";
                                latch.countDown();
                            }
                            else if (value2.equals("AMBIGUOUS"))   {
                                latch = new CountDownLatch(1);
                                resultText = value2 + ": multiple magents...";
                                latch.countDown();
                            } else {
                                emitMagnetUrl3(wv, value2);
                                //dont block this thread?
                            }

                            try {
                                latch.await(5, TimeUnit.SECONDS);
                            } catch (java.lang.InterruptedException e ) {
                                resultText="InterruptedException";
                            }

                            Toast.makeText(getContext(),resultText,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                if (wv.canGoBack() == true) {
                    wv.goBack();
                } else {
                    requireActivity().finish();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        wv.setWebChromeClient(new WebChromeClient());

        WebSettings settings = wv.getSettings();
        settings.setJavaScriptEnabled(true);

        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        //settings.setAppCacheEnabled(false); //XXX-DEPRECATED?
        settings.setDomStorageEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowUniversalAccessFromFileURLs(true);
        }

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                et.setText(url);
                //Log.i("TAG","HELLO WRORLD onPageStarted: "+url);
                super.onPageStarted(view,url,favicon);
            }

            /*
            @Override
            public void onPageFinished(WebView view, String url)  {
                ...
            }*/

            //TODO:DELETME:
            /*
            public void loadPopupUrlInHiddenWebView(String url)
            {
                WebView hiddenWebView = null;
                System.err.println("loadPopupUrlInHiddenWebView: HERE: url="+url);

                if (hiddenWebView == null)  {
                    hiddenWebView = new WebView(getContext());
                    hiddenWebView.setVisibility(View.GONE);


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)  {
                        hiddenWebView.createWebMessageChannel();
                    }


                    hiddenWebView.loadUrl(url);
                }
            }
             */

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Here put your code
                //Log.d("My Webview", url);

                if (url.startsWith("magnet:"))  {
                    System.err.println("HERE: url="+url);
                    emitMagnetUrl3(view, url);

                    //dont block this thread?
                    try {
                        latch.await(5, TimeUnit.SECONDS);
                    } catch (java.lang.InterruptedException e ) {
                        resultText="InterruptedException";
                    }
                    Toast.makeText(getContext(),resultText,Toast.LENGTH_SHORT).show();

                    return true;
                }

                //THIS IS SUPPOSED TO BE A CRAPPY POP-UP BLOCKER:
                System.err.println("url="+url+", theDomain="+theDomain);
                try {
                    if (!(new URL(url).getHost().endsWith(theDomain))) {
                        //loadPopupUrlInHiddenWebView(url);
                        return true;  //return true: Indicates WebView to NOT load the url;

                    }
                } catch (java.net.MalformedURLException e) {}


                System.err.println("shouldOverrideUrlLoading: HERE2,url="+url);
                return super.shouldOverrideUrlLoading(view, url); //Allow WebView to load url
            }
        });

        wv.loadUrl("https://duckduckgo.com");
        theDomain = "duckduckgo.com";

        final ArrayList<String> historyItems = new ArrayList<>();

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(),
                android.R.layout.simple_dropdown_item_1line, historyItems);
        et.setAdapter(adapter);

        et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    //XXX - this code path stopped being reached at around android 13?:
                } else {
                        //<-- NOTE: This is still needed...
                        adapter.clear();
                        adapter.addAll(getHistoryItems());
                        adapter.notifyDataSetChanged();

                }
            }
        });

        //NOTE: This one setOnKeyListener doesnt appear to get called:
        /*
        et.setOnKeyListener(new View.OnKeyListener() {
                                @Override
                                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                                    //2023-10-07 11:37:57.255 25148-25148/com.example.mynewapplication I/TAG: ONKEY1,key=KeyEvent { action=ACTION_UP, keyCode=KEYCODE_ENTER, scanCode=28, metaState=0, flags=0x8, repeatCount=0, eventTime=79448241, downTime=79448156, deviceId=0, source=0x101, displayId=-1 }
                                    ...
                                }
                            }
        );
         */


        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                    //Toast.makeText(getContext(),"IME_ACTION_DONE",Toast.LENGTH_SHORT).show();
                    //Log.i("TAG","GOT ENTER");
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
                    addNewHistoryItem(et.getText());
                    handleLoadUrl(false, wv, et);
                    wv.requestFocus();
                } else {
                    Toast.makeText(getContext(),"IME_ACTION: "+Integer.toHexString(actionId),Toast.LENGTH_SHORT).show();
                }
                return false; //<-- whether to "buuble up" the event so that other handler may get it
            }
        });

        return root;
    }

    //** NOTE: Need to clear focus on editText when switching away from this app, otherwise the keyboard will pop back up on other tab...:
    @Override
    public void onPause() {
        super.onPause();

        System.err.println("ONPAUSE - HERE");
        if(theEt != null)  {
            System.err.println("ONPAUSE - HERE - CLEARING FOCUS");
            theEt.clearFocus();
        }
    }


    private void addNewHistoryItem(Editable text) {
        ArrayList<String> newHistoryItems = getHistoryItems();
        if (newHistoryItems.contains(text.toString())) {
            System.err.println("ALREADY PRESENT, SKIPPING");
            return;
        }

        newHistoryItems.add(text.toString());
        JSONArray jsonArray = new JSONArray(newHistoryItems);
        System.err.println("THE JSON TO WRITE IS: "+jsonArray.toString());
        SharedPreferences sp = ((MainActivity) getActivity()).sharedPreferences;

        //Save to the preferences...
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("history-items", jsonArray.toString());
        editor.apply();
    }

    private ArrayList<String> getHistoryItems()  {
        //read SharedPreference value and unmarshal:
        SharedPreferences sp = ((MainActivity) getActivity()).sharedPreferences;
        final String jsonText = sp.getString("history-items","[]");
        try {
            JSONArray jArray = new JSONArray(jsonText);
            ArrayList<String> retval = new ArrayList<>();
            for (int i=0;i<jArray.length();i++){
                retval.add(jArray.getString(i));
            }
            return retval;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }


    private void handleLoadUrl(boolean forceReload, WebView wv, EditText et) {

        String url = et.getText().toString();
        if (url.startsWith("http://")) {
        } else if (url.startsWith("https://")) {
        } else {
            url = String.format("https://%s", url);
        }

        if (!url.equals(wv.getUrl()) || forceReload) {
            wv.loadUrl(url);
        }

        try {
            theDomain = new URL(url).getHost();
        } catch (java.net.MalformedURLException e) {
            System.err.println("MALFORMED URL EXCEPTION!");
        }

    }

    private byte[] encrypt(byte[] keyBytes, byte[] buffer)
    {
        try {

            Cipher rsa;
            rsa = Cipher.getInstance("RSA");
            X509EncodedKeySpec spec =
                    new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");

            rsa.init(Cipher.ENCRYPT_MODE, kf.generatePublic(spec));
            return rsa.doFinal(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    private byte[] readInputStream(InputStream is) throws IOException
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String runEncryption(byte[] secretKey, byte[] iv,String plainText) throws InvalidAlgorithmParameterException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        SecretKeySpec keySpec = new SecretKeySpec(secretKey, 0, 16, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        System.err.println("keySpec is: "+bytesToHex(secretKey));
        System.err.println("iv is: "+bytesToHex(iv));

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));

        String encryptedHexDump = bytesToHex(encrypted);
        String encryptedBase64 = Base64.getEncoder().encodeToString(encrypted);

        System.out.println("Encrypted hex dump = " + encryptedHexDump);
        System.out.println("");
        System.out.println("Encrypted base64 = " + encryptedBase64);
        return encryptedBase64;
    }

    /*
    private static byte[] hexToBytes(String s)
    {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2)
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));

        return data;
    }
     */

    final protected static char[] hexArray = "0123456789abcdef".toCharArray();

    public static String bytesToHex(byte[] bytes)
    {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++)
        {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    private void emitMagnetUrl3(final WebView view, final String magnetText) {

        SharedPreferences sp = ((MainActivity) getActivity()).sharedPreferences;
        final String machineId = sp.getString("machine-id","SETME");
        System.err.println("emitMagnetUrl3: machineId="+machineId);

        latch = new CountDownLatch(1);
        if (machineId.equals("SETME") || machineId.equals(""))  {
            resultText="U NEED TO SET MACHINE ID";
            latch.countDown();
            return;
        }
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                //1. get the public key
                try {
                    resultText = "notsure";

                    System.err.println("machineId IS: "+machineId);

                    URL url = new URL("https://" + CLOUD_SERVER_HOSTNAME + "/getpublickey/" + machineId);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        publicKeyBytes = readInputStream(in);
                    } finally {
                        urlConnection.disconnect();
                    }

                    //Check if non-200 response:
                    //TODO: is this neccessary?
                    if ( urlConnection.getResponseCode() != 200)  {
                        resultText="couldnt get public key, got: "+urlConnection.getResponseCode();
                        latch.countDown();
                        return; //abort thread
                    }

                    System.err.println("publicKeyBytes = "+bytesToHex(publicKeyBytes));

                    //2. Generate the symmetric encryption key (keySpec and ivSpec actually...)
                    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    SecureRandom randomSecureRandom = SecureRandom.getInstance("SHA1PRNG");
                    byte[] iv = new byte[cipher.getBlockSize()];
                    randomSecureRandom.nextBytes(iv);

                    byte[] secretKey = new byte[16];
                    randomSecureRandom.nextBytes(secretKey);

                    //3. Encrypt the keySpec and ivSpec (and base64 encode them...) using the machine's public key:
                    String encryptedKeySpec = Base64.getEncoder().encodeToString(encrypt(publicKeyBytes, bytesToHex(secretKey).getBytes()));
                    String encryptedIvSpec = Base64.getEncoder().encodeToString(encrypt(publicKeyBytes, bytesToHex(iv).getBytes()));

                    //4. Next, encrypt the magnet link using symettric encryption with keySpec and ivSpec:
                    String encryptedMagnetTextBase64 = runEncryption(secretKey, iv, magnetText);

                    //5. POST the encrypted magnet text and symmetric keys to the BitMediaCentre server:
                    String postContent = String.format("{\"EncryptedKeySpec\":\"%s\",\"EncryptedIvSpec\":\"%s\",\"EncryptedPayload\":\"%s\"}",
                        encryptedKeySpec, encryptedIvSpec, encryptedMagnetTextBase64);

                    System.err.println("XXX THE POST CONTENT IS: "+postContent);

                    url = new URL ("https://" + CLOUD_SERVER_HOSTNAME + "/startdownload/"+machineId);

                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    con.setDoOutput(true);
                    try (OutputStream os = con.getOutputStream()) {
                        os.write(postContent.getBytes());
                    }
                    resultText = "POST response was: " + con.getResponseCode();
                } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException e)  {
                    System.err.println("EXception: "+e);
                    resultText=e.toString();
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    resultText=e.toString();
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    resultText=e.toString();
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    resultText=e.toString();
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    resultText=e.toString();
                    e.printStackTrace();
                }
                latch.countDown();
            }
        }.start();
    }

}
