package com.bitmediacentre.bitmediacentre.ui.main;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bitmediacentre.bitmediacentre.MainActivity;
import com.bitmediacentre.bitmediacentre.R;

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

    private PageViewModel pageViewModel;

    private String theDomain = "";  //crappy popup blocker...

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
        EditText editText = (EditText) root.findViewById(R.id.et);
        final EditText et = editText;

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
        settings.setAppCacheEnabled(false);
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
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Here put your code
                //Log.d("My Webview", url);

                if (url.startsWith("magnet:"))  {
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
                        return true;  //return true: Indicates WebView to NOT load the url;
                    }
                } catch (java.net.MalformedURLException e) {}

                return false; //Allow WebView to load url
            }
        });

        wv.loadUrl("https://html5test.com");
        theDomain = "html5test.com";

        et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    handleLoadUrl(false, wv, et);
                }
            }
        });



        return root;
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

                    URL url = new URL("https://api.bitmediacentre.ca/getpublickey/" + machineId);
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

                    url = new URL ("https://api.bitmediacentre.ca/startdownload/"+machineId);

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
