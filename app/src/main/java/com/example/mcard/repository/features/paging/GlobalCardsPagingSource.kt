package com.example.mcard.repository.features.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.mcard.repository.features.rest.firebase.FirebaseController.Companion.FIRESTORE_CARDS
import com.example.mcard.repository.models.storage.CardEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await


internal class GlobalCardsPagingSource(
    private val searchParams: String? = null,
) : PagingSource<QuerySnapshot, CardEntity>() {

    private val sourceApi: FirebaseFirestore =
        FirebaseFirestore.getInstance()


    override fun getRefreshKey(
        state: PagingState<QuerySnapshot, CardEntity>,
    ) = null

    override suspend fun load(
        params: LoadParams<QuerySnapshot>,
    ): LoadResult<QuerySnapshot, CardEntity> =
        try {
            val currentPage =
                params.key
                    ?: sourceApi.collection(
                        FIRESTORE_CARDS
                    ).run {
                        searchParams?.run {
                            whereEqualTo(
                                "name", searchParams
                            )
                        } ?: this
                    }.limit(10).get().await()

            val nextPage =
                sourceApi.collection(FIRESTORE_CARDS).run {
                    searchParams?.run {
                        whereEqualTo(
                            "name", searchParams
                        )
                    } ?: this
                }.limit(8)
                    .startAfter(currentPage.documents[currentPage.size() - 1])
                    .get()
                    .await()

            LoadResult.Page(
                data = currentPage.toObjects(CardEntity::class.java),
                prevKey = null,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
}