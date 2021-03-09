package ch.epfl.sdp.drone3d.storage

import androidx.lifecycle.LiveData

interface Database {
    fun storeUserPseudo(UID: String, pseudo: String)
    fun loadUserPseudo(UID: String): LiveData<String>
    fun removeUsePseudo(UID: String)
}