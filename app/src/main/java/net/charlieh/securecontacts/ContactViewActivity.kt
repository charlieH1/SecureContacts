package net.charlieh.securecontacts

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import kotlinx.android.synthetic.main.activity_contact_view.*
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.util.Linkify
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import kotlinx.android.synthetic.main.content_contact_view.*


private var REQUEST_LOCATION_CODE = 101

private lateinit var googleApiClient: GoogleApiClient
private lateinit var location: Location
private lateinit var locationRequest: LocationRequest

private val UPDATE_INTERVAL: Long = 10000 //10 secs
private val FASTEST_INTERVAL: Long = 2000 //2 secs

class ContactViewActivity : AppCompatActivity(),OnMapReadyCallback {

    private lateinit var contact:Contact

    private val ctx = this

    private lateinit var map:GoogleMap



    private val locationCallback  = object:Callback<LocationData>{
        override fun onFailure(call: Call<LocationData>, t: Throwable) {
            Toast.makeText(ctx,"A Problem occurred with the API", Toast.LENGTH_LONG).show()

        }

        override fun onResponse(call: Call<LocationData>, response: Response<LocationData>) {
            if (response.body()!!.status=="OK") {
                val lat = response.body()!!.results[0].geometry.location.lat
                val long = response.body()!!.results[0].geometry.location.lng
                map.addMarker(MarkerOptions().position(LatLng(lat,long)))
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat,long),15F))
            }
            else
            {
                Toast.makeText(ctx,"Address Not Found",Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_view)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val db = AppDatabase.getInstance(applicationContext)
        val contactAttempt = db.contactDao().getContactById(intent.getIntExtra("ContactId",0))
        if (contactAttempt == null)
        {
            finish()
        }
        else {
            contact = contactAttempt
        }
        if(!contact.image.isNullOrBlank())
        {
            val file = File(contact.image)
            if(file.exists())
            {
                val bmp = BitmapFactory.decodeFile(file.absolutePath)
                ContactImage.setImageBitmap(bmp)
            }
        }
        ContactName.text = """${contact.first_name} ${contact.last_name}"""

        if(contact.phone_number!=null)
        {
            txtPhoneArea.text = contact.phone_number
            Linkify.addLinks(txtPhoneArea,Linkify.PHONE_NUMBERS)
            PhoneImageView.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:"+contact.phone_number)
                startActivity(intent)
            }
            PhoneIconTextView.setOnClickListener{
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:"+contact.phone_number)
                startActivity(intent)
            }
            PhoneImageView.setColorFilter(resources.getColor(android.R.color.holo_blue_dark))
            PhoneIconTextView.setTextColor(resources.getColor(android.R.color.holo_blue_dark))

            SMSImageView.setOnClickListener{
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data= Uri.parse("smsto:"+ contact.phone_number)
                startActivity(intent)
            }
            SMSTextView.setOnClickListener{
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data= Uri.parse("smsto:"+ contact.phone_number)
                startActivity(intent)
            }
            SMSImageView.setColorFilter(resources.getColor(android.R.color.holo_blue_dark))
            SMSTextView.setTextColor(resources.getColor(android.R.color.holo_blue_dark))
        }
        if(contact.email_address!=null)
        {
            txtEmailEntry.text = contact.email_address
            Linkify.addLinks(txtEmailEntry,Linkify.EMAIL_ADDRESSES)
            EmailIconImageView.setOnClickListener{
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.putExtra(Intent.EXTRA_EMAIL,contact.email_address)
                if(intent.resolveActivity(packageManager)!=null)
                {
                    startActivity(intent)
                }
                else
                {
                    Toast.makeText(this,"No Emailer App found",Toast.LENGTH_LONG).show()
                }
            }
            EmailIconTextView.setOnClickListener{
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.putExtra(Intent.EXTRA_EMAIL,contact.email_address)
                if(intent.resolveActivity(packageManager)!=null)
                {
                    startActivity(intent)
                }
                else
                {
                    Toast.makeText(this,"No Emailer App found",Toast.LENGTH_LONG).show()
                }
            }
            EmailIconImageView.setColorFilter(resources.getColor(android.R.color.holo_blue_dark))
            EmailIconTextView.setTextColor(resources.getColor(android.R.color.holo_blue_dark))
        }

        if(contact.postal_address!= null)
        {
            txtAddressArea.text = contact.postal_address
            val mapFragment = fragmentManager
                    .findFragmentById(R.id.map) as MapFragment
            mapFragment.getMapAsync(this)



            //access LocationServices API provided in Google Play library
            googleApiClient = GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .build()

            //create location request specifying quality of request - accuracy and frequency
            locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(UPDATE_INTERVAL)
                    .setFastestInterval(FASTEST_INTERVAL)

            NavigateButton.setOnClickListener{
                openMap(NavigateButton.rootView)
            }
        }






    }

    override fun onBackPressed() {
        ActivityCompat.finishAfterTransition(this)
    }
    override fun onStart() {
        super.onStart()
        //manage network connection between device and services
        googleApiClient.connect()
    }

    override fun onStop() {
        super.onStop()
        if (googleApiClient.isConnected) googleApiClient.disconnect()
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.contact_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_deleteContact->
            {
                val db = AppDatabase.getInstance(applicationContext)
                val deleteBuilder = AlertDialog.Builder(this)
                deleteBuilder.setMessage("Are you sure you want to delete?")
                deleteBuilder.setCancelable(true)
                deleteBuilder.setPositiveButton(
                        "Cancel"
                ) { dialog, id -> dialog.cancel() }
                deleteBuilder.setNegativeButton(
                        "Delete"
                ) { dialog, id ->
                    if(!contact.image.isNullOrBlank())
                    {
                        val file = File(contact.image)
                        if(file.exists())
                        {
                            file.deleteOnExit()
                        }
                    }
                    db.contactDao().delete(contact)
                    val intentGoToContactView = Intent(this,ContactListActivity::class.java)
                    startActivity(intentGoToContactView)
                }
                var alertDialog = deleteBuilder.create()
                alertDialog.show()
                return true
            }
            R.id.action_editContact->
            {
                Toast.makeText(this,"Goes To edit Contact",Toast.LENGTH_LONG).show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        if (contact.postal_address!=null)
        {
            if(isNetworkConnected())
            {
                map = googleMap
                LocationRetriever().getLatLong(locationCallback,contact.postal_address.toString())
            }

        }
    }

    private fun isNetworkConnected(): Boolean {
        //get network information
         val networkInfo=(getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        //check if device connected to available network
        return networkInfo!= null && networkInfo.isConnected

    }

    fun openMap(x: View?)
    {
        //check whether GPS is enabled and permissions granted
        if (!(getSystemService(Context.LOCATION_SERVICE) as
                        LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER) )
        //no GPS enabled
            noGPSAlert()
        else if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)
        //Location permission not set
            noPermissionsAlert()
        else {
            //Location permission granted


            location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
            val lat = location.latitude.toString()
            val long = location.longitude.toString()

            val intent = Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?saddr=$lat,$long&daddr=${contact.postal_address}"))
            startActivity(intent)


        }

    }
    private fun noGPSAlert() {
        AlertDialog.Builder(this)
                .setTitle("Enable Location")
                .setMessage("Location Settings is 'Off'.\nPlease enable Settings to use app")
                .setPositiveButton("Location Settings") { _, _ ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                .setNegativeButton("Cancel") { _, _ -> }
                .show()
    }

    private fun noPermissionsAlert() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder(this)
                    .setTitle("Location permission required")
                    .setMessage("Please allow access to Location to continue using this app")
                    .setPositiveButton("OK") { _, _ ->
                        ActivityCompat.requestPermissions(this,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                REQUEST_LOCATION_CODE) }
                    .setNegativeButton("Cancel") { _, _ -> }
                    .show()
        } else ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_CODE)
    }





}

class LocationRetriever{
    fun getLatLong(callback: Callback<LocationData>, Address:String)
    {
        val googleMapAPI = "https://maps.googleapis.com/maps/api/"

        val retrofit = Retrofit.Builder().baseUrl(googleMapAPI).addConverterFactory(MoshiConverterFactory.create()).build()

        val service = retrofit.create(LocationService::class.java)

        val call = service.latLong(Address,"AIzaSyCN0-AgrDg8DIjg0B1zm3OzqxDn5ja_koM")

        call.enqueue(callback)
    }
}