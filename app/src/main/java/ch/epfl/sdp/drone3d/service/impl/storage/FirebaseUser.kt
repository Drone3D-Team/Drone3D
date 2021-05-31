/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.impl.storage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.drone3d.service.api.auth.Database
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import timber.log.Timber
import javax.inject.Inject


class FirebaseUser @Inject constructor(
    private val database: FirebaseDatabase
) : Database {
    companion object {
        private const val TAG = "FirebaseDatabase"
        private const val PSEUDO_PATH = "pseudo"
    }

    private val pseudo: MutableLiveData<String> = MutableLiveData()

    /**
     * Returns the reference in database of the user identified by the given [UID].
     */
    private fun userRef(UID: String): DatabaseReference{
        return database.getReference("users/$UID")
    }

    override fun storeUserPseudo(UID: String, pseudo: String) {
        userRef(UID).child(PSEUDO_PATH).setValue(pseudo)
    }

    override fun loadUserPseudo(UID: String): LiveData<String> {
        val pseudoListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val fetchedPseudo = dataSnapshot.getValue<String>()
                if (fetchedPseudo != null) {
                    pseudo.value = fetchedPseudo
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Timber.tag(TAG).w(databaseError.toException(), "loadUserPseudo:onCancelled")
            }
        }
        userRef(UID).child(PSEUDO_PATH).addValueEventListener(pseudoListener)

        return pseudo
    }

    override fun removeUserPseudo(UID: String) {
        userRef(UID).child(PSEUDO_PATH).removeValue()
    }
}