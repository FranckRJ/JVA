package com.franckrj.jva.base

import androidx.lifecycle.MutableLiveData
import com.franckrj.jva.utils.LoadableValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class AbsAsyncValueSetter<T>(protected val liveDataToSet: MutableLiveData<LoadableValue<T?>?>) {
    private var currentJob: Job? = null

    fun execute(tryToGetCachedValue: Boolean) {
        if (currentJob == null) {
            currentJob = GlobalScope.launch {
                val result = getValueToSetInBackground(tryToGetCachedValue)
                withContext(Dispatchers.Main) {
                    setTheValueGetted(result, isActive)
                }
                afterValueGettedInBackground(result, isActive)
            }
        }
    }

    fun cancel() {
        if (currentJob != null && currentJob?.isActive == true) {
            currentJob?.cancel()
            onCancelled()
        }
    }

    protected abstract fun getValueToSetInBackground(tryToGetCachedValue: Boolean): T?
    protected abstract fun setTheValueGetted(result: T?, isStillActive: Boolean)
    protected open fun afterValueGettedInBackground(result: T?, isStillActive: Boolean) {}
    protected abstract fun onCancelled()
}
