package net.charlieh.securecontacts

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_password_entry.*
import java.io.File

class PasswordEntryActivity : AppCompatActivity() {


    private lateinit var passwordFile:File


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_entry)
        passwordFile = File(filesDir.path+"PasswordFile.txt")
        if (!passwordFile.exists())
        {
            val intent = Intent(this,Settings::class.java)
            intent.putExtra("LocationFrom","LoginScreen")
            startActivity(intent)
        }



    }

    fun loginButtonClicked(x:View?)
    {
        val passwordEntered = txtPasswordEntry.text.toString()
        val filePassword = passwordFile.readText(Charsets.UTF_8)

        if(passwordEntered.equals(filePassword,false))
        {
            val intent = Intent(this,ContactListActivity::class.java)
            startActivity(intent)
        }
        else
        {
            txtPasswordEntry.error = getString(R.string.error_password_invalid)
        }
    }


}
