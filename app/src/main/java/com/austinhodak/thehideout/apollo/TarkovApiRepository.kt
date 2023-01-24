package com.austinhodak.thehideout.apollo

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.austinhodak.thehideout.AmmoQuery
import com.austinhodak.thehideout.ItemQuery
import com.austinhodak.thehideout.ItemsQuery
import com.austinhodak.thehideout.fragment.ItemFragment
import com.austinhodak.thehideout.realm.converters.ItemWrapper
import com.austinhodak.thehideout.realm.converters.toWrapper
import com.austinhodak.thehideout.type.ItemType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TarkovApiRepository @Inject constructor(
    private val apolloClient: ApolloClient
) : ApolloDataSource {

/*
    override fun getItem(id: String): Flow<ItemWrapper?> = apolloClient.query(
        ItemQuery(id = id)
    ).toFlow().map { response ->
        response.data?.item?.toWrapper()
    }
*/

    override fun getAllItems(type: ItemType): Flow<List<ItemFragment>> = apolloClient.query(
        ItemsQuery(
            type = Optional.present(type)
        )
    ).toFlow().map { response ->
        if (!response.hasErrors()) {
            val items = response.data?.items?.filterNotNull()?.map { item -> item}
            if (!items.isNullOrEmpty()) {
                items
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    override fun getItemsByType(type: ItemType): Flow<List<ItemWrapper>> = apolloClient.query(
        ItemsQuery(
            types = Optional.present(
                listOf(type)
            )
        )
    ).toFlow().map { response ->
        if (!response.hasErrors()) {
            val items = response.data?.items?.filterNotNull()?.map { item -> item.toWrapper() }
            if (!items.isNullOrEmpty()) {
                items
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    override fun getAllAmmunition(): Flow<List<AmmoQuery.Data.Ammo>?> = apolloClient.query(
        AmmoQuery()
    ).toFlow().map { response ->
        if (!response.hasErrors()) {
            val ammo = response.data?.ammo?.filterNotNull()
            if (!ammo.isNullOrEmpty()) {
                ammo
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    override fun <T> getItem(id: String?): Flow<T?> {
        return if (id != null) {
            flow<T> { apolloClient.query(ItemQuery(id = id)).toFlow().map { it.data?.item } }
        } else {
            flow<T> { apolloClient.query(ItemsQuery()).toFlow().map { it.data?.items } }
        }
    }
}