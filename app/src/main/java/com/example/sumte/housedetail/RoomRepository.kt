package com.example.sumte.housedetail

import com.example.sumte.housedetail.toRoomInfo

class RoomRepository(
    private val service: RoomService
) {
    // 단일 방 조회
    suspend fun fetchRoom(roomId: Int): RoomInfo {
        // 1. service.getRoom은 이제 SingleRoomResponse를 반환
        val response = service.getRoom(roomId)
        // 2. 그 안의 result(RoomDto)를 꺼내서 toRoomInfo() 호출
        return response.data.toRoomInfo()
    }



    // 게스트하우스 방 목록 조회 (날짜 범위 필요)
    suspend fun fetchRooms(
        guesthouseId: Int,
        startDate: String,
        endDate: String
    ): List<RoomInfo> {
        // 1. 이제 service.getRooms()는 'RoomResponse' 객체를 반환합니다.
        val response = service.getRooms(guesthouseId, startDate, endDate)

        response.data.orEmpty().forEachIndexed { i, dto ->
            android.util.Log.d("RoomRepo", "[$i] imageUrls=${dto.imageUrls} size=${dto.imageUrls?.size}")
            android.util.Log.d("RoomRepo", "[$i] first=${dto.imageUrls?.firstOrNull()}")
        }
        // 2. RoomResponse 객체 안의 'data' 리스트를 꺼내서 매핑합니다.
        return response.data.map { roomDto -> roomDto.toRoomInfo() }
    }

    //게스트하우스 정보
    suspend fun fetchGuesthouse(guesthouseId: Int): GuesthouseInfo {
        val response = service.getGuesthouse(guesthouseId)
        return response.data.toGuesthouseInfo()
    }
}

