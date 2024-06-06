package com.fransbudikashira.chefies.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
//import com.google.android.gms.tflite.client.TfLiteInitializationOptions
//import com.google.android.gms.tflite.gpu.support.TfLiteGpu
import org.tensorflow.lite.DataType
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector
//import org.tensorflow.lite.task.gms.vision.TfLiteVision
//import org.tensorflow.lite.task.gms.vision.detector.Detection
//import org.tensorflow.lite.task.gms.vision.detector.ObjectDetector

class ObjectDetectorHelper(
    val thresholdInfo: Float = 0.1f,
    val maxResult: Int = 10,
    val modelName: String = "ingredient_classification.tflite",
    val context: Context,
    val detectorListener: DetectorListener?
) {
    private var objectDetector: ObjectDetector? = null

//    init {
//        TfLiteGpu.isGpuDelegateAvailable(context).onSuccessTask { gpuAvailable ->
//            val optionBuilder = TfLiteInitializationOptions.builder()
//            if (gpuAvailable) optionBuilder.setEnableGpuDelegateSupport(true)
//            TfLiteVision.initialize(context, optionBuilder.build())
//        }.addOnSuccessListener {
//            setupObjectDetector()
//        }.addOnFailureListener{
//            detectorListener?.onError("TfLiteVision is not initialized yet")
//        }
//    }

    private fun setupObjectDetector() {
        val optionsBuilder = ObjectDetector.ObjectDetectorOptions.builder()
            .setScoreThreshold(thresholdInfo)
            .setMaxResults(maxResult)
        val baseOptionBuilder = BaseOptions.builder()

        when {
            CompatibilityList().isDelegateSupportedOnThisDevice -> baseOptionBuilder.useGpu()
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 -> baseOptionBuilder.useNnapi()
            else -> baseOptionBuilder.setNumThreads(4) //use CPU
        }

        optionsBuilder.setBaseOptions(baseOptionBuilder.build())

        try {
            objectDetector = ObjectDetector.createFromFileAndOptions(
                context, modelName, optionsBuilder.build()
            )
        } catch (e: IllegalStateException) {
            detectorListener?.onError("Image classifier failed to initialize. See error logs for details")
            Log.e(TAG, e.message.toString())
        }
    }

    fun detectObject(imageUri: Uri) {
//        if (!TfLiteVision.isInitialized()) {
//            val errorMessage = "TfLiteVision is not initialized yet"
//            Log.e(TAG, errorMessage)
//            detectorListener?.onError(errorMessage)
//            return
//        }

        if (objectDetector == null) setupObjectDetector()

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
            .add(CastOp(DataType.FLOAT32))
            .add(NormalizeOp(0f, 1f))
            .build()

        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, imageUri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        }.copy(Bitmap.Config.ARGB_8888, true)

        bitmap?.let {
            val tensorImage = imageProcessor.process(TensorImage.fromBitmap(it))
            val results = objectDetector?.detect(tensorImage)
            detectorListener?.onResult(results)
        } ?: run {
            detectorListener?.onError("Failed to decode bitmap from imageUri")
        }
    }

    interface  DetectorListener {
        fun onError(error: String)
        fun onResult(results: List<Detection>?)
    }

    companion object {
        private const val TAG = "ObjectDetectorHelper"
    }
}