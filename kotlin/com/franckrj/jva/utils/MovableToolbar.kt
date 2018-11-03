package com.franckrj.jva.utils

/**
 * Interface pour les Activity ayant une Toolbar pouvant bouger.
 */
interface MovableToolbar {
    /**
     * Fonction appelée lorsque la Toolbar a bougée.
     *
     * @param   toolbarIsOnTop  Vrai si la Toolbar est haut plus haut qu'elle puisse être, faux sinon.
     */
    fun toolbarMoved(toolbarIsOnTop: Boolean)
}
