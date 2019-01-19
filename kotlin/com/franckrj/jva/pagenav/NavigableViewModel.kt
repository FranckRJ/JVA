package com.franckrj.jva.pagenav

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData

abstract class NavigableViewModel(app: Application) : AndroidViewModel(app) {
    protected val lastPageNumber: MediatorLiveData<Int> = MediatorLiveData()
    protected val currentPageNumber: MutableLiveData<Int> = MutableLiveData()
    protected val frameScrollOffset: MutableLiveData<Int?> = MutableLiveData()
    var frameOutsideScreenHeight: Int = 0
        protected set

    fun setCurrentPageNumber(newPossibleCurrentPageNumber: Int) {
        val newRealCurrentPageNumber = newPossibleCurrentPageNumber.coerceIn(1, (lastPageNumber.value ?: 1))

        if (currentPageNumber.value != newRealCurrentPageNumber) {
            currentPageNumber.value = newRealCurrentPageNumber
        }
    }

    fun getLastPageNumber(): LiveData<Int?> = lastPageNumber

    fun getCurrentPageNumber(): LiveData<Int?> = currentPageNumber

    fun getFrameScrollOffset() : LiveData<Int?> = frameScrollOffset
}
