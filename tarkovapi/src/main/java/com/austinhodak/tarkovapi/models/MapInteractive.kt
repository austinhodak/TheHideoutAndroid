package com.austinhodak.tarkovapi.models

data class MapInteractive(
    var distanceToolConfig: DistanceToolConfig? = null,
    var groups: List<Group?>? = null,
    var locations: List<Location?>? = null,
    var map: Map? = null,
    var mapConfig: MapConfig? = null,
    var proCategoryLocationCounts: List<Any?>? = null,
    var regions: List<Any?>? = null,
    var searchQuery: Any? = null,
) {
    data class DistanceToolConfig(
        var lineStyle: LineStyle? = null,
        var scale: Double? = null,
        var speeds: List<Speed?>? = null,
        var useHaversine: Boolean? = null
    ) {
        data class LineStyle(
            var strokeColor: String? = null
        )

        data class Speed(
            var name: String? = null,
            var speedMps: Double? = null
        )
    }

    data class Group(
        var categories: List<Category?>? = null,
        var color: String? = null,
        var gameId: Int? = null,
        var id: Int? = null,
        var order: Int? = null,
        var title: String? = null
    ) {
        data class Category(
            var description: Any? = null,
            var featuresEnabled: Boolean? = null,
            var groupId: Int? = null,
            var hasHeatmap: Boolean? = null,
            var icon: String? = null,
            var id: Int? = null,
            var info: Any? = null,
            var order: Int? = null,
            var premium: Boolean? = null,
            var title: String? = null,
            var visible: Boolean? = null
        )
    }

    data class Location(
        var category_id: Int? = null,
        var description: String? = null,
        var features: Any? = null,
        var id: Int? = null,
        var latitude: String? = null,
        var longitude: String? = null,
        var map_id: Int? = null,
        var media: List<Media?>? = null,
        var region_id: Any? = null,
        var tags: List<Any?>? = null,
        var title: String? = null,
        var z: Any? = null,
        var quests: List<Int>? = null
    ) {
        data class Media(
            var attribution: String? = null,
            var fileName: String? = null,
            var id: Int? = null,
            var mimeType: String? = null,
            var order: Int? = null,
            var title: String? = null,
            var type: String? = null,
            var url: String? = null
        )

        fun getFormattedDescription(): String {
            return description ?: ""
        }
        
        fun isQuest(): Boolean = category_id == 955
    }

    data class Map(
        var id: Int? = null,
        var title: String? = null,
        var url: String? = null
    )

    data class MapConfig(
        var initial_zoom: Int? = null,
        var start_lat: Double? = null,
        var start_lng: Double? = null,
        var tile_sets: List<TileSet?>? = null
    ) {
        data class TileSet(
            var bounds: Bounds? = null,
            var extension: String? = null,
            var max_zoom: Int? = null,
            var min_zoom: Int? = null,
            var name: String? = null,
            var path: String? = null
        ) {
            data class Bounds(
                var `10`: Coords? = null,
                var `11`: Coords? = null,
                var `12`: Coords? = null,
                var `13`: Coords? = null,
                var `14`: Coords? = null,
                var `15`: Coords? = null,
                var `16`: Coords? = null,
                var `17`: Coords? = null,
                var `8`: Coords? = null,
                var `9`: Coords? = null
            ) {
                data class Coords(
                    var x: X? = null,
                    var y: Y? = null
                ) {
                    data class X(
                        var max: Int? = null,
                        var min: Int? = null
                    )

                    data class Y(
                        var max: Int? = null,
                        var min: Int? = null
                    )
                }
            }
        }
    }
    fun getFirstMap(): MapConfig.TileSet? {
       return mapConfig?.tile_sets?.first()
    }
}