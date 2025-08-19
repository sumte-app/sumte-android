import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sumte.databinding.FragmentBookedListMainBinding
import com.example.sumte.mybook.BookedAdapter
import com.example.sumte.mybook.BookedData
import com.example.sumte.reservation.ReservationRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class BookedListMainFragment : Fragment() {
    private lateinit var binding: FragmentBookedListMainBinding
    private lateinit var adapter: BookedAdapter
    private lateinit var bookedVM: BookedViewModel

//    private val reviewWriteResultLauncher = registerForActivityResult(
//        ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        // ReviewBookedWriteActivity에서 RESULT_OK 응답을 보내면 이 블록이 실행
//        if (result.resultCode == Activity.RESULT_OK) {
//            // 리뷰가 작성되었으므로, ViewModel을 통해 목록을 새로고침하도록 요청
//            Log.d("BookedListMainFragment", "리뷰 작성 완료. 목록을 새로고침합니다.")
//            bookedVM.fetchBookedList()
//        }
//    }
private val reviewWriteResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        // 리뷰가 작성된 예약 건의 ID를 결과로부터 받습니다.
        val completedReservationId = result.data?.getIntExtra("completedReservationId", -1) ?: -1
        if (completedReservationId != -1) {
            // 현재 ViewModel이 가지고 있는 리스트를 직접 수정합니다.
            val currentList = bookedVM.bookedList.value.toMutableList()

            // 리스트에서 해당 reservationId를 가진 아이템을 찾습니다.
            val itemIndex = currentList.indexOfFirst { it.id == completedReservationId }

            if (itemIndex != -1) {
                // 아이템을 찾았다면, 해당 아이템의 reviewWritten 상태를 true로 변경합니다.
                val updatedItem = currentList[itemIndex].copy(reviewWritten = true)
                currentList[itemIndex] = updatedItem

                // 수정된 리스트로 어댑터의 데이터를 업데이트합니다.
                // (ViewModel의 StateFlow를 업데이트하면 collectLatest가 자동으로 감지하여 UI를 갱신합니다.)
                bookedVM.updateBookedList(currentList)
            }
        }
    }
}
    class BookedViewModelFactory(private val repository: ReservationRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BookedViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return BookedViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookedListMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val repository = ReservationRepository(requireContext())
        val factory = BookedViewModelFactory(repository)
        bookedVM = ViewModelProvider(this, factory).get(BookedViewModel::class.java)

        binding.backBtn.setOnClickListener { requireActivity().finish() }

        adapter = BookedAdapter(emptyList(), this, reviewWriteResultLauncher)
        binding.bookedListRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.bookedListRecyclerview.adapter = adapter

        bookedVM.fetchBookedList()
        //데이터전달부분
        lifecycleScope.launch {
            bookedVM.bookedList.collectLatest { list ->
                val bookedDataList = list.map { item ->
                    BookedData(
                        reservationId = item.id,
                        houseName = item.guestHouseName,
                        roomType = item.roomName,
                        startDate = item.startDate,
                        endDate = item.endDate,
                        dateCount = "${item.nightCount}박",
                        adultCount = item.adultCount,
                        childCount = item.childCount,
                        status = item.status,
                        roomId = item.roomId,
                        canWriteReview = item.canWriteReview,
                        reviewWritten = item.reviewWritten,
                        reservedAt = item.reservedAt
                    )
                }.sortedByDescending { LocalDateTime.parse(it.reservedAt) } // ← 최신 예약이 위로

                // 어댑터를 매번 새로 만들지 말고 updateData 사용
                adapter.updateData(bookedDataList)
            }
        }
    }

//    override fun onResume() {
//        super.onResume()
//        bookedVM.fetchBookedList()
//    }
}
