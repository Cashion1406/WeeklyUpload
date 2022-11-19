package com.example.weeklyupload

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.util.Patterns
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.finalandroid.adapter.NetworkManagement.NetworkConnection
import com.example.weeklyupload.Object.Image
import com.example.weeklyupload.ViewModel.ImageViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.shashank.sony.fancytoastlib.FancyToast
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_image_dashboard.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class image_dashboard : Fragment() {


    private val REQUEST_CAPTURE_IMAGE = 200
    private val REQUEST_LOAD_GALLERY = 100
    private val REQUEST_DOWNLOAD = 300
    private lateinit var imageAdapter: imageAdapter
    private lateinit var imageViewModel: ImageViewModel

    private val REQUEST_CHECK_SETTING = 100
    private val UPDATE_INTERVAL: Long = 5000
    private val UPDATE_FASTEST: Long = 1000

    private lateinit var networkConnection: NetworkConnection
    private lateinit var latestImage: List<Image>

    private var fileCapture: File? = null
    private lateinit var pathImage: String

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mSettingClient: SettingsClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mLocationSettingRequest: LocationSettingsRequest? = null
    private var mLocationCallback: LocationCallback? = null
    private var mCurrentLocation: Location? = null
    private var geocoder: Geocoder? = null
    private var mRequestionLocationUPdate = false
    private var address: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        imageAdapter = imageAdapter()
        networkConnection = NetworkConnection(requireContext())

        imageViewModel = ViewModelProvider(this)[ImageViewModel::class.java]
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        mSettingClient = LocationServices.getSettingsClient(requireContext())


        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                mCurrentLocation = p0.lastLocation


                try {

                    networkConnection.observe(viewLifecycleOwner) { connection ->
                        if (connection) {
                            geocoder = Geocoder(requireContext(), Locale.getDefault())
                            val addresses: List<Address> = geocoder!!.getFromLocation(
                                mCurrentLocation!!.latitude,
                                mCurrentLocation!!.longitude, 1
                            )

                            val location =
                                addresses[0].countryName.toString() + "\n" + addresses[0].getAddressLine(
                                    0
                                )

                            address = location

                        } else {
                            imageViewModel.getlastedImage()
                                .observe(viewLifecycleOwner) { image ->
                                    latestImage = image


                                    for (e in latestImage) {

                                        address = if (e.location.contains("Last known at")) {

                                            e.location
                                        } else {
                                            "Last known at" + "\n" + e.location

                                        }

                                    }


                                }


                        }

                    }


                } catch (e: IOException) {

                    e.printStackTrace()
                    Log.i("CATN FETCH LOCATION", e.toString())
                }

            }
        }
        mLocationRequest =
            LocationRequest.create().setInterval(UPDATE_INTERVAL).setFastestInterval(UPDATE_FASTEST)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()

        builder.addLocationRequest(mLocationRequest!!)
        mLocationSettingRequest = builder.build()

        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    mRequestionLocationUPdate = true
                    Toast.makeText(requireContext(), "LOcation Start", Toast.LENGTH_SHORT).show()
                    startlocate()
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    if (p0 != null) {
                        if (p0.isPermanentlyDenied) {
                            openSetting()
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }
            }).check()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageDefult()

        ed_image_url.setOnKeyListener { v, keyCode, event ->
            when {
                ((keyCode == KeyEvent.KEYCODE_ENTER) && event.action == KeyEvent.ACTION_UP) -> {
                    addImage()
                    ed_image_url.clearFocus()
                    view.hideKeyboard()
                }
            }
            return@setOnKeyListener false
        }
        ed_image_url.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                image_url_holder.helperText = null
            }
        }
        ed_image_url.addTextChangedListener {

            if (ed_image_url.text!!.isEmpty()) {
                return@addTextChangedListener
            }
            fetchImage()

        }
        btn_add_image.setOnClickListener {
            image_url_holder.helperText = null
            ed_image_url.clearFocus()
            addImage()

        }


        btn_camera.setOnClickListener {
            openCamera()

        }
        btn_gallery.setOnClickListener {

            loadGallery()
        }
        btn_download.setOnClickListener {

            checkdownPer()
        }
        btn_clear.setOnClickListener {
            ed_image_url.clearFocus()
            image_url_holder.helperText = null
            ed_image_url.text?.clear()
            img_view.setImageDrawable(null)
            view.hideKeyboard()
            imageDefult()
        }

    }


    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun imageDefult() {
        if (img_view.drawable == null) {
            img_view.setImageResource(R.drawable.image_placeholder)
        }

    }

    private fun fetchImage(): Boolean {
        val image = ed_image_url.text.toString().trim { it <= ' ' }
        val isValid = URLUtil.isValidUrl(image) && Patterns.WEB_URL.matcher(image).matches()
        if (image.isEmpty()) {

            image_url_holder.helperText = "Empty image URL"
        } else if (!isValid) {
            image_url_holder.helperText = "INVALID URL"

        } else {
            Picasso.get().load(image).into(img_view)
        }
        return isValid
    }

    private fun addImage() {

        val image = ed_image_url.text.toString().trim { it <= ' ' }

        if (fetchImage()) {
            val img = Image(0, image, address.toString())
            imageViewModel.addimage(img)
            FancyToast.makeText(
                requireContext(),
                "Added Image",
                FancyToast.LENGTH_SHORT,
                FancyToast.SUCCESS,
                false
            ).show()
        }
    }

    private fun addImagefromGallery(image: Image) {
        imageViewModel.addimage(image)
        FancyToast.makeText(
            requireContext(),
            "Added Image from Gallery",
            FancyToast.LENGTH_SHORT,
            FancyToast.SUCCESS,
            false
        ).show()
    }


    private fun checkdownPer() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), REQUEST_DOWNLOAD
                )
                startDownload()
            }

        } catch (e: Exception) {

            e.printStackTrace()
            image_url_holder.helperText = e.message.toString()
        }
    }

    private fun startDownload() {
        val url = ed_image_url.text.toString()

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Download")
            .setDescription("Downloading..")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                System.currentTimeMillis().toString()
            )

        val downloadManager: DownloadManager =
            requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        if (downloadManager != null) {
            val img = Image(0, url, address!!)
            imageViewModel.addimage(img)

            downloadManager.enqueue(request)
        }
    }

    private fun loadGallery() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), REQUEST_LOAD_GALLERY
                )
                loadGallery()
            } else {
                val galleryIntent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                startActivityForResult(galleryIntent, REQUEST_LOAD_GALLERY);
            }

        } catch (e: Exception) {

            e.printStackTrace()
            image_url_holder.helperText = e.message.toString()
        }
    }


    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (intent.resolveActivity(requireContext().packageManager) != null) {
            try {
                fileCapture = createImageFile();
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (fileCapture != null) {
                val photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.weeklyupload",
                    fileCapture!!
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, REQUEST_CAPTURE_IMAGE)
            }
        }
    }

    private fun createImageFile(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "PNG_" + timeStamp + "_"
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".png",  /* suffix */
            storageDir   /* directory */
        )
        pathImage = image.absolutePath
        return image
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CAPTURE_IMAGE ->
                if (resultCode == Activity.RESULT_OK) {
                    if (address == null || address!!.isEmpty()) {

                        address = "N/A"
                    }
                    val img = Image(0, "file://$pathImage", address!!)

                    img_view.setImageURI(Uri.parse(pathImage))

                    imageViewModel.addimage(img)
                } else if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == Activity.RESULT_CANCELED) {

                    fileCapture?.delete()

                    /*Toast.makeText(requireContext(), fileCapture.toString(), Toast.LENGTH_LONG)
                        .show()*/

                }

            REQUEST_LOAD_GALLERY ->
                if (resultCode == Activity.RESULT_OK) {
                    val imageSelected: Uri? = data?.data
                    val filePath = arrayOf(MediaStore.Images.Media.DATA)
                    val cursor: Cursor? =
                        requireContext().contentResolver.query(
                            imageSelected!!,
                            filePath,
                            null,
                            null,
                            null
                        )
                    cursor!!.moveToFirst()
                    val columnIndex = cursor.getColumnIndex(filePath[0])
                    val imagePath = cursor.getString(columnIndex)

                    pathImage = imagePath
                    cursor.close()
                    val img = Image(0, "file://$pathImage", address!!)
                    btn_add_image.setOnLongClickListener {

                        addImagefromGallery(img)
                        return@setOnLongClickListener true
                    }


                    /*  Toast.makeText(requireContext(), pathImage, Toast.LENGTH_SHORT)
                          .show()*/

                    img_view.setImageURI(Uri.parse(pathImage))
                }
            REQUEST_DOWNLOAD ->
                startDownload()
        }

    }

    fun confirDialog(image: Image) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Add selected Image to Image List ?")
        builder.setTitle("Selected Image")
        builder.setIcon(R.drawable.ic_launcher_foreground)

        builder.setPositiveButton("Yes") { dialogInterface, which ->
            imageViewModel.addimage(image)
            dialogInterface.dismiss()
        }
        builder.setNegativeButton("No") { lmao, which ->
            lmao.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }


    fun openSetting() {

        val intent = Intent().setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)

    }

    fun startlocate() {
        mLocationSettingRequest?.let {
            mSettingClient?.checkLocationSettings(it)?.addOnSuccessListener {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                    return@addOnSuccessListener
                }
                mFusedLocationClient.requestLocationUpdates(
                    mLocationRequest!!, mLocationCallback!!,
                    Looper.getMainLooper()
                )
            }
                ?.addOnFailureListener {

                    val status: Int = (it as ApiException).statusCode

                    when (status) {

                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                            try {
                                val rea = it as ResolvableApiException
                                rea.startResolutionForResult(
                                    requireActivity(),
                                    REQUEST_CHECK_SETTING
                                )
                            } catch (e: IntentSender.SendIntentException) {
                                Toast.makeText(
                                    requireContext(),
                                    "PENDING REQUEST CANT EXECUTE",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->
                            Toast.makeText(
                                requireContext(),
                                "LOCATION SETTING BROKEN",
                                Toast.LENGTH_SHORT
                            ).show()
                    }
                }
        }
    }

    fun stoplocate() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback!!)
        //Toast.makeText(requireContext(), "LOcation Stop", Toast.LENGTH_SHORT).show()
    }

    fun checkPER(): Boolean {

        val permissionState = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    override fun onResume() {
        super.onResume()
        if (mRequestionLocationUPdate && checkPER()) {
            startlocate()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mRequestionLocationUPdate) {

            stoplocate()
        }
    }

}