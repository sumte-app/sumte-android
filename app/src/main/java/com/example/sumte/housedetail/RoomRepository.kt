package com.example.sumte.housedetail

import com.example.sumte.housedetail.toRoomInfo
import retrofit2.HttpException
import java.time.LocalDate

class RoomRepository(
    private val service: RoomService
) {
    // 단일 방 조회
    suspend fun fetchRoom(roomId: Int): RoomDetailInfo {
        // 1. service.getRoom은 이제 SingleRoomResponse를 반환
        val response = service.getRoom(roomId)
        // 2. 그 안의 result(RoomDto)를 꺼내서 toRoomInfo() 호출
        return response.data.toRoomDetailInfo()
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
//            android.util.Log.d("RoomRepo", "[$i] imageUrls=${dto.imageUrl} size=${dto.imageUrl?.size}")
//            android.util.Log.d("RoomRepo", "[$i] first=${dto.imageUrl?.firstOrNull()}")
        }
        // 2. RoomResponse 객체 안의 'data' 리스트를 꺼내서 매핑합니다.
        return response.data.map { roomDto -> roomDto.toRoomInfo() }
    }

    //게스트하우스 정보
    suspend fun fetchGuesthouse(guesthouseId: Int): GuesthouseInfo {
        val response = service.getGuesthouse(guesthouseId)
        return response.data.toGuesthouseInfo()
    }

    suspend fun fetchUnavailableDates(roomId: Int): Result<List<LocalDate>> = try {
        val res = service.getUnavailableDates(roomId)
        if (!res.isSuccessful) {
            Result.failure(HttpException(res))
        } else {
            val list = res.body()?.data.orEmpty().mapNotNull {
                runCatching { LocalDate.parse(it) }.getOrNull() // "yyyy-MM-dd"
            }
            Result.success(list)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

