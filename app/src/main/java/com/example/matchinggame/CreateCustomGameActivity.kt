package com.example.matchinggame

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.matchinggame.adapter.ImagePickerAdapter
import com.example.matchinggame.models.BoardSize
import com.example.matchinggame.models.UserImageList
import com.example.matchinggame.utils.BitmapScaler
import com.example.matchinggame.utils.EXTRA_BOARD_SIZE
import com.example.matchinggame.utils.EXTRA_GAME_NAME
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_create_custom_game.*
import java.io.ByteArrayOutputStream

class CreateCustomGameActivity : AppCompatActivity() {
    private lateinit var boardSize: BoardSize
    private val imageUris = mutableListOf<Uri>()
    private lateinit var adapter: ImagePickerAdapter
    private val storage=Firebase.storage
    private val db=Firebase.firestore
    private val auth=Firebase.auth
    companion object{
        private const val TAG="CreateCustomGameActivity"
        private const val MY_PERMISSIONS_REQUEST_GALLERY=99
        private const val PICK_PHOTO_CODE=98
        private const val MIN_GAME_NAME_LENGTH=4
        private const val MAX_GAME_NAME_LENGTH=14
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_custom_game)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        boardSize=intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize
        // getNumPairs returns num of pairs so that we can get num of images required to create custom board
        supportActionBar?.title="Choose pics (0 / ${boardSize.getNumPairs()})"

        nameEditText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                saveBtn.isEnabled=shouldEnableSaveBtn()
            }
        })
        nameEditText.filters=arrayOf(InputFilter.LengthFilter(MAX_GAME_NAME_LENGTH))

        adapter= ImagePickerAdapter(this, imageUris, boardSize, createImageClickListener(), createRemoveClickListener())
        rvImagePicker.adapter=adapter
        rvImagePicker.layoutManager=GridLayoutManager(this,boardSize.getWidth())
        rvImagePicker.setHasFixedSize(true)

        saveBtn.setOnClickListener {
            saveDataToFirebase()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createImageClickListener() = object: ImagePickerAdapter.OnImageClickListener{
        override fun onPlaceHolderClicked() {
            if(ContextCompat.checkSelfPermission(
                    this@CreateCustomGameActivity,android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            )
                launchIntentForPhotos()
            else
                checkPermission()
        }
    }

    private fun createRemoveClickListener() = object : ImagePickerAdapter.OnRemoveClickListener{
        override fun onRemoveClicked(position: Int) {
            imageUris.removeAt(position)
            adapter.notifyDataSetChanged()
            supportActionBar?.title="Choose pics (${imageUris.size} / ${boardSize.getNumPairs()})"
        }
    }

    private fun launchIntentForPhotos() {
        val intent=Intent(Intent.ACTION_PICK)
        intent.type="image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
        startActivityForResult(intent, PICK_PHOTO_CODE)
    }

    @SuppressLint("LongLogTag")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode != Activity.RESULT_OK || data==null || requestCode!= PICK_PHOTO_CODE)  return

        val selectedUri=data.data
        val clipData=data.clipData

            //Adding images at empty spaces
        if (clipData != null) {
            Log.i(TAG, "clipData numImages ${clipData.itemCount}: $clipData")
            for (i in 0 until clipData.itemCount) {
                val clipItem = clipData.getItemAt(i)
                if (imageUris.size < boardSize.getNumPairs()) {
                    imageUris.add(clipItem.uri)
                }
            }
        } else if (selectedUri != null) {
            Log.i(TAG, "$selectedUri")
            imageUris.add(selectedUri)
        }

        adapter.notifyDataSetChanged()
        supportActionBar?.title="Choose pics (${imageUris.size} / ${boardSize.getNumPairs()})"

        saveBtn.isEnabled=shouldEnableSaveBtn()
    }

    @SuppressLint("LongLogTag")
    private fun saveDataToFirebase() {
        val customGameName=nameEditText.text.toString()
        saveBtn.isEnabled=false
        //checking that we're not creating game with existing game name
        db.collection("games").document(customGameName).get().addOnSuccessListener{
            if(it!=null && it.data!=null){
                showAlertDialog("Name taken","A game already exist with the name '$customGameName' please choose another.",null)
                saveBtn.isEnabled=true
            }else{
                handleImageUploading(customGameName)
            }
        }
    }

    @SuppressLint("LongLogTag")
    private fun handleImageUploading(customGameName: String) {
        linearProgressBar.visibility= View.VISIBLE

        var didEncounterError=false
        val uploadedImageUrls= mutableListOf<String>()
        Toast.makeText(this,"It just take a while.",Toast.LENGTH_LONG).show()
        for((index,photoUri) in imageUris.withIndex()){
            val imageByteArray=getImageByteArray(photoUri)
            val filePath="Images/$customGameName/${System.currentTimeMillis()}-${index}.jpg"

            val photoReference = storage.reference.child(filePath)

            photoReference.putBytes(imageByteArray)
                .continueWithTask{
                    //it -> photoUploadTask
                    Log.i(TAG,"upload bytes: ${it.result?.bytesTransferred}")
                    photoReference.downloadUrl
                }.addOnCompleteListener {
                    //it -> downloadUrlTask
                    if(!it.isSuccessful){
                        Log.e(TAG,"Exception with firebase",it.exception)
                        Toast.makeText(this,"Failed to upload images",Toast.LENGTH_SHORT).show()
                        didEncounterError=true
                        linearProgressBar.visibility= View.GONE
                        return@addOnCompleteListener
                    }
                    if(didEncounterError)
                        return@addOnCompleteListener

                    linearProgressBar.progress=uploadedImageUrls.size*100/imageUris.size
                    val downloadUrl=it.result.toString()
                    uploadedImageUrls.add(downloadUrl)
                    if(uploadedImageUrls.size==imageUris.size){
                        handleAllImagesUploaded(customGameName,uploadedImageUrls)
                    }
                }
        }
    }

    private fun handleAllImagesUploaded(customGameName: String, imageUrls: MutableList<String>) {
        //upload images url to firebase
        val userImageList=UserImageList(customGameName, imageUrls,auth.currentUser?.email)
        db.collection("games").document(customGameName).set(userImageList)
            .addOnCompleteListener {
                if(!it.isSuccessful){
                    Toast.makeText(this,"Failed game creation",Toast.LENGTH_LONG).show()
                    linearProgressBar.visibility= View.GONE
                    return@addOnCompleteListener
                }
                val msg="You become a contributor to this game. People all over the world can play this game."
                showAlertDialog("Congratulations!",msg){ _, _ ->
                    showAlertDialog("Upload completed, let's play your game.",null){_,_ ->
                        val resultData=Intent()
                        resultData.putExtra(EXTRA_GAME_NAME,customGameName)
                        setResult(Activity.RESULT_OK,resultData)
                        finish()
                    }
                }
            }
        linearProgressBar.visibility = View.GONE
    }

    private fun showAlertDialog(title:String,msg:String?,listener:DialogInterface.OnClickListener?) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(msg)
            .setCancelable(false)
            .setPositiveButton("OK",listener)
            .show()
    }

    @SuppressLint("LongLogTag")
    private fun getImageByteArray(photoUri: Uri): ByteArray {
        val originalBitmap = if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.P){
            val source=ImageDecoder.createSource(contentResolver,photoUri)
            ImageDecoder.decodeBitmap(source)
        }else{
            MediaStore.Images.Media.getBitmap(contentResolver,photoUri)
        }
        Log.i(TAG, "Original width ${originalBitmap.width} and height ${originalBitmap.height}")
        val scaledBitmap = BitmapScaler.scaleToFitHeight(originalBitmap,250)
        Log.i(TAG, "Scaled width ${scaledBitmap.width} and height ${scaledBitmap.height}")
        val byteOutputStream=ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG,60,byteOutputStream)

        return byteOutputStream.toByteArray()
    }

    private fun shouldEnableSaveBtn(): Boolean {
        //Check if we should enable the save btn or not
        if(imageUris.size!=boardSize.getNumPairs())
            return false
        if(nameEditText.text?.isBlank() == true || nameEditText.text?.length!!< MIN_GAME_NAME_LENGTH)
            return false
        return true
    }

    private fun checkPermission() {
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)
            !=PackageManager.PERMISSION_GRANTED){
            //Asking user when explanation is needed
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                //Show an explanation to the user
                //this thread wait for the user's response! after the user sees the explanation, try again to request the permission
                //Prompt the user once the explanation has been shown.
                showRelationalDialogForPermission()
            }else{
                val s= arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                ActivityCompat.requestPermissions(this,s, MY_PERMISSIONS_REQUEST_GALLERY)
            }
        }
    }

    private fun showRelationalDialogForPermission() {
        val msg="It seems like you have turned off permission required for this feature. It can be enable under App settings."
        AlertDialog.Builder(this)
            .setMessage(msg)
            //first _ is DialogInterface, second _ is int which(which button is clicked)
            .setPositiveButton("Go to Settings") { _, _ ->
                try{
                    val intent= Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri= Uri.fromParts("package",packageName,null)
                    intent.data=uri
                    startActivity(intent)
                }catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
            .setNegativeButton("cancel"){dialog,_ ->
                dialog.dismiss()
            }.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            MY_PERMISSIONS_REQUEST_GALLERY -> {
                //if request is empty, the result arrays are empty
                if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    //Permission was granted. Do the contacts related task you need to do
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        launchIntentForPhotos()
                    }
                }else{
//                    Permission denied, disable the functionality that depends on this permission
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
                }
                return
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}