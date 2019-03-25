package net.charlieh.securecontacts
import com.squareup.moshi.Json
data class LocationData(
    @Json(name = "results")
    val results: List<Result> = listOf(),
    @Json(name = "status")
    val status: String = ""
)

data class Result(
    @Json(name = "address_components")
    val addressComponents: List<AddressComponent> = listOf(),
    @Json(name = "formatted_address")
    val formattedAddress: String = "",
    @Json(name = "geometry")
    val geometry: Geometry = Geometry(),
    @Json(name = "place_id")
    val placeId: String = "",
    @Json(name = "types")
    val types: List<String> = listOf()
)

data class Geometry(
    @Json(name = "location")
    val location: Location = Location(),
    @Json(name = "location_type")
    val locationType: String = "",
    @Json(name = "viewport")
    val viewport: Viewport = Viewport()
)

data class Viewport(
    @Json(name = "northeast")
    val northeast: Northeast = Northeast(),
    @Json(name = "southwest")
    val southwest: Southwest = Southwest()
)

data class Northeast(
    @Json(name = "lat")
    val lat: Double = 0.0,
    @Json(name = "lng")
    val lng: Double = 0.0
)

data class Southwest(
    @Json(name = "lat")
    val lat: Double = 0.0,
    @Json(name = "lng")
    val lng: Double = 0.0
)

data class Location(
    @Json(name = "lat")
    val lat: Double = 0.0,
    @Json(name = "lng")
    val lng: Double = 0.0
)

data class AddressComponent(
    @Json(name = "long_name")
    val longName: String = "",
    @Json(name = "short_name")
    val shortName: String = "",
    @Json(name = "types")
    val types: List<String> = listOf()
)