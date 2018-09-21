package com.franckrj.jva.base

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import com.franckrj.jva.services.WebService

abstract class AbsRepository {
    protected abstract val serviceForWeb: WebService
    private val mapOfGetterInstances: HashMap<LiveData<out Any?>, AsyncTask<out Any?, out Any?, out Any?>> = HashMap()

    fun addThisRequestForThisLiveData(newRequest: AsyncTask<out Any?, out Any?, out Any?>, liveDataLinkedToRequest: LiveData<out Any?>) {
        cancelRequestForThisLiveData(liveDataLinkedToRequest)
        mapOfGetterInstances[liveDataLinkedToRequest] = newRequest
    }

    fun removeRequestForThisLiveData(liveDataLinkedToRequest: LiveData<out Any?>) {
        mapOfGetterInstances.remove(liveDataLinkedToRequest)
    }

    fun cancelRequestForThisLiveData(liveDataLinkedToRequest: LiveData<out Any?>) {
        val requestInstance: AsyncTask<out Any?, out Any?, out Any?>? = mapOfGetterInstances[liveDataLinkedToRequest]

        if (requestInstance != null) {
            requestInstance.cancel(false)
            serviceForWeb.cancelRequest(requestInstance.hashCode())
            mapOfGetterInstances.remove(liveDataLinkedToRequest)
        }
    }
}
