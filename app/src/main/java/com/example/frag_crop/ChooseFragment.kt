package com.example.frag_crop

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.canhub.cropper.*
import com.example.frag_crop.databinding.FragmentChooseBinding

import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ChooseFragment : Fragment() {

    private var _binding: FragmentChooseBinding? = null
    private val binding get() = _binding!!

    private var outputUri: Uri? = null
    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            startCameraWithUri()
        } else {
            showErrorMessage("taking picture failed")
        }
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        when {
            result.isSuccessful -> {
                handleCropImageResult(result.uriContent.toString())
            }
            result is CropImage.CancelledResult -> showErrorMessage("cropping image was cancelled by the user")
            else -> showErrorMessage("cropping image failed")
        }
    }

    private val customCropImage = registerForActivityResult(CropImageContract()) {
        if (it !is CropImage.CancelledResult) {
            handleCropImageResult(it.uriContent.toString())
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//
//        binding.takePictureBeforeCallLibraryWithUri.setOnClickListener {
//            setupOutputUri()
//            takePicture.launch(outputUri)
//        }
//        binding.callLibraryWithoutUri.setOnClickListener {
//            startCameraWithoutUri(includeCamera = true, includeGallery = true)
//        }
        binding.callLibraryWithoutUriCameraOnly.setOnClickListener {
            startCameraWithoutUri(includeCamera = true, includeGallery = false)
        }
        binding.callLibraryWithoutUriGalleryOnly.setOnClickListener {
            startCameraWithoutUri(includeCamera = false, includeGallery = true)
        }
    }

    private fun startCameraWithoutUri(includeCamera: Boolean, includeGallery: Boolean) {
        customCropImage.launch(
            CropImageContractOptions(
                uri = null,
                cropImageOptions = CropImageOptions(
                    imageSourceIncludeCamera = includeCamera,
                    imageSourceIncludeGallery = includeGallery,
                ),
            ),
        )
    }

    private fun startCameraWithUri() {
        cropImage.launch(
            CropImageContractOptions(
                uri = outputUri,
                cropImageOptions = CropImageOptions(),
            ),
        )
    }

    private fun showErrorMessage(message: String) {

        Toast.makeText(activity, "Crop failed: $message", Toast.LENGTH_SHORT).show()
    }

    private fun handleCropImageResult(uri: String) {
        binding.cropedImage.setImageURI(Uri.parse(uri))

      //  SampleResultScreen.start(this, null, Uri.parse(uri.replace("file:", "")), null)
    }

    private fun setupOutputUri() {
        if (outputUri == null) {
            context?.let { ctx ->
                val authorities = "${ctx.applicationContext?.packageName}$AUTHORITY_SUFFIX"
                outputUri = FileProvider.getUriForFile(ctx, authorities, createImageFile())
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
        val storageDir: File? = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "$FILE_NAMING_PREFIX${timeStamp}$FILE_NAMING_SUFFIX",
            FILE_FORMAT,
            storageDir,
        )
    }

    companion object {
        const val DATE_FORMAT = "yyyyMMdd_HHmmss"
        const val FILE_NAMING_PREFIX = "PNG"
        const val FILE_NAMING_SUFFIX = "_"
        const val FILE_FORMAT = ".png"
        const val AUTHORITY_SUFFIX = ".cropper.fileprovider"
    }





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChooseBinding.inflate(inflater, container, false)

        return binding.root
    }




}


