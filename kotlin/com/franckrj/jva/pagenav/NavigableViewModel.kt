package com.franckrj.jva.pagenav

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData

abstract class NavigableViewModel(app: Application) : AndroidViewModel(app) {
    protected val lastPageNumber: MediatorLiveData<Int> = MediatorLiveData()
    protected val currentPageNumber: MutableLiveData<Int> = MutableLiveData()

    fun setCurrentPageNumber(newPossibleCurrentPageNumber: Int) {
        val newRealCurrentPageNumber = newPossibleCurrentPageNumber.coerceIn(1, (lastPageNumber.value ?: 1))

        if (currentPageNumber.value != newRealCurrentPageNumber) {
            currentPageNumber.value = newRealCurrentPageNumber
        }
    }

    fun getLastPageNumber(): LiveData<Int?> = lastPageNumber

    fun getCurrentPageNumber(): LiveData<Int?> = currentPageNumber
}
