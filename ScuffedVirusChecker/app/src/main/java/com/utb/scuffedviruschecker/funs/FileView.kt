package com.utb.scuffedviruschecker.funs
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import android.net.Uri
import androidx.lifecycle.LiveData
import com.utb.scuffedviruschecker.model.FileProperties
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



class FileViewModel(application: Application) : AndroidViewModel(application) {
    val fileProperties = MutableLiveData<FileProperties>()
    private val sharedPreferences =
        application.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

    private val _lastFileProperties = MutableLiveData<FileProperties>()
    val lastFileProperties: LiveData<FileProperties> get() = _lastFileProperties

    init {
        // Load last file properties when the FileViewModel is created
        loadLastFileProperties()
    }

    fun openFile(uri: Uri?) {
        uri?.let {
            val contentResolver = getApplication<Application>().contentResolver
            val cursor = contentResolver.query(uri, null, null, null, null)

            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val displayName = c.getString(c.getColumnIndexOrThrow("_display_name"))
                    val size = c.getLong(c.getColumnIndexOrThrow("_size"))
                    val lastModifiedUnix = c.getLong(c.getColumnIndexOrThrow("last_modified"))

                    val lastModifiedHumanReadable = convertUnixTimeToHumanReadable(lastModifiedUnix)

                    val fileProperties = FileProperties(displayName, size, lastModifiedHumanReadable)
                    this.fileProperties.value = fileProperties
                    saveLastFileProperties(fileProperties)
                }
            }

        }
    }

    private fun saveLastFileProperties(fileProperties: FileProperties) {
        with(sharedPreferences.edit()) {
            putString("last_display_name", fileProperties.fileName)
            putLong("last_size", fileProperties.fileSize)
            putString("last_last_modified", fileProperties.lastModified)
            apply()
        }
    }

    private fun loadLastFileProperties() {
        val lastDisplayName = sharedPreferences.getString("last_display_name", "")
        val lastSize = sharedPreferences.getLong("last_size", 0)
        val lastLastModifiedUnix = sharedPreferences.getString("last_last_modified", "")

        if (!lastDisplayName.isNullOrBlank() && !lastLastModifiedUnix.isNullOrBlank()) {
            _lastFileProperties.value =
                FileProperties(lastDisplayName, lastSize, lastLastModifiedUnix)
        }
    }

    private fun convertUnixTimeToHumanReadable(unixTime: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = Date(unixTime) // Assuming it's in milliseconds
        return dateFormat.format(date)
    }


}

