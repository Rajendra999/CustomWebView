 package harsha_main.github.customwebview;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends Activity {

    final String Url =
            "https://drive.google.com/uc?export=download&id=1oPmin1dmKGsEzCRNiZ_o2Bbg7uicKhU7";

    String base =
            Environment.getExternalStorageDirectory().toString();

    String filename = "/Webfiles.zip";
    String directory = "/newdir";

    String StorezipFileLocation =
            base + directory + filename;

    WebView webView;
    ProgressBar progressBar;
    Button but;

    ProcessZipfile mew;

    String device_id;

    private static final int PICK_HTML_FILE = 100;
    private static final int FILE_CHOOSER_REQUEST = 101;

    private ValueCallback<Uri[]> filePathCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.web);
        but = findViewById(R.id.button);
        progressBar = findViewById(R.id.progressBar);


        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);


        webView.setWebViewClient(new WebViewClient());


        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onShowFileChooser(
                    WebView webView,
                    ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {

                MainActivity.this.filePathCallback =
                        filePathCallback;

                try {

                    Intent intent =
                            fileChooserParams.createIntent();

                    startActivityForResult(
                            intent,
                            FILE_CHOOSER_REQUEST
                    );

                    return true;

                } catch (Exception e) {

                    MainActivity.this.filePathCallback = null;

                    return false;
                }
            }


            @Override
            public boolean onConsoleMessage(
                    ConsoleMessage consoleMessage) {

                Log.d(
                        "WebViewConsole",
                        consoleMessage.message()
                                + " -- line "
                                + consoleMessage.lineNumber()
                                + " of "
                                + consoleMessage.sourceId()
                );

                return true;
            }
        });


        if (Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        1
                );
            }
        }


        device_id = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );


        new File(base + directory).mkdirs();

        mew = new ProcessZipfile();


        but.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                openHtmlFilePicker();

                Animator animator = null;

                if (Build.VERSION.SDK_INT
                        >= Build.VERSION_CODES.LOLLIPOP) {

                    animator =
                            ViewAnimationUtils.createCircularReveal(
                                    getWindow().getDecorView(),
                                    getWindow().getDecorView().getWidth() / 2,
                                    getWindow().getDecorView().getHeight() / 2,
                                    0,
                                    500
                            );

                    animator.start();
                }

                Toast.makeText(
                        MainActivity.this,
                        "getting data",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }


    protected void onResume() {

        super.onResume();

        if (isNetworkConnected()) {
            return;
        }

        Toast.makeText(
                this,
                "Check your internet and try again",
                Toast.LENGTH_SHORT
        ).show();


        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        builder.setMessage(
                        "You need internet connection for this app. Please turn on mobile network or Wi-Fi in Settings."
                )
                .setTitle("Unable to connect to Internet")
                .setCancelable(false)

                .setPositiveButton(
                        "Settings",
                        new DialogInterface.OnClickListener() {

                            public void onClick(
                                    DialogInterface dialog,
                                    int id) {

                                startActivity(
                                        new Intent(
                                                "android.settings.SETTINGS"
                                        )
                                );
                            }
                        }
                )

                .setNegativeButton(
                        "Cancel",
                        new DialogInterface.OnClickListener() {

                            public void onClick(
                                    DialogInterface dialog,
                                    int id) {

                                MainActivity.this.finish();
                            }
                        }
                );

        builder.create().show();
    }


    private boolean isNetworkConnected() {

        return (
                (ConnectivityManager)
                        getSystemService(
                                Context.CONNECTIVITY_SERVICE
                        )
        ).getActiveNetworkInfo() != null;
    }


    class ProcessZipfile
            extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(
                String... aurl) {

            int count;

            try {

                URL url =
                        new URL(aurl[0]);

                URLConnection connection =
                        url.openConnection();

                connection.connect();

                InputStream input =
                        new BufferedInputStream(
                                url.openStream()
                        );

                OutputStream output =
                        new FileOutputStream(
                                StorezipFileLocation
                        );

                byte data[] =
                        new byte[8192];

                while ((count =
                        input.read(data)) != -1) {

                    output.write(
                            data,
                            0,
                            count
                    );
                }

                output.close();
                input.close();

                extractZip();

                modifyhtml();

            } catch (Exception e) {

                Log.e(
                        "ZIP",
                        "Download error",
                        e
                );
            }

            return null;
        }


        void extractZip() throws Exception {

            int BUFFER_SIZE = 4096;

            String zipFilePath =
                    base + directory + filename;

            String destDirectory =
                    base + directory;

            ZipInputStream zipIn =
                    new ZipInputStream(
                            new FileInputStream(
                                    zipFilePath
                            )
                    );


            while (true) {

                ZipEntry entry =
                        zipIn.getNextEntry();

                if (entry == null) {
                    break;
                }


                String filePath =
                        destDirectory
                                + File.separator
                                + entry.getName();


                if (!entry.isDirectory()) {

                    File parent =
                            new File(filePath)
                                    .getParentFile();

                    if (parent != null) {
                        parent.mkdirs();
                    }

                    BufferedOutputStream bos =
                            new BufferedOutputStream(
                                    new FileOutputStream(
                                            filePath
                                    )
                            );

                    byte[] bytesIn =
                            new byte[BUFFER_SIZE];

                    int read;

                    while ((read =
                            zipIn.read(bytesIn)) != -1) {

                        bos.write(
                                bytesIn,
                                0,
                                read
                        );
                    }

                    bos.close();

                } else {

                    File dir =
                            new File(filePath);

                    dir.mkdirs();
                }

                zipIn.closeEntry();
            }

            zipIn.close();
        }


        void modifyhtml() throws Exception {

            String content =
                    "document.write(\"Device id: "
                            + device_id
                            + "\")";


            Writer writer =
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(
                                            base
                                                    + directory
                                                    + "/First-Aid Kit_files/device.js"
                                    )
                            )
                    );


            writer.write(content);

            writer.close();
        }


        @Override
        protected void onPostExecute(String s) {

            super.onPostExecute(s);

            Toast.makeText(
                    MainActivity.this,
                    "Finished",
                    Toast.LENGTH_LONG
            ).show();

            progressBar.setVisibility(View.GONE);

            webView.setVisibility(View.VISIBLE);

            webView.getSettings()
                    .setJavaScriptEnabled(true);


            Log.e(
                    "base",
                    base
                            + directory
                            + "/First-Aid Kit.html"
            );


            Animator animator = null;

            if (Build.VERSION.SDK_INT
                    >= Build.VERSION_CODES.LOLLIPOP) {

                animator =
                        ViewAnimationUtils.createCircularReveal(
                                getWindow().getDecorView(),
                                getWindow().getDecorView().getWidth() / 2,
                                getWindow().getDecorView().getHeight() / 2,
                                500,
                                0
                        );

                animator.start();
            }
        }
    }


    private void openHtmlFilePicker() {

        Intent intent =
                new Intent(Intent.ACTION_GET_CONTENT);

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        intent.setType("*/*");

        startActivityForResult(
                intent,
                PICK_HTML_FILE
        );
    }


    @Override
    public void onBackPressed() {

        if (webView != null
                && webView.canGoBack()) {

            webView.goBack();

        } else {

            super.onBackPressed();
        }
    }


    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );


        if (requestCode == PICK_HTML_FILE) {

            if (resultCode == RESULT_OK
                    && data != null
                    && data.getData() != null) {

                Uri uri = data.getData();

                try {

                    File htmlFile =
                            new File(
                                    getCacheDir(),
                                    "selected.html"
                            );

                    InputStream input =
                            getContentResolver()
                                    .openInputStream(uri);

                    FileOutputStream output =
                            new FileOutputStream(
                                    htmlFile
                            );

                    byte[] buffer =
                            new byte[8192];

                    int length;

                    while ((length =
                            input.read(buffer)) != -1) {

                        output.write(
                                buffer,
                                0,
                                length
                        );
                    }

                    input.close();
                    output.close();


                    webView.getSettings()
                            .setJavaScriptEnabled(true);


                    webView.loadUrl(
                            "file://"
                                    + htmlFile.getAbsolutePath()
                    );


                } catch (Exception e) {

                    Log.e(
                            "HTML_LOAD",
                            "Error loading selected HTML",
                            e
                    );

                    Toast.makeText(
                            MainActivity.this,
                            "HTML file could not be opened",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            return;
        }


        if (requestCode == FILE_CHOOSER_REQUEST) {

            if (filePathCallback == null) {
                return;
            }


            Uri[] results = null;


            if (resultCode == RESULT_OK
                    && data != null) {

                Uri uri = data.getData();

                if (uri != null) {

                    results =
                            new Uri[]{uri};
                }
            }


            filePathCallback.onReceiveValue(
                    results
            );

            filePathCallback = null;
        }
    }
}
