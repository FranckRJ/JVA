package com.franckrj.jva.base

import androidx.lifecycle.LiveData
import com.franckrj.jva.services.WebService

abstract class AbsRepository {
    protected abstract val serviceForWeb: WebService
    private val mapOfGetterInstances: HashMap<LiveData<out Any?>, AbsAsyncValueSetter<out Any?>> = HashMap()

    fun addThisRequestForThisLiveData(newRequest: AbsAsyncValueSetter<out Any?>, liveDataLinkedToRequest: LiveData<out Any?>) {
        cancelRequestForThisLiveData(liveDataLinkedToRequest)
        mapOfGetterInstances[liveDataLinkedToRequest] = newRequest
    }

    fun removeRequestForThisLiveData(liveDataLinkedToRequest: LiveData<out Any?>) {
        mapOfGetterInstances.remove(liveDataLinkedToRequest)
    }

    fun cancelRequestForThisLiveData(liveDataLinkedToRequest: LiveData<out Any?>) {
        val requestInstance: AbsAsyncValueSetter<out Any?>? = mapOfGetterInstances[liveDataLinkedToRequest]

        if (requestInstance != null) {
            requestInstance.cancel()
            serviceForWeb.cancelRequest(requestInstance.hashCode())
            mapOfGetterInstances.remove(liveDataLinkedToRequest)
        }
    }
}
