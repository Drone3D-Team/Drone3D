package ch.epfl.sdp.drone3d.storage.data
import java.time.LocalDateTime
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

data class MappingMission(
    var uid:String,
    var ownerUid:String,
    val name:String,
    val flightPath: List<LatLng>,
    val createdTime: LocalDateTime? = LocalDateTime.now()
    )
