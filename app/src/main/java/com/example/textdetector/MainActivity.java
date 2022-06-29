package com.example.textdetector;

import static android.Manifest.permission.CAMERA;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class MainActivity extends AppCompatActivity {

    // creating variables for our
    // image view, text view and two buttons.
    private ImageView img;
    private TextView textview;
    private Button snapBtn;
    private Button detectBtn;
    private Button copy;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    // variable for our image bitmap.
    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // hide action bar
        getSupportActionBar().hide();

        // on below line we are initializing our variables.
        img = (ImageView) findViewById(R.id.image);
        textview = (TextView) findViewById(R.id.text);
        snapBtn = (Button) findViewById(R.id.snapbtn);
        detectBtn = (Button) findViewById(R.id.detectbtn);

        // copy text to clipboard
        ImageButton copy = findViewById(R.id.copy);
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Text",textview.getText().toString());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(MainActivity.this,"Copied",Toast.LENGTH_SHORT).show();

            }
        });

        //  share option
        ImageButton share = findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = textview.getText().toString();
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, s);
                startActivity(Intent.createChooser(sharingIntent, "Share text via"));
            }
        });


        // adding on click listener for detect button.
        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calling a method to
                // detect a text .
                detectTxt();
            }
        });
        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calling a method to capture our image.
                if (checkPermissions()) {
                    captureImage();
                } else {
                    requestPermission();
                }
            }
        });
    }

    private boolean checkPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(),CAMERA);
        return cameraPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        int PERMISSSION_CODE = 200;
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, PERMISSSION_CODE);
    }

    private void captureImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (cameraPermission) {
                Toast.makeText(this, "Permission granted...", Toast.LENGTH_SHORT).show();
                captureImage();
            } else {
                Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show();
            }
        }

    }

        @Override
        protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
            super.onActivityResult(requestCode, resultCode, data);
            // calling on activity result method.
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                // on below line we are getting
                // data from our bundles. .
                Bundle extras = data.getExtras();
                imageBitmap = (Bitmap) extras.get("data");

                // below line is to set the
                // image bitmap to our image.
                img.setImageBitmap(imageBitmap);
            }
        }

        private void detectTxt() {
            InputImage image = InputImage.fromBitmap(imageBitmap, 0);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            Task<Text> result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
                @Override
                public void onSuccess(Text text) {
                    StringBuilder result = new StringBuilder();
                    for (Text.TextBlock block : text.getTextBlocks()) {
                        String blockText = block.getText();
                        Point[] blockCornerPoint = block.getCornerPoints();
                        Rect blockFrame = block.getBoundingBox();
                        for (Text.Line line : block.getLines()) {
                            String lineText = line.getText();
                            Point[] lineCornerPoint = line.getCornerPoints();
                            Rect lineRect = line.getBoundingBox();
                            for (Text.Element element : line.getElements()) {
                                String elementText = element.getText();
                                result.append(elementText);
                            }
                            textview.setText(blockText);
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // handling an error listener.
                    Toast.makeText(MainActivity.this, "Fail to detect the text from image..", Toast.LENGTH_SHORT).show();
                }
            });

        }

    }
