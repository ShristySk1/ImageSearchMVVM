package com.codinginflow.imagesearchapp.ui.gallary

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.codinginflow.imagesearchapp.data.UnsplashPhoto
import com.codinginflow.imagesearchapp.data.UnsplashRepository

class GallaryViewModel @ViewModelInject constructor(
    private val repository: UnsplashRepository,
    @Assisted state: SavedStateHandle
) :
    ViewModel() {
//        private val currentQuery = MutableLiveData(DEFAULT_QUERY)
    private val currentQuery = state.getLiveData(CURRENT_QUERY, DEFAULT_QUERY)

//        val photos = repository.getSearchResult(query = "cats")
    val photos: LiveData<PagingData<UnsplashPhoto>> = currentQuery.switchMap { queryString ->
        repository.getSearchResult(queryString).cachedIn(viewModelScope)
    }


    fun searchPhotos(query: String) {
        currentQuery.value = query
    }

    companion object {
        private const val CURRENT_QUERY = "current_query"
        private const val DEFAULT_QUERY = "cats"
    }
}