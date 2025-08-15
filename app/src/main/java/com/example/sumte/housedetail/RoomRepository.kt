package com.example.sumte.housedetail

import com.example.sumte.housedetail.toRoomInfo
import retrofit2.HttpException
import java.time.LocalDate

class RoomRepository(
    private val service: RoomService
) {
    // 단일 방 조회
    suspend fun fetchRoom(roomId: Int): RoomDetailInfo {

        val response = service.getRoom(roomId)

        return response.data.toRoomDetailInfo()
    }


    suspend fun fetchRooms(
        guesthouseId: Int,
        startDate: String,
        endDate: String
    ): List<RoomInfo> {
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

