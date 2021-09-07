package com.makrov.alarmmanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.lang.System.gc;
import static java.util.concurrent.TimeUnit.SECONDS;

public class MainActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    //#################### Firebase
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 105;
    ////////////// Timer
    private static final long[] TIMER_MILLIs = {2000, 12000, 3000, 12000, 3000, 12000, 3000, 12000, 3000, 12000, 3000, 12000, 3000, 300000};//
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    //check state orientation of output images
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    int idx_ms = 0;
    long beginMilliSec;
    long beginTimerMillis;
    long timeLeftInMillis;
    Vibrator vibrator;
    //#################### volley api request
    String[] filename = new String[10];
    String[] url = new String[10];
    String responseObject;
    String get_url = "https://jkpeax7xw1.execute-api.us-west-2.amazonaws.com/qa1/question-answer?sem_plus_sub=";
    String get_url_1 = "";
    Button sem_plus_sub_btn;
    String sem_plus_sub;
    String currentPhotoPath;
    StorageReference storageReference; // firebase storage reference
    Button speak_btn;
    String[] img_file = new String[10];
    int idx_img = 0;
    int img_captured = 0;
    Button galleryBtn;
    EditText editText;
    int upl_idx = 0;
    int url_idx = 0;
    private long endTimeInMillis;
    private TextView textViewCountDown;
    private CountDownTimer countDownTimer;
    private boolean timerRunning;
    private TextView textView;
    private String img_source;
    //##################### TTS text to speech
    private TextToSpeech TTS;
    //Save to file
    private File file;
    private CameraDevice cameraDevice;
    //    private CameraCaptureSession cameraCaptureSessions;
//    private CaptureRequest.Builder captureRequestBuilder;
    private Handler backgroundHandler;

    //    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
//        @Override
//        public void onOpened(@NonNull CameraDevice camera) {
//            cameraDevice = camera;
////            createCameraPreview();
//        }
//
//        @Override
//        public void onDisconnected(@NonNull CameraDevice camera) {
//            cameraDevice.close();
//        }
//
//        @Override
//        public void onError(@NonNull CameraDevice camera, int i) {
//            cameraDevice.close();  // closing the camera
//            cameraDevice = null;
//        }
//    };
    private HandlerThread backgroundThread;
    private ImageReader imageReader;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Intent intent = new Intent(MainActivity.this, MyAccessibilityService.class);
        startService(intent);

        new CheckUserRegistered().execute();




        final Intent finalIntent;
        finalIntent = new Intent(this, com.makrov.alarmmanager.GetStartedCheck.class);


        editText = findViewById(R.id.editText);
        sem_plus_sub_btn = findViewById(R.id.sem_plus_Sub_btn);

        sem_plus_sub_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(finalIntent);

                if (get_url.endsWith("?sem_plus_sub=")) { // checking that sem_plus_sub has already appended to url or not.
                    sem_plus_sub = editText.getText().toString();
                    get_url_1 = get_url + sem_plus_sub;
                }
            }
        });

        textView = findViewById(R.id.textView);

        Button buttonTimePicker = findViewById(R.id.button_timepicker);
        buttonTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timerRunning) {
                    resetTimer();
                }
                DialogFragment timePicker = new TimePickerFragment();  // calling a different package TimePickerFragment.java
                timePicker.show(getSupportFragmentManager(), "time picker");
            }
        });

        ////////////////////// Timer
        textViewCountDown = findViewById(R.id.text_view_countdown);
        Button buttonCancel = findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });
        updateCountDownText(beginTimerMillis);
        ///////////////  Vibrator
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        Button quanified_btn = findViewById(R.id.quanified);


        quanified_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (get_url.endsWith("?sem_plus_sub=")) { // checking that sem_plus_sub has already appended to url or not.
                    sem_plus_sub = editText.getText().toString();
                    get_url_1 = get_url + sem_plus_sub;
                }
                textView.setText(get_url_1);
            }
        });

        //////////////  Firabase Storage refenrence
        storageReference = FirebaseStorage.getInstance().getReference(); // initializing the firebase storage reference
        galleryBtn = findViewById(R.id.gallery);
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("IntentReset")
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View view) {
                @SuppressLint("IntentReset")
                Intent gallery = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                gallery.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // for selecting multiple files
                // gallery.setType("images/*"); // for just picking the images
                startActivityForResult(gallery, GALLERY_REQUEST_CODE);
            }
        });

        //////////////////////// camera permissions
//        askCameraPermissions();
        //################## TTS speaking  use the same at taking the time of picture
        TTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = TTS.setLanguage(Locale.ENGLISH);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {
                        speak_btn.setEnabled(true);
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });
        TTS.setSpeechRate((float) 0.6);  // SpeechRate 0.0 < x < 2.0

        speak_btn = findViewById(R.id.speak_btn);
        speak_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    speak(editText.getText().toString());
                    SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    //################### Timer and execution methods
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private synchronized void nextExecution() throws InterruptedException {
        if (countDownTimer != null) {
            countDownTimer.cancel(); // first cancel previously running countdown timer
        }
        if (idx_ms <= TIMER_MILLIs.length) {
            if (TIMER_MILLIs[idx_ms] != 300000) {
                TTS.speak((TIMER_MILLIs[idx_ms]) + " millisecond", TextToSpeech.QUEUE_FLUSH, null);
                SECONDS.sleep(1);
                startTimer(TIMER_MILLIs[idx_ms]);
                updateCountDownText(TIMER_MILLIs[idx_ms]);
                textView.setText("" + TIMER_MILLIs[idx_ms] + " ms step executing.");

//              vibrator.vibrate(VibrationEffect.createOneShot(TIMER_MILLIs[idx_ms] - 1000, VibrationEffect.DEFAULT_AMPLITUDE));
            }
            if (TIMER_MILLIs[idx_ms] == 300000) {
                TTS.speak((TIMER_MILLIs[idx_ms]) + " ms", TextToSpeech.QUEUE_FLUSH, null);
                SECONDS.sleep(1);
                img_captured = idx_img + 1;

                upload_and_make_request(); // uploads based on gallery and local files and make request
            }
            idx_ms += 1;
        } else {
            closeCamera();
        }
    }

    @SuppressLint("SetTextI18n")
    public void upload_and_make_request() {
        if (img_source.equals("LOCAL_DIR")) {
            uploadLocalImgToFirebase();
        }

        closeCamera();

        textView.setText("makeRequestAndSpeak(createUrl(get_url_1));");

        startBackgroundThread();
        backgroundHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    makeRequestAndSpeak(createUrl(get_url));
                } catch (JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, 180000);
    }

    @Override
    public synchronized void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar calender = Calendar.getInstance();
        calender.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calender.set(Calendar.MINUTE, minute);
        calender.set(Calendar.SECOND, 0);

        if (calender.before(Calendar.getInstance())) {
            calender.add(Calendar.DATE, 1);
        }
        updateTimeText(calender);

        beginTimerMillis = calender.getTimeInMillis() - System.currentTimeMillis();

        startTimer(beginTimerMillis);
        updateCountDownText(beginTimerMillis);
    }

    @SuppressLint("SetTextI18n")
    private synchronized void updateTimeText(Calendar calender) {
        textView.setText("Lambda set for: " + DateFormat.getTimeInstance(DateFormat.SHORT).format(calender.getTime()));
    }

    private synchronized void startTimer(long ms) {
        timerRunning = true;
        countDownTimer = new CountDownTimer(ms, 1) {

            @Override
            public void onTick(long millisUntilFinished) {
                updateCountDownText(millisUntilFinished);
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {
                timerRunning = false;
                try {
                    if (TIMER_MILLIs[idx_ms - 1] == 3000) {
                        takePicture();
                    }
                    if (cameraDevice == null) {
                        openCamera();
                    }
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                try {
                    nextExecution();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        timerRunning = true;
    }

    @SuppressLint("SetTextI18n")
    private synchronized void resetTimer() {
        if (countDownTimer != null) {  // stopping timer and rest processes
            beginMilliSec = 0;
            countDownTimer.cancel();
            updateCountDownText(0);
        }
//        if (TTS != null) {  // Stopping TTS
//            TTS.stop();
//            TTS.shutdown();
//        }
        idx_ms = 0;
        idx_img = 0;
        img_captured = 0;

        upl_idx = 0;
        url_idx = 0;
        get_url_1 = "";

        closeCamera();
        textView.setText("Textraction cancelled.");
    }

    @SuppressLint("DefaultLocale")
    private synchronized void updateCountDownText(long timeLeftInMillis) {
        textViewCountDown.setText(String.format("%02d:%02d:%02d.%d", (timeLeftInMillis / (1000 * 60 * 60)) % 24, (timeLeftInMillis / (1000 * 60)) % 60, (timeLeftInMillis / 1000) % 60, timeLeftInMillis % 1000));
    }

//    private void askCameraPermissions() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
//            ActivityCompat.
//        } else {
//            dispatchTakePictureIntent();
//        }
//    }

    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("millisLeft", timeLeftInMillis);
        outState.putBoolean("timerRunning", timerRunning);
        outState.putLong("endTime", endTimeInMillis);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        timeLeftInMillis = savedInstanceState.getLong("millisLeft");
        timerRunning = savedInstanceState.getBoolean("timerRunning");
        updateCountDownText(timeLeftInMillis);
        if (timerRunning) {
            endTimeInMillis = savedInstanceState.getLong("endTime");
            timeLeftInMillis = endTimeInMillis - System.currentTimeMillis();
            startTimer(timeLeftInMillis);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "You can't use camera without the permission grantation.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("Camera Background");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());

    }

    //##################### image methods
    public synchronized void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                cameraDevice = camera;
                img_source = "LOCAL_DIR";
//            createCameraPreview();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                cameraDevice.close();
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int i) {
                cameraDevice.close();  // closing the camera
                cameraDevice = null;
            }
        };

        try {
            assert manager != null;
            StreamConfigurationMap map = manager.getCameraCharacteristics(manager.getCameraIdList()[0]).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            // check realtime permission if run higher API 23
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(manager.getCameraIdList()[0], stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // ctrl + o
    private synchronized void closeCamera() {
        if (null != cameraDevice) {
//            cameraCaptureSessions.close();
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    @SuppressLint("SimpleDateFormat")
    public synchronized void takePicture() {
        if (cameraDevice == null) {
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            assert manager != null;
            int width = 4208;
            int height = 3120;
            imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(imageReader.getSurface());
//            outputSurface.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());

            /////////////////////////////////
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO); //Overall mode of 3A (auto-exposure, auto-white-balance, auto-focus) control routines.
            captureBuilder.set(CaptureRequest.BLACK_LEVEL_LOCK, true);  //Whether black-level compensation is locked to its current values, or is free to vary.
            captureBuilder.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY); //Mode of operation for the chromatic aberration correction algorithm
//            captureBuilder.set(CaptureRequest.COLOR_CORRECTION_GAINS, rggbChannelVector); // lower brightness
            captureBuilder.set(CaptureRequest.COLOR_CORRECTION_MODE, CameraMetadata.COLOR_CORRECTION_MODE_HIGH_QUALITY); //this control is overridden by the AWB routine. When AWB is disabled, the application controls how the color mapping is performed.
            captureBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_OFF); //  brightness = AUTO, OFF, INCANDESCENT, FLORESCENT, CLOUDY_DAYLIGHT
            captureBuilder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_50HZ); //Auto exposure 50HZ i india
            captureBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 30);
            captureBuilder.set(CaptureRequest.CONTROL_AE_LOCK, true);
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE); // continuous autofocus
            captureBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_BARCODE); // BARCODE SCANNING FOR TEXT DETECTION
//            captureBuilder.set(CaptureRequest.DISTORTION_CORRECTION_MODE, CameraMetadata.DISTORTION_CORRECTION_MODE_HIGH_QUALITY);  //The lens distortion correction block attempts to improve image quality by fixing radial, tangential, or other geometric aberrations in the camera device's optic
            captureBuilder.set(CaptureRequest.EDGE_MODE, CameraMetadata.EDGE_MODE_HIGH_QUALITY); // EDGE DETECTION
            captureBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_BARCODE);
            captureBuilder.set(CaptureRequest.HOT_PIXEL_MODE, CameraMetadata.HOT_PIXEL_MODE_HIGH_QUALITY);
            captureBuilder.set(CaptureRequest.JPEG_QUALITY, (byte) 100);  //1-100; larger is higher quality
//            captureBuilder.set(CaptureRequest.LENS_FOCAL_LENGTH, (float) 15);
            captureBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, (float) 15);
            captureBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_ON);
            captureBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE, CameraMetadata.NOISE_REDUCTION_MODE_HIGH_QUALITY);
            captureBuilder.set(CaptureRequest.CONTROL_POST_RAW_SENSITIVITY_BOOST, 30);
            captureBuilder.set(CaptureRequest.REPROCESS_EFFECTIVE_EXPOSURE_FACTOR, (float) 20); //The amount of exposure time increase factor applied to the original output frame by the application processing before sending for reprocessing.
            captureBuilder.set(CaptureRequest.SENSOR_FRAME_DURATION, (long) 100000000); //Duration from start of frame exposure to start of next frame exposure.
            captureBuilder.set(CaptureRequest.TONEMAP_MODE, CameraMetadata.TONEMAP_MODE_CONTRAST_CURVE);
            captureBuilder.set(CaptureRequest.TONEMAP_PRESET_CURVE, CameraMetadata.TONEMAP_PRESET_CURVE_SRGB);

            // check orientation based on the device
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(getWindowManager().getDefaultDisplay().getRotation()));

            //////////// saving img files directory
            img_file[idx_img] = "JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg";
            final String imageFileName = Environment.getExternalStorageDirectory().getPath() + "/" + img_file[idx_img];
            Log.e("FILENAME", imageFileName);
            file = new File(imageFileName);
            idx_img += 1; // incrementing the index for saving the image file

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    try (Image image = imageReader.acquireLatestImage()) {
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                private void save(byte[] bytes) throws IOException {
                    try (OutputStream outputStream = new FileOutputStream(file)) {
                        outputStream.write(bytes);
                        Log.e("outputStream", " saved: " + imageFileName);
                    }
                }
            };
            imageReader.setOnImageAvailableListener(readerListener, backgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                }
            };
            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try {
                        cameraCaptureSession.capture(captureBuilder.build(), captureListener, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                }
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    //################### Firebase methods
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("ShowToast")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { //uploading the files selected from the gallery
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            img_source = "LOCAL_GALLERY";
            assert data != null;
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    @SuppressLint("SimpleDateFormat")
                    String imageFileName = "JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "." + getFileExt(data.getClipData().getItemAt(i).getUri());
                    uploadImageToFirebase(imageFileName, data.getClipData().getItemAt(i).getUri(), "LOCAL_GALLERY");
                }
                upload_and_make_request();
            }
        }
    }

    private synchronized void uploadLocalImgToFirebase() {
        if (img_captured - 1 > upl_idx) {
            if (img_file[upl_idx] == null) {
                upl_idx += 1;
                uploadLocalImgToFirebase();
            } else {
                Uri contentUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath() + "/" + img_file[upl_idx]));
                uploadImageToFirebase(img_file[upl_idx], contentUri, "LOCAL_DIR");
            }
        }
    }

    private synchronized void uploadImageToFirebase(final String name, Uri contentUri, final String source) { /// grab from here the url and name of the file both
        final StorageReference image = storageReference.child("pictures/" + name);
        image.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() { // uploading the image to pictures/photo.png
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(Uri uri) {
                        filename[url_idx] = name;
                        url[url_idx] = uri.toString();
                        Log.e("uploadImageToFirebase", " URL: " + url[url_idx]);
                        TTS.speak("image upload success.", TextToSpeech.QUEUE_FLUSH, null);
                        try {
                            SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        url_idx += 1;
                        upl_idx += 1;

                        if (source.equals("LOCAL_DIR")) {
                            uploadLocalImgToFirebase();
                            img_source = "LOCAL_DIR";
                        } else if (source.equals("LOCAL_GALLERY")) {
                            textView.setText("LOCAL_GALLERY");
                            img_source = "LOCAL_GALLERY";
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("uploadImageToFirebase", " failed: " + e.getMessage());
                TTS.speak("image upload failed.", TextToSpeech.QUEUE_FLUSH, null);
                try {
                    SECONDS.sleep(1);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                upl_idx += 1;
                if (source.equals("LOCAL_DIR")) {
                    uploadLocalImgToFirebase();
                }
            }
        });
    }

    private synchronized String getFileExt(Uri contentUri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(contentUri));
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the file Where the photo should go
            File photofile = null;
            try {
                photofile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photofile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.makrov.uploadtofirebase", photofile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    private synchronized File createImageFile() throws IOException {
        // create an image file name
        @SuppressLint("SimpleDateFormat")
        File image = File.createTempFile("JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_", //prefix
                ".jpg", // suffix
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)); // directory

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    ///////////////////////////  api request
    @SuppressLint({"WrongConstant", "ShowToast"})
    private synchronized String createUrl(String u) {
        //////////////////////make api get request right in here
        if (u.endsWith("?sem_plus_sub=")) { // checking that sem_plus_sub has already appended to url or not.
            sem_plus_sub = editText.getText().toString();
            get_url_1 = u = u + sem_plus_sub;
        }
        StringBuilder uBuilder = new StringBuilder(u);
        for (int i = 0; i < 10; i++) {
            try {
                if (url[i] != null) {
                    uBuilder.append("&photoUrl_").append(i).append("=").append(url[i]);
                    url[i] = "";
                    filename[i] = "";
                }
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        u = uBuilder.toString();
        Log.e("createURL", u);

        Log.e("paramString", u);

        Toast.makeText(this, u, Toast.LENGTH_SHORT).show();

        return u;

    }

    private void postRequest(String url) {
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(String response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("data_1_post", "value 1 data");
                params.put("data_2_post", "value 2 data");
                params.put("data_3_post", "value 3 data");
                return params;

            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }


    private synchronized void sendGetRequest(String url) {
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(String response) {
                Log.e("sendGetRequest", "Volley Request succeed:" + response);
                TTS.speak("Volley Request succeed.", TextToSpeech.QUEUE_FLUSH, null);
                try {
                    SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                textView.setText("Volley Request succeed: " + response);
                responseObject = response;

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("sendGetRequest", "Volley Request failed:" + error.getMessage());
                TTS.speak("Volley Request failed.", TextToSpeech.QUEUE_FLUSH, null);
                try {
                    SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                textView.setText(("Volley Request failed: " + error.getMessage()));
            }
        });
        queue.add(stringRequest);
    }

    private synchronized void makeRequestAndSpeak(String u) throws JSONException, InterruptedException {
        TTS.speak("making get request", TextToSpeech.QUEUE_FLUSH, null);
        SECONDS.sleep(1);
        sendGetRequest(u);  // responseObject = response;
        if (responseObject != null) {
            for (int i = 0; i < 3; i++) {
                // call the methods for start speaking these sentences.
                if (speak(new JSONObject(responseObject).getString((i + 1) + "_ans"))) {
                    continue;
                } else {
                    Log.e("makeRequestAndSpeak", "TTS failing");
                    TTS.speak("text to speech failing.", TextToSpeech.QUEUE_FLUSH, null);
                    SECONDS.sleep(1);

                }
            }
        } else {
            Log.e("makeRequestAndSpeak", "api response is null");
            TTS.speak("api response is null.", TextToSpeech.QUEUE_FLUSH, null);
            SECONDS.sleep(1);
        }
    }

    //#########################  TTS speaking the en language

    private synchronized boolean speak(String text) throws InterruptedException {
//        float pitch = (float) seekBarPitch.getProgress() / 50;
//        if (pitch < 0.1)
//            pitch = 0.1f;
//        TTS.setPitch(pitch);
//        float speed = (float) seekBarSpeed.getProgress() / 50;
//        if (speed < 0.1)
//            speed = 0.1f;
        String[] s_arr = text.split(" ");
        text = null;
        gc(); // garbage collection
        String words = "";
        for (int idx_space = 1; idx_space < s_arr.length; idx_space++) {
            words += s_arr[idx_space - 1] + " ";
            if (idx_space % 5 == 0) {
                for (int i = 0; i < 3; ) {
                    if (!TTS.isSpeaking()) {
                        i += 1;
                        SECONDS.sleep((long) 1.3);
                        TTS.speak(words, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
                SECONDS.sleep(2);
                words = "";
            }
        }
        s_arr = null;
        words = null;
        gc(); // garbage collection
        return true;
    }

    @Override
    protected void onDestroy() {
        if (TTS != null) {
            TTS.stop();
            TTS.shutdown();
        }
        super.onDestroy();
    }

}
