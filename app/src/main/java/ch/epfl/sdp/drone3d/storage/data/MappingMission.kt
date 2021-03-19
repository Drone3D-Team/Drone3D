package ch.epfl.sdp.drone3d.storage.data

enum class State {
    NOT_STORED, PRIVATE, SHARED, PRIVATE_AND_SHARED
}

data class MappingMission(
        val name: String = "",
        val flightPath: List<LatLong> = listOf(),
        var privateId: String? = null,
        var sharedId: String? = null,
        var ownerUid: String? = null,
        var state: State = State.NOT_STORED,
)
