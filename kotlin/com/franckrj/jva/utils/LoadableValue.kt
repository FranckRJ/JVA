package com.franckrj.jva.utils

class LoadableValue<out T> private constructor(val status: Int, val value: T, val message: String){
    companion object {
        const val STATUS_ERROR: Int = -1
        const val STATUS_LOADING: Int = 0
        const val STATUS_LOADED: Int = 1

        fun<T> error(newValue: T, newMessage:String = ""): LoadableValue<T> = LoadableValue(STATUS_ERROR, newValue, newMessage)

        fun<T> loading(newValue: T, newMessage:String = ""): LoadableValue<T> = LoadableValue(STATUS_LOADING, newValue, newMessage)

        fun<T> loaded(newValue: T, newMessage:String = ""): LoadableValue<T> = LoadableValue(STATUS_LOADED, newValue, newMessage)
    }
}
