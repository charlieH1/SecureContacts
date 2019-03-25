package net.charlieh.securecontacts

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.contact_item.view.*
import java.io.File

class ListAdapterForContact(private val ctx:Context, private val DataSource: List<Contact>): BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater :LayoutInflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val contactView = inflater.inflate(R.layout.contact_item,parent,false)
        val contact = getItem(position) as Contact

        contactView.contact_name.text = "${contact.first_name} ${contact.last_name}"
        if (!contact.image.isNullOrBlank())
        {

            val file = File(contact.image)
            if(file.exists())
            {
                val bmp = BitmapFactory.decodeFile(file.absolutePath)
                contactView.contact_icon.setImageBitmap(bmp)
            }
        }
        return contactView
    }


    override fun getItem(position: Int): Any = DataSource[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = DataSource.size
}