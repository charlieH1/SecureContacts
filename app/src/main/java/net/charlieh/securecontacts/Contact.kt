package net.charlieh.securecontacts

import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.content.Context

@Entity
data class Contact (
    @PrimaryKey (autoGenerate = true) var contactId:Int,
    @ColumnInfo (name="first_name") var first_name:String,
    @ColumnInfo (name="last_name") var last_name:String?,
    @ColumnInfo (name="postal_address") var postal_address:String?,
    @ColumnInfo (name="company") var company:String?,
    @ColumnInfo (name="image") var image:String?,
    @ColumnInfo (name="phone_number") var phone_number:String?,
    @ColumnInfo (name="email_address") var email_address:String?,
    @ColumnInfo (name="is_user") var is_user:Boolean

)

@Dao
interface ContactDao{
    @Query ("SELECT * FROM Contact")
    fun getAllContacts(): List<Contact>

    @Query ("SELECT * FROM Contact WHERE is_user = 0")
    fun getAllContactsWithoutUser(): List<Contact>

    @Query("SELECT * FROM Contact WHERE contactId = :contactId LIMIT 1")
    fun getContactById(contactId: Int):Contact?

    @Query("SELECT * FROM Contact WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): Contact?

    @Query("SELECT * FROM Contact WHERE is_user = 1 LIMIT 1")
    fun findUser ():Contact?

    @Update(onConflict = REPLACE)
    fun update(contact:Contact)

    @Insert
    fun insertAll(vararg contacts: Contact)

    @Delete
    fun delete(contact: Contact)
}

@Database(entities = arrayOf(Contact::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao

    companion object : SingletonHolder<AppDatabase, Context>({
        Room.databaseBuilder(it.applicationContext,
                AppDatabase::class.java, "Contacts.db").allowMainThreadQueries()
                .build()
    })
}

open class SingletonHolder<T, A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile private var instance: T? = null

    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}
