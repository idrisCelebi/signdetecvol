package ai.ido.camera;

import android.graphics.Canvas;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.util.Size;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.fritz.core.Fritz;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.FritzVisionOrientation;
import ai.fritz.vision.styletransfer.FritzVisionStyleResult;

public class MainActivity extends BaseCameraActivity implements ImageReader.OnImageAvailableListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    public FritzVisionImage visionImage;
    public static final Size DESIRED_PREVIEW_SIZE = new Size(1280, 960);

    public AtomicBoolean computing = new AtomicBoolean(false);

    public String styleResult;

    // STEP 1:
    // TODO: Define the predictor variable
     public GestureRecognizer predictor;
    // END STEP 1

    private Size cameraViewSize;



    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Fritz
        Fritz.configure(this,"254e966dbade4de68364c7c7c6de0554");

        // STEP 1: Get the predictor and set the options.
        // ----------------------------------------------
        // A FritzOnDeviceModel object is available when a model has been
        // successfully downloaded and included with the app.
        // TODO: uncomment the following lines to use a style transfer model to pass into the style predictor. You must include ``implementation "ai.fritz:vision-style-painting-models:${sdk_version}"`` in your app/build.gradle file

        // FritzOnDeviceModel[] styles = PaintingStyles.getAll();
        // predictor = FritzVision.StyleTransfer.getPredictor(styles[0]);
        // ----------------------------------------------
        // END STEP 1
    }

    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection_fragment_stylize;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    @Override
    public void onPreviewSizeChosen(final Size previewSize, final Size cameraViewSize, final int rotation) {

        this.cameraViewSize = cameraViewSize;

        // Callback draws a canvas on the OverlayView
        addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        // STEP 4: Draw the prediction result
                        // ----------------------------------
                        if (styleResult != null) {
                            // TODO: Draw or show the result here

                        }
                        // ----------------------------------
                        // END STEP 4
                    }
                });
    }

    @Override
    public void onImageAvailable(final ImageReader reader) {
        Image image = reader.acquireLatestImage();

        if (image == null) {
            return;
        }

        if (!computing.compareAndSet(false, true)) {
            image.close();
            return;
        }

        // STEP 2: Create the FritzVisionImage object from media.Image
        // ------------------------------------------------------------------------
        // TODO: Add code for creating FritzVisionImage from a media.Image object
        int rotationFromCamera = FritzVisionOrientation.getImageRotationFromCamera(this, cameraId);
        visionImage = FritzVisionImage.fromMediaImage(image, rotationFromCamera);

        // ------------------------------------------------------------------------
        // END STEP 2

        image.close();


        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        // STEP 3: Run predict on the image
                        // ---------------------------------------------------
                        // TODO: Add code for running prediction on the image
                        styleResult = predictor.predict(visionImage.rotateBitmap());
                        System.out.println(styleResult);
                        // ----------------------------------------------------
                        // END STEP 3

                        // Fire callback to change the OverlayView

                        requestRender();
                        computing.set(false);
                    }
                });
    }


}
