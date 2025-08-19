import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sumte.MyReservationItem
import com.example.sumte.reservation.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BookedViewModel(private val repository: ReservationRepository) : ViewModel() {

    private val _bookedList = MutableStateFlow<List<MyReservationItem>>(emptyList())
    val bookedList: StateFlow<List<MyReservationItem>> get() = _bookedList

    fun fetchBookedList() {
        viewModelScope.launch {
            _bookedList.value = repository.getMyReservations()
        }
    }

    fun updateBookedList(newList: List<MyReservationItem>) {
        _bookedList.value = newList
    }
}
