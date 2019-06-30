package com.androidtutorialpoint.androidpdfcreator;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;


public class PdfCreatorActivity extends AppCompatActivity {
    private static final String TAG = "PdfCreatorActivity";
    private EditText  mContentEditText;
    private Button mCreateButton;
    private File pdfFile;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfcreator);

        mContentEditText = (EditText) findViewById(R.id.edit_text_content);
        mCreateButton = (Button) findViewById(R.id.button_create);
        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mContentEditText.getText().toString().isEmpty()){
                    mContentEditText.setError("Body is empty");
                    mContentEditText.requestFocus();
                    return;
                }

                try {
                    createPdfWrapper();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                }

            }
        });

    }
    private void createPdfWrapper() throws FileNotFoundException,DocumentException{

            int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                        showMessageOKCancel("You need to allow access to Storage",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                    REQUEST_CODE_ASK_PERMISSIONS);
                                        }
                                    }
                                });
                        return;
                    }


                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_ASK_PERMISSIONS);
                }
                    return;
            }else {
                createPdf();
            }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    try {
                        createPdfWrapper();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Permission Denied
                    Toast.makeText(this, "WRITE_EXTERNAL Permission Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    
    private void createPdf() throws FileNotFoundException, DocumentException {

        if(hasExternalStorage().mExternalStorageAvailable && hasExternalStorage().mExternalStorageWriteable){
            File docsFolder = new File(this.getFilesDir() + "/Documents");
            if (!docsFolder.exists()) {
                docsFolder.mkdir();
            }

            pdfFile = new File(docsFolder.getAbsolutePath(),"HelloWorld.pdf");
            OutputStream output = new FileOutputStream(pdfFile);
            Document document = new Document();
            BaseFont urName = null;
            try {
                urName = BaseFont.createFont("assets/fonts/Alkaios.ttf", BaseFont.IDENTITY_H,BaseFont.EMBEDDED); //----------------------------->
            } catch (IOException e) {
                e.printStackTrace();
            }
            Font urFontName = new Font(urName, 25);
            PdfWriter.getInstance(document, output);
            document.open();
            document.add(new Paragraph(mContentEditText.getText().toString(), urFontName));

            document.close();
            previewPdf1();
        }
        else{
            Toast.makeText(this, "Ηλίθιε δεν έχεις κάρτα μνήμης", Toast.LENGTH_LONG).show();
        }
    }


    private void previewPdf1() {

        PackageManager packageManager = getPackageManager();
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setType("application/pdf");
        List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
        try {
            if (list.size() > 0) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri apkURI = FileProvider.getUriForFile(PdfCreatorActivity.this,BuildConfig.APPLICATION_ID + ".provider", pdfFile);
                intent.setDataAndType(apkURI, "application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                startActivity(intent);
            } else {
                Toast.makeText(this, "Download a PDF Viewer to see the generated PDF", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

  class MyResult {
        private final boolean mExternalStorageAvailable;
        private final boolean mExternalStorageWriteable;

    public MyResult(boolean mExternalStorageAvailable, boolean mExternalStorageWriteable) {
        this.mExternalStorageAvailable = mExternalStorageAvailable;
        this.mExternalStorageWriteable = mExternalStorageWriteable;
    }

    public boolean ismExternalStorageAvailable() {
        return mExternalStorageAvailable;
    }

    public boolean ismExternalStorageWriteable() {
        return mExternalStorageWriteable;
    }
}


    private  MyResult hasExternalStorage(){
        boolean mExternalStorageAvailable;
        boolean mExternalStorageWriteable;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) || state.equals(Environment.MEDIA_MOUNTED) ) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        return new MyResult(mExternalStorageAvailable, mExternalStorageWriteable);
    }

}
