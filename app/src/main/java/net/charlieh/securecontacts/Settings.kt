package net.charlieh.securecontacts

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.content_settings.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class Settings : AppCompatActivity() {

    private lateinit var locationFrom:String
    private lateinit var  currentPhotoPath: String
    private val REQUEST_IMAGE_CAPTURE = 200
    private val PERMISSION_TAKE_PHOTO = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        locationFrom = intent.getStringExtra("LocationFrom")

        val db = AppDatabase.getInstance(applicationContext)
        val contactDAO = db.contactDao()
        var user = contactDAO.findUser()
        if(user!=null) {
            firstName.setText(user.first_name)
            lastName.setText(user.last_name)
            if(!user.image.isNullOrBlank())
            {
                val file = File(user.image)
                if(file.exists())
                {
                    val bmp = BitmapFactory.decodeFile(file.absolutePath)
                    contactImage.setImageBitmap(bmp)
                }
            }
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

            } catch (ex: IOException) {
                // Error occurred while creating the File
                Toast.makeText(this,"Error occured when making file", Toast.LENGTH_LONG).show()
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.add_contact_menu, menu)
        return true
    }


    override fun getSupportParentActivityIntent(): Intent? {
        return getParentActivityIntentImpl()
    }

    override fun getParentActivityIntent(): Intent? {
        return getParentActivityIntentImpl()
    }

    private fun getParentActivityIntentImpl(): Intent? {
        var i: Intent? = null

        // Here you need to do some logic to determine from which Activity you came.
        // example: you could pass a variable through your Intent extras and check that.
        if (locationFrom == "LoginScreen") {
            this.finish()
        } else {
            i = Intent(this, ContactListActivity::class.java)
            // same comments as above
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        }

        return i
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_complete->
            {
                if(firstName.text.toString().isBlank())
                {
                    firstName.error = "Enter a first name"
                    return true
                }
                if(password.text.toString().isNotBlank()&&password.text.toString()!=confirmPassword.text.toString())
                {
                    password.error = "Passwords do not match"
                    confirmPassword.error = "Passwords do not match"
                    return true
                }
                else if(password.text.toString().isNotBlank()&&password.text.toString()==confirmPassword.text.toString())
                {
                    val passwordFile = File(filesDir.path+"PasswordFile.txt")
                    passwordFile.writeText(password.text.toString())
                }

                val db = AppDatabase.getInstance(applicationContext)
                val contactDAO = db.contactDao()
                var user = contactDAO.findUser()

                if(user==null)
                {
                    user = Contact(0,firstName.text.toString(),lastName.text.toString(),null,null,null,null,null,true)
                    contactDAO.insertAll(user)
                }
                else
                {
                    user.first_name = firstName.text.toString()
                    user.last_name = lastName.text.toString()
                    user.image = currentPhotoPath
                    contactDAO.update(user)
                }
                val intent = Intent(this,ContactListActivity::class.java)
                startActivity(intent)
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

}
