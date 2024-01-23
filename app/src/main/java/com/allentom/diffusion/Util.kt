package com.allentom.diffusion

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Base64
import com.allentom.diffusion.store.Prompt
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
}