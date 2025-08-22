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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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

    private val reviewWriteResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val completedReservationId = result.data?.getIntExtra("completedReservationId", -1) ?: -1
            if (completedReservationId != -1) {
                val currentList = bookedVM.bookedList.value.toMutableList()
                val itemIndex = currentList.indexOfFirst { it.id == completedReservationId }
                if (itemIndex != -1) {
                    val updatedItem = currentList[itemIndex].copy(reviewWritten = true)
                    currentList[itemIndex] = updatedItem
                    bookedVM.updateBookedList(currentList)
                    val updatedBookedDataList = currentList.map { item ->
                        BookedData(
                            roomImg = item.imageUrl,
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
                    }.sortedByDescending { LocalDateTime.parse(it.reservedAt) }
                    adapter.updateData(updatedBookedDataList)
                }
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            val canceledReservationId = result.data?.getIntExtra("canceledReservationId", -1) ?: -1
            if (canceledReservationId != -1) {
                val currentList = bookedVM.bookedList.value.toMutableList()
                val itemIndex = currentList.indexOfFirst { it.id == canceledReservationId }
                if (itemIndex != -1) {
                    val updatedItem = currentList[itemIndex].copy(reviewWritten = false)
                    currentList[itemIndex] = updatedItem
                    bookedVM.updateBookedList(currentList)
                    val updatedBookedDataList = currentList.map { item ->
                        BookedData(
                            roomImg = item.imageUrl,
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
                    }.sortedByDescending { LocalDateTime.parse(it.reservedAt) }
                    adapter.updateData(updatedBookedDataList)
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

//        bookedVM.fetchBookedList()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                bookedVM.fetchBookedList()
            }
        }
        //데이터전달부분
        lifecycleScope.launch {
            bookedVM.bookedList.collectLatest { list ->
                val bookedDataList = list.map { item ->
                    BookedData(
                        roomImg = item.imageUrl,
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
                if (bookedDataList.isEmpty()) {
                    // 예약 내역이 없으면 Empty Layout 보이기
                    binding.reviewEmptyLayout.visibility = View.VISIBLE
                    binding.bookedListRecyclerview.visibility = View.GONE
                } else {
                    // 예약 내역이 있으면 RecyclerView 보이기
                    binding.reviewEmptyLayout.visibility = View.GONE
                    binding.bookedListRecyclerview.visibility = View.VISIBLE
                    adapter.updateData(bookedDataList)
                }
            }
        }

//    override fun onResume() {
//        super.onResume()
//        bookedVM.fetchBookedList()
//    }
    }
}
