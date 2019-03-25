package net.charlieh.securecontacts


import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_contact_list.*
import kotlinx.android.synthetic.main.app_bar_contact_list.*
import kotlinx.android.synthetic.main.content_contact_list.*

class ContactListActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            val intentToAddContact = Intent(this,AddContact::class.java)
            startActivity(intentToAddContact)
        }

        val db = AppDatabase.getInstance(applicationContext)
        val user = db.contactDao().findUser()
        if(user == null)
        {
            val redirectToSettingsIntent = Intent(this,Settings::class.java)
            redirectToSettingsIntent.putExtra("LocationFrom","ContactListActivity")
            startActivity(redirectToSettingsIntent)
        }



        val contacts =db.contactDao().getAllContactsWithoutUser()
        list_of_contacts.adapter = ListAdapterForContact(this,contacts)

        list_of_contacts.setOnItemClickListener{parent, view, position, id->
            val viewContact = Intent(this,ContactViewActivity::class.java)
            viewContact.putExtra("ContactId",contacts[position].contactId)
            // Get the transition name from the string
            val transitionName = getString(R.string.transition)

            val options =

                    ActivityOptionsCompat.makeSceneTransitionAnimation(this@ContactListActivity,
                            view, // Starting view
                            transitionName    // The String
                    )
            ActivityCompat.startActivity(this@ContactListActivity,viewContact,options.toBundle())
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        val headerView = nav_view.inflateHeaderView(R.layout.nav_header_contact_list)
        val headerViewHeaderString:TextView  =  headerView.findViewById(R.id.header_string)
        val headerViewImage: ImageView = headerView.findViewById(R.id.header_image)

        if(!user!!.image.isNullOrBlank())
        {
            val imageLocation=user.image
            val image = BitmapFactory.decodeFile(imageLocation)
            headerViewImage.setImageBitmap(image)
        }

        var name=user.first_name
        if(user.last_name!=null)
        {
            name=name+" "+ user.last_name
        }
        headerViewHeaderString.text = name


    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }





    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_import_from_contacts -> {
                Toast.makeText(this,"Opens import contact", Toast.LENGTH_LONG).show()

            }
            R.id.nav_add_contact -> {
                var intentToAddContact = Intent(this,AddContact::class.java)
                startActivity(intentToAddContact)
            }
            R.id.nav_settings -> {
                val intent = Intent(this,Settings::class.java)
                intent.putExtra("LocationFrom","ContactList")
                startActivity(intent)
            }

        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }


}
