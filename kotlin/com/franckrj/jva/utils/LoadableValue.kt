package com.franckrj.jva.utils

/**
 * Une valeur ayant un statut de chargement.
 *
 * @param       T           Le type de la valeur à stocker.
 * @property    status      Le statut de la valeur ([STATUS_ERROR], [STATUS_LOADING], [STATUS_LOADED]).
 * @property    value       Le contenu de la valeur.
 * @property    message     Un message servant de description du statut de la valeur (ex: un message d'erreur explicite pour [STATUS_ERROR]).
 */
class LoadableValue<out T> private constructor(val status: Int, val value: T, val message: String){
    companion object {
        const val STATUS_ERROR: Int = -1
        const val STATUS_LOADING: Int = 0
        const val STATUS_LOADED: Int = 1

        /**
         * Fonction pour créer une nouvelle [LoadableValue] ayant le statut [STATUS_ERROR].
         *
         * @param   newValue    Le contenu de la valeur stockée.
         * @param   newMessage  Message optionnel pour décrire le statut de la valeur.
         */
        fun<T> error(newValue: T, newMessage:String = ""): LoadableValue<T> = LoadableValue(STATUS_ERROR, newValue, newMessage)

        /**
         * Fonction pour créer une nouvelle [LoadableValue] ayant le statut [STATUS_LOADING].
         *
         * @param   newValue    Le contenu de la valeur stockée.
         * @param   newMessage  Message optionnel pour décrire le statut de la valeur.
         */
        fun<T> loading(newValue: T, newMessage:String = ""): LoadableValue<T> = LoadableValue(STATUS_LOADING, newValue, newMessage)

        /**
         * Fonction pour créer une nouvelle [LoadableValue] ayant le statut [STATUS_LOADED].
         *
         * @param   newValue    Le contenu de la valeur stockée.
         * @param   newMessage  Message optionnel pour décrire le statut de la valeur.
         */
        fun<T> loaded(newValue: T, newMessage:String = ""): LoadableValue<T> = LoadableValue(STATUS_LOADED, newValue, newMessage)
    }
}
