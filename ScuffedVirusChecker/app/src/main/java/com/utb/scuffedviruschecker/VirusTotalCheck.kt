package com.utb.scuffedviruschecker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.utb.scuffedviruschecker.databinding.FragmentVirusTotalCheckBinding
import com.utb.scuffedviruschecker.funs.GetFileinformation
import com.utb.scuffedviruschecker.funs.VirusTotalApiService
import com.utb.scuffedviruschecker.model.FileInfoData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileInputStream
import java.security.DigestInputStream
import java.security.MessageDigest

class VirusTotalCheck : Fragment() {
    private var _binding: FragmentVirusTotalCheckBinding? = null
    private val binding get() = _binding!!
    private var fP = ""
    private var GETid = ""
    private var md5Sum = ""
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

        binding.CheckResult.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                // Call the scanFileOnVirusTotal function
                Log.d("Start of Result Check", "TEST")
                val fileInfo = getFileInformation(md5Sum)
                Log.d("FILE INFO", "$fileInfo")
                fileInfo?.let {
                    // Update your UI here with file information
                    // For example, set it in a TextView
                    val result = it.attributes.last_analysis_results["Bkav"]
                    binding.VirusTotalText.text = "Bkav Result: ${result?.category}, Engine Version: ${result?.engine_version}"
                }
            }
        }

        binding.BackToMM.setOnClickListener {
            findNavController().navigate(R.id.action_virusTotalCheck_to_initfragment)
        }

        binding.VirusTotalText.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                // Call the scanFileOnVirusTotal function
                GETid = scanFileOnVirusTotal(fP)
                md5Sum = GETid
                binding.VirusTotalText.text = GETid
            }
        }

        return root
    }

    private suspend fun getFileInformation(fileId: String): FileInfoData? {
        try {
            val response = getFileInformationApiService.getFileInformation(fileId)
            Log.d("GET RESPONSE", "$response")
            if (response.isSuccessful) {
                return response.body()?.data
            } else {
                // Handle unsuccessful response
                return null
            }
        } catch (e: Exception) {
            // Handle exceptions, e.g., network errors
            Log.e("VirusTotalCheck", "Exception during file report", e)
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
            Log.d("POST", "$scanId")
            val md5Sum = calculateMD5(file)
            return md5Sum
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

    private fun calculateMD5(file: File): String {
        val digest = MessageDigest.getInstance("MD5")

        FileInputStream(file).use { fileInputStream ->
            DigestInputStream(fileInputStream, digest).use { digestInputStream ->
                // Read the file in chunks of 4K
                val buffer = ByteArray(4 * 1024)
                while (digestInputStream.read(buffer) != -1) {
                    // Update the digest with the bytes read from the file
                    // This is done automatically as data is read through DigestInputStream
                }
            }
        }

        // Get the MD5 hash as a byte array
        val hashBytes = digest.digest()

        // Convert the byte array to a hexadecimal string
        val hexString = StringBuilder()
        for (byte in hashBytes) {
            // Convert each byte to a two-character hexadecimal representation
            hexString.append(String.format("%02x", byte))
        }

        return hexString.toString()
    }
}
