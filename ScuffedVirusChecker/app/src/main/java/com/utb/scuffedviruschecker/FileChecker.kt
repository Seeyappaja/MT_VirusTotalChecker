package com.utb.scuffedviruschecker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.utb.scuffedviruschecker.funs.FileViewModel
import com.utb.scuffedviruschecker.model.FileProperties
import com.utb.scuffedviruschecker.databinding.FragmentFileCheckerBinding

class FileChecker : Fragment() {

    private lateinit var fileViewModel: FileViewModel
    private var _binding: FragmentFileCheckerBinding? = null
    private val binding get() = _binding!!

    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    fileViewModel.openFile(uri)
                    binding.FilePath.text=uriToFilePath(uri)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFileCheckerBinding.inflate(inflater, container, false)
        val root = binding.root


        fileViewModel = ViewModelProvider(this).get(FileViewModel::class.java)

        binding.FileToCheck.setOnClickListener {
            openFilePicker()
        }

        binding.BackToMenu.setOnClickListener {
            findNavController().navigate(R.id.action_fileChecker_to_initfragment)
        }

        fileViewModel.fileProperties.observe(viewLifecycleOwner, Observer {
            updateFileProperties(it)
        })

        fileViewModel.lastFileProperties.observe(viewLifecycleOwner, Observer {
            updateFileProperties(it)
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun uriToFilePath(uri: Uri): String {
        var filePath = ""

        when (uri.scheme) {
            "file" -> filePath = uri.path ?: ""
            "content" -> {
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    inputStream?.close()
                    filePath = uri.toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        return filePath
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        filePickerLauncher.launch(intent)
    }

    private fun updateFileProperties(properties: FileProperties) {
        binding.FileProperities.text = """
            File Name: ${properties.fileName}
            File Size: ${properties.fileSize} bytes
            Last Modified: ${properties.lastModified}
        """.trimIndent()
    }
}
