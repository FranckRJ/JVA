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

    fun execute() {
        if (currentJob == null) {
            currentJob = GlobalScope.launch {
                val result = doInBackground()
                withContext(Dispatchers.Main) {
                    onPostExecute(result, isActive)
                }
            }
        }
    }

    fun cancel() {
        if (currentJob != null && currentJob?.isActive == true) {
            currentJob?.cancel()
            onCancelled()
        }
    }

    protected abstract fun doInBackground(): T?
    protected abstract fun onPostExecute(result: T?, isStillActive: Boolean)
    protected abstract fun onCancelled()
}
