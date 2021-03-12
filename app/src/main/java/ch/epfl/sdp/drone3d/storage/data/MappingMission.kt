package ch.epfl.sdp.drone3d.storage.data
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

data class MappingMission(
    val name: String,
    val flightPath: List<LatLng>,
    var isShared: Boolean = false
) {
    var id: String? = null
    var sharedId: String? = null
    var ownerUid: String? = null
    val createdTime: LocalDateTime? = LocalDateTime.now()
}
