package ch.epfl.sdp.drone3d.storage.data

import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

enum class State {
    RAM, PRIVATE, SHARED, PRIVATE_AND_SHARED
}

data class MappingMission(
        val name: String,
        val flightPath: List<LatLng>,
) {
    var privateId: String? = null
    var sharedId: String? = null
    var ownerUid: String? = null
    var state: State = State.RAM
    val createdTime: LocalDateTime? = LocalDateTime.now()
}
