package com.fransbudikashira.chefies.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.fransbudikashira.chefies.BuildConfig
import com.fransbudikashira.chefies.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss"
private val timeStamp: String = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(Date())

fun isValidEmail(email: String): Boolean {
    val emailRegex = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,})+\$")
    return email.matches(emailRegex)
}

fun getImageUri(context: Context): Uri {
    var uri: Uri? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$timeStamp.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/MyCamera/")
        }
        uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        // content://media/external/images/media/1000000062
        // storage/emulated/0/Pictures/MyCamera/20230825_155303.jpg
    }
    return uri ?: getImageUriForPreQ(context)
}

private fun getImageUriForPreQ(context: Context): Uri {
    val filesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val imageFile = File(filesDir, "/MyCamera/$timeStamp.jpg")
    if (imageFile.parentFile?.exists() == false) imageFile.parentFile?.mkdir()
    return FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.fileprovider",
        imageFile
    )
    //content://com.dicoding.picodiploma.mycamera.fileprovider/my_images/MyCamera/20230825_133659.jpg
}

fun createCustomTempFile(context: Context): File {
    val filesDir = context.externalCacheDir
    return File.createTempFile(timeStamp, ".jpg", filesDir)
}

fun String.prettierIngredientResult(context: Context): String {
    return when (this) {
        "banana" -> context.getString(R.string.banana)
        "banana_unripe" -> context.getString(R.string.banana)
        "egg" -> context.getString(R.string.egg)
        "red_id_onion" -> context.getString(R.string.shallot)
        "tomato" -> context.getString(R.string.tomato)
        "white_id_onion" -> context.getString(R.string.garlic)
        "white_id_onion_full" -> context.getString(R.string.garlic)
        else -> this
    }
}

fun getDefaultLanguage(): String = Locale.getDefault().language

suspend fun <T> LiveData<T>.await(): T {
    return suspendCancellableCoroutine { cont ->
        val observer = object : Observer<T> {
            override fun onChanged(value: T) {
                if (value != null) {
                    cont.resume(value)
                    this@await.removeObserver(this)
                } else {
                    cont.resumeWithException(Exception("No data available"))
                }
            }
        }
        observeForever(observer)
        cont.invokeOnCancellation { removeObserver(observer) }
    }
}
