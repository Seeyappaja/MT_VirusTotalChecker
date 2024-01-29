package com.utb.scuffedviruschecker
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.utb.scuffedviruschecker.databinding.FragmentVirusTotalCheckBinding
import com.utb.scuffedviruschecker.funs.GetFileinformation
import com.utb.scuffedviruschecker.funs.VirusTotalApiService
import com.utb.scuffedviruschecker.model.GetResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class VirusTotalCheck : Fragment() {
    private var _binding: FragmentVirusTotalCheckBinding? = null
    private val binding get() = _binding!!
    private var fP = ""
    private var GETid = ""
    private val gson = Gson() // Gson instance


    private val virusTotalApiService: VirusTotalApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.virustotal.com/api/v3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VirusTotalApiService::class.java)
    }

    private val getFileInformationApiService: GetFileinformation by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.virustotal.com/api/v3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GetFileinformation::class.java)
    }

    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    fP = downloadFile(uri)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVirusTotalCheckBinding.inflate(inflater, container, false)
        val root = binding.root

        binding.CheckForVirus.setOnClickListener {
            openFilePicker()
        }

        binding.BackToMM.setOnClickListener {
            findNavController().navigate(R.id.action_virusTotalCheck_to_initfragment)
        }

        binding.VirusTotalText.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                // Call the scanFileOnVirusTotal function
                GETid = scanFileOnVirusTotal(fP)
                binding.VirusTotalText.text = GETid
                // Call the getFileInformation function and update the UI accordingly
                val fileInfo = getFileInformation(GETid)
                fileInfo?.let {
                    // Update your UI here with file information
                    // For example, set it in a TextView
                    binding.VirusTotalText.text = "File Information: $it"
                }
            }
        }

        return root
    }

    private suspend fun getFileInformation(fileId: String): GetResults? {
        try {
            val response = getFileInformationApiService.getFileInformation(fileId)
            Log.d("GET RESPONSE", "$response")
            if (response.isSuccessful) {
                // Parse the response using Gson
                return gson.fromJson(response.body()?.data, GetResults::class.java)
            } else {
                // Handle unsuccessful response
                return null
            }
        } catch (e: Exception) {
            // Handle exceptions, e.g., network errors
            return null
        }
    }


    suspend fun scanFileOnVirusTotal(filePath: String): String {
        val file = File(filePath)
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        try {
            val response = virusTotalApiService.scanFile(body)
            val scanId = response.body()?.data?.id
            return scanId ?: ""
        } catch (e: Exception) {
            Log.e("VirusTotalCheck", "Exception during file scan", e)
        }
        return ""
    }

    private fun downloadFile(uri: Uri): String {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val fileName = "downloaded_file"
            val outputStream = requireContext().openFileOutput(fileName, Context.MODE_PRIVATE)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            return requireContext().getFileStreamPath(fileName).absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        filePickerLauncher.launch(intent)
    }
}
