package ch.epfl.sdp.drone3d.service.storage

import androidx.lifecycle.LiveData

/** Interface representing a database */
interface Database {

    /** Store the [pseudo] of the user identified by [UID] */
    fun storeUserPseudo(UID: String, pseudo: String)

    /** Listen to the pseudo of the user identified by [UID] and update a LiveData accordingly */
    fun loadUserPseudo(UID: String): LiveData<String>

    /** Erase the [pseudo] of the user identified by [UID] */
    fun removeUserPseudo(UID: String)
}