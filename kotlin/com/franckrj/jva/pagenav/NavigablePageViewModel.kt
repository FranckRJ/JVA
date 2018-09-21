package com.franckrj.jva.pagenav

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

abstract class NavigablePageViewModel(app: Application) : AndroidViewModel(app) {
    protected val pageNumber: MutableLiveData<Int> = MutableLiveData()

    fun init(pageNumberUsed: Int) {
        /* Pour ne pouvoir initialiser qu'une seule fois la valeur. */
        if (pageNumber.value == null) {
            pageNumber.value = pageNumberUsed
        }
    }

    fun getCurrentPageNumber(): LiveData<Int?> = pageNumber

    abstract fun cancelGetContentPageInfos()
    abstract fun clearListOfContentShowable()
    abstract fun clearInfosForContentPage()
}
