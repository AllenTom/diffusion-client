package com.allentom.diffusion

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Base64
import androidx.compose.ui.graphics.asImageBitmap
import com.allentom.diffusion.store.Prompt
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.security.MessageDigest
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.TimeUnit

object Util {
    fun getTimeDeltaString(unixTime: Long): String {
        val now = Calendar.getInstance().timeInMillis
        val duration = now - unixTime * 1000 // convert seconds to milliseconds

        val days = TimeUnit.MILLISECONDS.toDays(duration)
        val hours = TimeUnit.MILLISECONDS.toHours(duration) - TimeUnit.DAYS.toHours(days)
        val minutes =
            TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.DAYS.toMinutes(days) - TimeUnit.HOURS.toMinutes(
                hours
            )

        return when {
            days > 0 -> "${days}d ${hours}h ago"
            hours > 0 -> "${hours}h ${minutes}m ago"
            else -> "${minutes}m ago"
        }
    }

    fun formatUnixTime(unixTime: Long): String {
        val timeFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        return timeFormat.format(unixTime)
    }

    fun getMd5FromImageBase64(imageBase64: String): String {
        val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
        val md = MessageDigest.getInstance("MD5")
        val md5 = md.digest(imageBytes)
        return md5.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun saveImageBase64ToAppData(context: Context, imageBase64: String, imageName: String): String {
        val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
        val fileName = "${imageName}.png"
        val saveDir = File(context.filesDir, "gen_images")
        if (!saveDir.exists()) {
            saveDir.mkdir()
        }
        val file = File(saveDir, fileName)
        file.writeBytes(imageBytes)
        return file.absolutePath
    }

    fun saveControlNetToAppData(context: Context, imageUri: Uri): String {
        // Read the image file and convert it to a byte array
        val inputStream: InputStream = context.contentResolver.openInputStream(imageUri)!!
        val imageBytes = inputStream.readBytes()
        val ext = imageUri.toString().substring(imageUri.toString().lastIndexOf(".") + 1)
        // Generate a unique name for the image
        val uuid = UUID.randomUUID().toString()
        val fileName = "${uuid}.${ext}"

        // Create the directory if it doesn't exist
        val saveDir = File(context.filesDir, "control_net")
        if (!saveDir.exists()) {
            saveDir.mkdir()
        }

        // Save the image to the application's data directory
        val file = File(saveDir, fileName)
        file.writeBytes(imageBytes)
        // Return the absolute path of the saved image
        return file.absolutePath
    }

    fun saveControlNetPreviewToAppData(
        context: Context,
        imagePath: String,
        controlNetMd5: String
    ): String {
        val file = File(imagePath)
        val imageBytes = file.readBytes()
        val fileName = "${controlNetMd5}.png"
        val saveDir = File(context.filesDir, "control_net_preview")
        if (!saveDir.exists()) {
            saveDir.mkdir()
        }
        val saveFile = File(saveDir, fileName)
        saveFile.writeBytes(imageBytes)
        return saveFile.absolutePath
    }

    fun saveControlNetPreviewToAppData(
        context: Context,
        imageUri: Uri,
        controlNetMd5: String
    ): String {
        val inputStream: InputStream = context.contentResolver.openInputStream(imageUri)!!
        val imageBytes = inputStream.readBytes()
        val fileName = "${controlNetMd5}.png"
        val saveDir = File(context.filesDir, "control_net_preview")
        if (!saveDir.exists()) {
            saveDir.mkdir()
        }
        val saveFile = File(saveDir, fileName)
        saveFile.writeBytes(imageBytes)
        return saveFile.absolutePath
    }

    fun saveLoraPreviewToAppData(
        context: Context,
        imagePath: String,
        loraId: Long
    ): String {
        val file = File(imagePath)
        val imageBytes = file.readBytes()
        val fileName = "${loraId}.png"
        val saveDir = File(context.filesDir, "lora_preview")
        if (!saveDir.exists()) {
            saveDir.mkdir()
        }
        val saveFile = File(saveDir, fileName)
        saveFile.writeBytes(imageBytes)
        return saveFile.absolutePath
    }

    fun saveModelPreviewToAppData(
        context: Context,
        imagePath: String,
        modelName: String,
    ): String {
        val file = File(imagePath)
        val imageBytes = file.readBytes()
        val fileName = "${getHashFromString(modelName)}.png"
        val saveDir = File(context.filesDir, "model_preview")
        if (!saveDir.exists()) {
            saveDir.mkdir()
        }
        val saveFile = File(saveDir, fileName)
        saveFile.writeBytes(imageBytes)
        return saveFile.absolutePath
    }


    fun saveControlNetImageBase64ToAppData(context: Context, imageBase64: String): String {
        // Decode the base64 string to get the image bytes
        val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)

        // Generate a unique name for the image
        val uuid = UUID.randomUUID().toString()
        val fileName = "$uuid.png"

        // Create the directory if it doesn't exist
        val saveDir = File(context.filesDir, "control_net")
        if (!saveDir.exists()) {
            saveDir.mkdir()
        }

        // Write the image bytes to a new file in the "control_net" directory
        val file = File(saveDir, fileName)
        file.writeBytes(imageBytes)

        // Return the absolute path of the saved image
        return file.absolutePath
    }

    fun saveImg2ImgFile(context: Context, imgBase64: String, fileName: String): String {
        val imageBytes = Base64.decode(imgBase64, Base64.DEFAULT)
        val uuid = UUID.randomUUID().toString()
        val ext = fileName.substring(fileName.lastIndexOf(".") + 1)
        val saveFilename = "${uuid}.${ext}"
        val saveDir = File(context.filesDir, "gen_images")
        if (!saveDir.exists()) {
            saveDir.mkdir()
        }
        val file = File(saveDir, saveFilename)
        file.writeBytes(imageBytes)
        return file.absolutePath
    }

    fun saveImg2ImgMaskFile(context: Context, imgBase64: String, fileName: String): String {
        val imageBytes = Base64.decode(imgBase64, Base64.DEFAULT)
        val uuid = UUID.randomUUID().toString()
        val ext = fileName.substring(fileName.lastIndexOf(".") + 1)
        val saveFilename = "${uuid}.${ext}"
        val saveDir = File(context.filesDir, "inpaint_mask")
        if (!saveDir.exists()) {
            saveDir.mkdir()
        }
        val file = File(saveDir, saveFilename)
        file.writeBytes(imageBytes)
        return file.absolutePath
    }

    fun saveReactorSourceFile(
        context: Context,
        imgBase64: String,
        fileName: String
    ): Pair<String, String> {
        val imageBytes = Base64.decode(imgBase64, Base64.DEFAULT)
        val uuid = UUID.randomUUID().toString()
        val ext = fileName.substring(fileName.lastIndexOf(".") + 1)
        val saveFilename = "${uuid}.${ext}"
        val saveDir = File(context.filesDir, "reactor_source")
        if (!saveDir.exists()) {
            saveDir.mkdir()
        }
        val file = File(saveDir, saveFilename)
        file.writeBytes(imageBytes)
        return Pair(file.absolutePath, saveFilename)
    }


    fun copyImageFileToGallery(context: Context, imagePath: String, fileName: String) {
        val file = File(imagePath)
        if (!file.exists()) {
            // The file does not exist
            return
        }

        val picturesDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val diffusionDirectory = File(picturesDirectory, "Diffusion")
        if (!diffusionDirectory.exists()) {
            diffusionDirectory.mkdir()
        }
        val newFile = File(diffusionDirectory, fileName)

        val inputStream = FileInputStream(file)
        val outputStream: OutputStream = FileOutputStream(newFile)

        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
    }

    fun saveImageBase64ToGallery(
        imageBase64: String,
        fileName: String,
        folderName: String = "Diffusion"
    ) {
        val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
        val picturesDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val diffusionDirectory = File(picturesDirectory, folderName)
        if (!diffusionDirectory.exists()) {
            diffusionDirectory.mkdir()
        }
        val newFile = File(diffusionDirectory, fileName)
        val outputStream: OutputStream = FileOutputStream(newFile)
        outputStream.write(imageBytes)
        outputStream.close()
    }

    fun downloadImage(url: String): ByteArray {
        val connection = URL(url).openConnection()
        connection.connect()
        val inputStream = connection.getInputStream()
        return inputStream.readBytes()
    }

    fun saveLoraImagesFromUrls(context: Context, imageUrls: List<String>): List<String> {
        val imagePaths = mutableListOf<String>()
        imageUrls.forEach {
            val imageBytes = downloadImage(it)
            val uuid = UUID.randomUUID().toString()
            val fileName = "$uuid.png"
            val saveDir = File(context.filesDir, "lora_images")
            if (!saveDir.exists()) {
                saveDir.mkdir()
            }
            val file = File(saveDir, fileName)
            file.writeBytes(imageBytes)
            imagePaths.add(file.absolutePath)
        }
        return imagePaths
    }

    fun saveModelImagesFromUrls(context: Context, imageUrls: List<String>): List<String> {
        val imagePaths = mutableListOf<String>()
        imageUrls.forEach {
            val imageBytes = downloadImage(it)
            val uuid = UUID.randomUUID().toString()
            val fileName = "$uuid.png"
            val saveDir = File(context.filesDir, "model_images")
            if (!saveDir.exists()) {
                saveDir.mkdir()
            }
            val file = File(saveDir, fileName)
            file.writeBytes(imageBytes)
            imagePaths.add(file.absolutePath)
        }
        return imagePaths
    }

    fun convertImageToBase64(imageUri: Uri, context: Context): String {
        val inputStream: InputStream = context.contentResolver.openInputStream(imageUri)!!
        val bytes = inputStream.readBytes()
        val base64 = Base64.encode(bytes, Base64.DEFAULT).toString(Charsets.UTF_8)
        return base64
    }

    fun readImageWithPathToBase64(imagePath: String): String {
        val file = File(imagePath)
        val bytes = file.readBytes()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    fun convertImageToBase64(imageFilePath: String, context: Context): String {
        val file = File(imageFilePath)
        val bytes = file.readBytes()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    fun isValidateUrl(url: String): Boolean {
        val urlRegex =
            "^(http://www.|https://www.|http://|https://)?[a-z0-9]+([-.]{1}[a-z0-9]+)*.[a-z]{2,5}(:[0-9]{1,5})?(/.*)?$".toRegex()
        return urlRegex.matches(url)
    }

    fun getHashFromString(str: String): String {
        val md = MessageDigest.getInstance("MD5")
        val md5 = md.digest(str.toByteArray())
        return md5.fold("") { str, it -> str + "%02x".format(it) }
    }


    fun parsePrompt(input: String): Prompt {
        var parseText = input.trim()
        val stack = mutableListOf<Char>()
        var piority = 0
        parseText.forEach {
            if (it == '(') {
                stack.add(it)
            } else if (it == ')') {
                if (stack.isNotEmpty()) {
                    stack.removeLast()
                    piority++
                }
            }
        }
        if (stack.isNotEmpty()) {
            piority = 0
        }
        parseText = parseText.replace("(", "").replace(")", "")
        if (parseText.split(":").size == 2) {
            parseText = parseText.split(":")[0]
        }
        return Prompt(text = parseText, piority = piority)
    }

    fun randomColor(): Int {
        val random = java.util.Random()
        return android.graphics.Color.argb(
            255,
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        )
    }

    fun randomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    fun getDimensionsFromBase64(base64String: String): Pair<Int, Int> {
        val decodedString = Base64.decode(base64String, Base64.DEFAULT)
        val decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        val imageBitmap = decodedBitmap.asImageBitmap()
        return Pair(imageBitmap.width, imageBitmap.height)
    }

    fun calculateActualSize(
        containerWidth: Int,
        containerHeight: Int,
        imageWidth: Int,
        imageHeight: Int
    ): Pair<Int, Int> {
        val containerRatio = containerWidth.toFloat() / containerHeight.toFloat()
        val imageRatio = imageWidth.toFloat() / imageHeight.toFloat()

        return if (containerRatio > imageRatio) {
            // Container is wider than the image relative to their heights, so height will be the limiting factor
            val actualWidth = (containerHeight * imageRatio).toInt()
            Pair(actualWidth, containerHeight)
        } else {
            // Container is taller than the image relative to their widths, so width will be the limiting factor
            val actualHeight = (containerWidth / imageRatio).toInt()
            Pair(containerWidth, actualHeight)
        }
    }

    fun combineBase64Images(base64Image1: String, base64Image2: String): String {
        // Decode the Base64 strings to Bitmaps
        val decodedBytes1 = Base64.decode(base64Image1, Base64.DEFAULT)
        val bitmap1 = BitmapFactory.decodeByteArray(decodedBytes1, 0, decodedBytes1.size)

        val decodedBytes2 = Base64.decode(base64Image2, Base64.DEFAULT)
        val bitmap2 = BitmapFactory.decodeByteArray(decodedBytes2, 0, decodedBytes2.size)

        // Create a new Bitmap that can contain both Bitmaps
        val width = bitmap1.width
        val height = bitmap1.height
        val combinedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Draw the Bitmaps onto the new Bitmap
        val canvas = Canvas(combinedBitmap)
        canvas.drawBitmap(bitmap1, 0f, 0f, null)
        canvas.drawBitmap(bitmap2, 0f, 0f, null)

        // Convert the new Bitmap back to a Base64 string
        val outputStream = ByteArrayOutputStream()
        combinedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val combinedBytes = outputStream.toByteArray()
        return Base64.encodeToString(combinedBytes, Base64.DEFAULT)
    }

    fun combineImagePaths(imagePath1: String, imagePath2: String): String {
        // Read the images and convert them to Bitmaps
        val bitmap1 = BitmapFactory.decodeFile(imagePath1)
        val bitmap2 = BitmapFactory.decodeFile(imagePath2)

        // Create a new Bitmap that can contain both Bitmaps
        val width = bitmap1.width
        val height = bitmap1.height
        val combinedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Draw the Bitmaps onto the new Bitmap
        val canvas = Canvas(combinedBitmap)
        canvas.drawBitmap(bitmap1, 0f, 0f, null)
        canvas.drawBitmap(bitmap2, 0f, 0f, null)

        // Convert the new Bitmap back to a Base64 string
        val outputStream = ByteArrayOutputStream()
        combinedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val combinedBytes = outputStream.toByteArray()
        return Base64.encodeToString(combinedBytes, Base64.DEFAULT)
    }

    fun generateDataImageString(fileName: String, base64String: String): String {
        val fileExtension = fileName.substringAfterLast(".", "")
        val mimeType = when (fileExtension.lowercase().trim()) {
            "jpg", "jpeg" -> "jpeg"
            "png" -> "png"
            "gif" -> "gif"
            else -> "jpeg" // Default to jpeg if the file extension is not recognized
        }
        return "data:image/$mimeType;base64,$base64String"
    }

//    fun getRealFileNameFromUri(context: Context, uri: Uri): String? {
//        var result: String? = null
//        if (uri.scheme == "content") {
//            val cursor = context.contentResolver.query(uri, null, null, null, null)
//            try {
//                if (cursor != null && cursor.moveToFirst()) {
//                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//                    result = cursor.getString()
//                }
//            } finally {
//                cursor?.close()
//            }
//        }
//        if (result == null) {
//            result = uri.path
//            val cut = result?.lastIndexOf('/')
//            if (cut != -1) {
//                result = result?.substring(cut + 1)
//            }
//        }
//        return result
//    }

}