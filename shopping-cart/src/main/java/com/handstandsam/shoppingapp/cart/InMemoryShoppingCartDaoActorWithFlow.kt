package com.handstandsam.shoppingapp.cart

import com.handstandsam.shoppingapp.models.ItemWithQuantity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In memory implementation of our [ShoppingCartDao]
 */
class InMemoryShoppingCartDaoActorWithFlow : ShoppingCartDao {

    override val allItems = MutableStateFlow<List<ItemWithQuantity>>(listOf())

    sealed class Intention {
        class FindByLabel(
            val label: String,
            val deferred: CompletableDeferred<ItemWithQuantity?>
        ) : Intention()

        class Upsert(val itemWithQuantity: ItemWithQuantity) : Intention()

        class Remove(val itemWithQuantity: ItemWithQuantity) : Intention()

        object Empty : Intention()
    }

    private val scope = CoroutineScope(Dispatchers.Default)

    private val actor = scope.actor<Intention> {

        val itemsInCart: MutableMap<String, ItemWithQuantity> = mutableMapOf()

        for (intention in channel) {
            when (intention) {
                is Intention.FindByLabel -> {
                    intention.deferred.complete(itemsInCart[intention.label])
                }
                is Intention.Upsert -> {
                    itemsInCart[intention.itemWithQuantity.item.label] = intention.itemWithQuantity
                }
                is Intention.Remove -> itemsInCart.remove(intention.itemWithQuantity.item.label)
                is Intention.Empty -> itemsInCart.clear()
            }

            val sortedItems = itemsInCart.asSortedList()

            if (allItems.value != sortedItems) {
                allItems.value = sortedItems
            } else {
                // No change detected, don't emit
            }
        }
    }

    override suspend fun findByLabel(label: String): ItemWithQuantity? {
        val deferred = CompletableDeferred<ItemWithQuantity?>()
        actor.send(
            Intention.FindByLabel(
                label = label,
                deferred = deferred
            )
        )
        return deferred.await()
    }

    override suspend fun upsert(itemWithQuantity: ItemWithQuantity) {
        actor.send(Intention.Upsert(itemWithQuantity))
    }

    override suspend fun remove(itemWithQuantity: ItemWithQuantity) {
        actor.send(Intention.Remove(itemWithQuantity))
    }

    override suspend fun empty() {
        actor.send(Intention.Empty)
    }

}
