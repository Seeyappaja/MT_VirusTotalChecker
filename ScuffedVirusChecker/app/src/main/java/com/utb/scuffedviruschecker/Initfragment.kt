package com.utb.scuffedviruschecker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.utb.scuffedviruschecker.databinding.FragmentInitfragmentBinding
import com.utb.scuffedviruschecker.funs.FileViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Initfragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Initfragment : Fragment() {
    private lateinit var fileViewModel: FileViewModel
    private var _binding: FragmentInitfragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInitfragmentBinding.inflate(inflater, container, false)
        val root = binding.root

        binding.FileChecker.setOnClickListener {
            findNavController().navigate(R.id.action_initfragment_to_fileChecker)
        }

        binding.VScheck.setOnClickListener {
            findNavController().navigate(R.id.action_initfragment_to_virusTotalCheck)
        }

        return root
    }
}