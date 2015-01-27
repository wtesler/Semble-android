package tesler.will.semble;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

@SuppressWarnings("deprecation")
public class QrCamera {

    public static final String TAG = "QrCamera";

    Camera camera;
    // convert the image to a binary bitmap source
    QRCodeReader reader = new QRCodeReader();

    QrCamera(final Activity context, final SurfaceHolder preview) {

        int cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    camera = Camera.open(camIdx);
                    Display display =
                            ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                                    .getDefaultDisplay();
                    Camera.Parameters params = camera.getParameters();
                    List<Camera.Size> sizes = params.getSupportedPreviewSizes();

                    final int width = (int) sizes.get(0).width;
                    final int height = (int) sizes.get(0).height;

                    params.setPreviewSize(width, height);
                    camera.setParameters(params);
                    camera.setDisplayOrientation(((display.getOrientation() + 1) * 90));
                    camera.setPreviewDisplay(preview);
                    camera.setPreviewCallback(new PreviewCallback() {
                        @Override
                        public void onPreviewFrame(byte[] data, Camera camera) {
                            LuminanceSource source =
                                    new PlanarYUVLuminanceSource(
                                            data,
                                            width, height,
                                            0, 0,
                                            width, height,
                                            false);
                            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                            Result result;
                            try {
                                result = reader.decode(bitmap, null);
                                Log.i(TAG, "result: " + result.getText());
                            } catch (ReaderException e) {
                                // the data is improperly formatted
                                Log.d(TAG, "error: " + e.getMessage());
                                return;
                            }
                        }
                    });
                    break;
                } catch (RuntimeException e) {
                    Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
                } catch (IOException e) {
                    Log.e(TAG, "Preview failed to open: " + e.getLocalizedMessage());
                }
            }
        }
    }

    public void startPreview() {
        camera.startPreview();
    }

    public void stopPreview() {
        camera.stopPreview();
    }
}
