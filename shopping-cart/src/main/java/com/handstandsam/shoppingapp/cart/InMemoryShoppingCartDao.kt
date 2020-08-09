package com.handstandsam.shoppingapp.cart

import com.handstandsam.shoppingapp.models.ItemWithQuantity
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

/**
 * In memory implementation of our [ShoppingCartDao]
 */
class InMemoryShoppingCartDao : ShoppingCartDao {

    private val itemsInCart: MutableMap<String, ItemWithQuantity> = mutableMapOf()

    private val channel = ConflatedBroadcastChannel(listOf<ItemWithQuantity>())

    override suspend fun findByLabel(label: String): ItemWithQuantity? {
        return itemsInCart[label]
    }

    override suspend fun upsert(itemWithQuantity: ItemWithQuantity) {
        itemsInCart[itemWithQuantity.item.label] = itemWithQuantity
        channel.send(itemsInCart.asSortedList())
    }

    override suspend fun remove(itemWithQuantity: ItemWithQuantity) {
        itemsInCart.remove(itemWithQuantity.item.label)
        channel.send(itemsInCart.asSortedList())
    }

    override suspend fun empty() {
        itemsInCart.clear()
        channel.send(itemsInCart.asSortedList())
    }

    override val allItems: Flow<List<ItemWithQuantity>>
        get() = channel.asFlow()

}
