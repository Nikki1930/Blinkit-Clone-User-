package com.example.userblinkitclone.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CartProduct::class], version = 2, exportSchema = false) // ⬅️ Bumped version to 2
abstract class CartProductDatabase : RoomDatabase() {

    abstract fun cartProductDao(): CartProductDao

    companion object {

        @Volatile
        var INSTANCE: CartProductDatabase? = null

        fun getDatabaseInstance(context: Context): CartProductDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) return tempInstance

            synchronized(this) {
                val roomDatabaseInstance = Room.databaseBuilder(
                    context.applicationContext,
                    CartProductDatabase::class.java,
                    "CartProduct"
                )

                    .allowMainThreadQueries()
                    .build()

                INSTANCE = roomDatabaseInstance
                return roomDatabaseInstance
            }
        }
    }
}
