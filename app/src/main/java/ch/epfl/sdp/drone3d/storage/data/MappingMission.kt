package ch.epfl.sdp.drone3d.storage.data
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

data class MappingMission(
    var uid:String,
    var ownerUid:String,
    val name:String,
    val flightPath: List<LatLng>,
    val createdTime: LocalDateTime? = LocalDateTime.now(),
    var isShared:Boolean
    )
