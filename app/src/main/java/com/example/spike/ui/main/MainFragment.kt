package com.example.spike.ui.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.spike.R
import com.example.spike.databinding.FragmentMainBinding
import com.google.android.material.snackbar.Snackbar

class MainFragment : Fragment(R.layout.fragment_main) {

    companion object {
        fun newInstance() = MainFragment()
    }

    private var _binding: FragmentMainBinding? = null

    private val binding get() = _binding!!

    private lateinit var viewModel: MainViewModel

//    private val pickSingleMediaLauncher =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//            if (it.resultCode != Activity.RESULT_OK) {
//                Toast.makeText(requireContext(), "Failed picking media.", Toast.LENGTH_SHORT)
//                    .show()
//            } else {
//                val uri = it.data?.data
//                showSnackBar("SUCCESS: ここで画像選択後の処理を書く")
//            }
//        }
//
//    private val requestPermissionLauncher =
//        registerForActivityResult(
//            ActivityResultContracts.RequestPermission()
//        ) { isGranted: Boolean ->
//            if (isGranted) {
//                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                intent.addCategory(Intent.CATEGORY_OPENABLE)
//                intent.putExtra(
//                    Intent.EXTRA_MIME_TYPES,
//                    arrayOf("image/jpeg", "image/png", "image/gif")
//                )
//                intent.type = "*/*"
//                pickSingleMediaLauncher.launch(intent)
//            } else {
//                Toast.makeText(requireContext(), "許可されなかった", Toast.LENGTH_SHORT)
//                    .show()
//            }
//        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = DataBindingUtil.bind(view)
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        val chooser = ImageChooser(
            fragment = this,
            onClickTargetView = binding.buttonPickPhoto,
        )

        chooser.setUp(
            wasAllow = {
                Toast.makeText(requireContext(), "許可されなかった", Toast.LENGTH_SHORT)
                    .show()
            },
            representationReason = {
                Toast.makeText(requireContext(), "許可したらいいことあるよー", Toast.LENGTH_SHORT)
                    .show()
            },
            chooseImageFailure = {
                Toast.makeText(requireContext(), "選択失敗したよー", Toast.LENGTH_SHORT)
                    .show()
            }
        ) { uri ->
            showSnackBar("SUCCESS: ここで画像選択後の処理を書く")
        }

//        binding.buttonPickPhoto.setOnClickListener {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                pickSingleMediaLauncher.launch(
//                    Intent(MediaStore.ACTION_PICK_IMAGES).apply {
//                        type = "image/*"
//                    }
//                )
//            } else {
//                when {
//                    shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
//                        Toast.makeText(requireContext(), "許可したらいいことあるよー", Toast.LENGTH_SHORT)
//                            .show()
//                    }
//                    else -> {
//                        requestPermissionLauncher.launch(
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE
//                        )
//                    }
//                }
//            }
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Setup max pick medias
            val maxPickMedia = MediaStore.getPickImagesMaxLimit()
            binding.textMackPickMedia.text = "Max Pick Media: $maxPickMedia"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Shows [message] in a [Snackbar].
     */
    private fun showSnackBar(message: String) {
        val snackBar = Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG,
        )
        // Set the max lines of SnackBar
        snackBar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).maxLines =
            10
        snackBar.show()
    }
}

class ImageChooser(
    private val fragment: Fragment,
    private val onClickTargetView: View,
) {

    fun setUp(
        wasAllow: (() -> Unit)? = null,
        representationReason: (() -> Unit)? = null,
        chooseImageFailure: (() -> Unit)? = null,
        chooseImageSuccess: (Uri) -> Unit
    ) {
        val pickSingleMediaLauncher =
            fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode != Activity.RESULT_OK) {
                    chooseImageFailure?.invoke()
                } else {
                    val uri = it.data?.data
                    if (uri == null) {
                        chooseImageFailure?.invoke()
                    } else {
                        chooseImageSuccess(uri)
                    }
                }
            }

        val requestPermissionLauncher =
            fragment.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.putExtra(
                        Intent.EXTRA_MIME_TYPES,
                        arrayOf("image/jpeg", "image/png", "image/gif")
                    )
                    intent.type = "*/*"
                    pickSingleMediaLauncher.launch(intent)
                } else {
                    wasAllow?.invoke()
                }
            }

        onClickTargetView.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pickSingleMediaLauncher.launch(
                    Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                        type = "image/*"
                    }
                )
            } else {
                when {
                    fragment.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                        representationReason?.invoke()
                    }
                    else -> {
                        requestPermissionLauncher.launch(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    }
                }
            }
        }
    }
}