package com.handstandsam.shoppingapp.di

import android.content.Context
import androidx.room.Room
import com.handstandsam.shoppingapp.cart.InMemoryShoppingCartDaoActorWithFlow
import com.handstandsam.shoppingapp.cart.RoomItemInCartDatabase
import com.handstandsam.shoppingapp.cart.RoomShoppingCartDao
import com.handstandsam.shoppingapp.cart.ShoppingCart
import com.handstandsam.shoppingapp.cart.ShoppingCartDao
import com.handstandsam.shoppingapp.cart.SqlDelightShoppingCartDao
import com.handstandsam.shoppingapp.cart.sqldelight.Database
import com.handstandsam.shoppingapp.preferences.UserPreferences
import com.handstandsam.shoppingapp.repository.SessionManager
import com.squareup.sqldelight.android.AndroidSqliteDriver

interface SessionGraph {
    val sessionManager: SessionManager
    val shoppingCart: ShoppingCart
    val userPreferences: UserPreferences
}

class SessionGraphImpl(
    appContext: Context
) : SessionGraph {

    private enum class DatabaseType { IN_MEMORY, ROOM, SQLDELIGHT }

    private val dbType = DatabaseType.IN_MEMORY

    private val shoppingCartDao: ShoppingCartDao = when (dbType) {
        DatabaseType.IN_MEMORY -> {
            InMemoryShoppingCartDaoActorWithFlow()
        }
        DatabaseType.ROOM -> {
            RoomShoppingCartDao(
                Room.databaseBuilder(
                    appContext,
                    RoomItemInCartDatabase::class.java,
                    "cart_room.db"
                ).build()
            )
        }
        DatabaseType.SQLDELIGHT -> {
            SqlDelightShoppingCartDao(
                AndroidSqliteDriver(
                    schema = Database.Schema,
                    context = appContext,
                    name = "cart_sqldelight.db"
                )
            )
        }
    }

    override val shoppingCart: ShoppingCart = ShoppingCart(shoppingCartDao)

    override val userPreferences = UserPreferences(appContext)

    override val sessionManager = SessionManager(shoppingCart, userPreferences)
}