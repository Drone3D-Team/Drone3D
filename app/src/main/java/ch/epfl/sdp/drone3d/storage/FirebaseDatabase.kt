package ch.epfl.sdp.drone3d.storage

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase


class FirebaseDatabase: Database {
    private val database =
        Firebase.database("https://drone3d-6819a-default-rtdb.europe-west1.firebasedatabase.app/")

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
                Log.w(TAG, "loadUserPseudo:onCancelled", databaseError.toException())
            }
        }
        userRef(UID).child(PSEUDO_PATH).addValueEventListener(pseudoListener)

        return pseudo
    }

    override fun removeUsePseudo(UID: String) {
        userRef(UID).child(PSEUDO_PATH).removeValue()
    }
}