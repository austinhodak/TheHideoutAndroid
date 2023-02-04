package com.austinhodak.thehideout.apollo

import com.austinhodak.thehideout.AmmoQuery
import com.austinhodak.thehideout.fragment.ItemFragment
import com.austinhodak.thehideout.realm.converters.ItemWrapper
import com.austinhodak.thehideout.type.ItemType
import kotlinx.coroutines.flow.Flow

interface ApolloDataSource {
    fun getAllItems(type: ItemType = ItemType.any): Flow<List<ItemFragment>?>
    fun getItemsByType(type: ItemType): Flow<List<ItemWrapper>?>
    //fun getItem(id: String): Flow<ItemWrapper?>
    fun getAllAmmunition(): Flow<List<AmmoQuery.Data.Ammo>?>

    fun <T> getItem(id: String? = null): Flow<T?>
}