package com.handstandsam.shoppingapp.cart

import com.handstandsam.shoppingapp.models.ItemWithQuantity


internal fun MutableMap<String, ItemWithQuantity>.asSortedList(): List<ItemWithQuantity> {
    return values.toList()
        .sortedBy { it.item.label }
}