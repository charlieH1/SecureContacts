package net.charlieh.securecontacts

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log

import kotlinx.android.synthetic.main.activity_add_contact.*

import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.content_add_contact.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest.permission
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat


class AddContact : AppCompatActivity() {

    private lateinit var  currentPhotoPath: String
    private val REQUEST_IMAGE_CAPTURE = 200
    private val PERMISSION_TAKE_PHOTO = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)
        setSupportActionBar(toolbar)
        currentPhotoPath=""

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.add_contact_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_complete->
            {
                val db = AppDatabase.getInstance(applicationContext)
                val contact = Contact(0,firstName.text.toString(),lastName.text.toString(),postalAddress.text.toString(),company.text.toString(),currentPhotoPath,phoneNumber.text.toString(),email.text.toString(),false)
                db.contactDao().insertAll(contact)
                var intentToListContact = Intent(this,ContactListActivity::class.java)
                startActivity(intentToListContact)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }



    private fun dispatchTakePictureIntent() {
        val pictureIntent = Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        if(pictureIntent.resolveActivity(getPackageManager()) != null){
            //Create a file to store the image
            var photoFile:File? = null
            try {
                photoFile = createImageFile()

            } catch (ex:IOException) {
                // Error occurred while creating the File
                Toast.makeText(this,"Error occured when making file",Toast.LENGTH_LONG).show()
            }
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(this,"net.charlieh.securecontacts.provider",photoFile)
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        photoURI)
                startActivityForResult(pictureIntent,
                        REQUEST_IMAGE_CAPTURE)
            }
        }
        if(pictureIntent.resolveActivity(getPackageManager()) == null)
        {
            Toast.makeText(this,"Cant Take photo no activity that takes photos found", Toast.LENGTH_LONG).show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode==REQUEST_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                contactImage.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath))
                }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }



    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        Log.e("StorageDir","Storage directory = " + storageDir!!.absolutePath)
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }



    fun onImageClick(x: View?)
    {

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.CAMERA), PERMISSION_TAKE_PHOTO)
            Log.d("onImageClick","request camera permission")
        }

        if((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED)) {
                dispatchTakePictureIntent()
        }
    }


}
