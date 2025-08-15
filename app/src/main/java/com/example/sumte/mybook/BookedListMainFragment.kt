import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class BookedListMainFragment : Fragment() {
    private lateinit var binding: FragmentBookedListMainBinding
    private lateinit var adapter: BookedAdapter
    private lateinit var bookedVM: BookedViewModel

//    private lateinit var binding: FragmentBookedListMainBinding
//    private val viewModel: BookedViewModel by viewModels()
//    private lateinit var adapter: BookedAdapter

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
        val repository = ReservationRepository(requireContext())
        val factory = BookedViewModelFactory(repository)
        bookedVM = ViewModelProvider(this, factory).get(BookedViewModel::class.java)

        binding.backBtn.setOnClickListener { requireActivity().finish() }

        adapter = BookedAdapter(emptyList(), this)
        binding.bookedListRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.bookedListRecyclerview.adapter = adapter

        bookedVM.fetchBookedList()

        lifecycleScope.launch {
            bookedVM.bookedList.collectLatest { list ->
                val bookedDataList = list.map { item ->
                    BookedData(
                        bookedDate = item.startDate,
                        houseName = item.guestHouseName,
                        roomType = item.roomName,
                        startDate = item.startDate,
                        endDate = item.endDate,
                        dateCount = "${item.nightCount}박",
                        adultCount = item.adultCount,
                        childCount = item.childCount,
                        status = item.status,
                        canWriteReview = item.canWriteReview,
                        reviewWritten = item.reviewWritten
                    )
                }
                adapter = BookedAdapter(bookedDataList, this@BookedListMainFragment)
                binding.bookedListRecyclerview.adapter = adapter
            }
        }
    }
}
