package com.example.sumte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sumte.databinding.FragmentReviewManageBinding

class ReviewManage: Fragment() {
    lateinit var binding:FragmentReviewManageBinding

//    private val databaseRef
//    private val currentUserUid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentReviewManageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserReviews()
    }

    fun loadUserReviews(){
//        if(currentUserUid == null) return
//        // "/reviews/{userUid}/" 아래에 후기들이 저장되어 있다고 가정
//
//        databaseRef.child("reviews").child(currentUserUid)
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val reviewList = mutableListOf<Review>()
//
//                    for (child in snapshot.children) {
//                        val review = child.getValue(Review::class.java)
//                        review?.let { reviewList.add(it) }
//                    }
//
//                    if (reviewList.isEmpty()) {
//                        binding.reviewManageRv.visibility = View.GONE
//                        binding.reviewEmptyLayout.visibility = View.VISIBLE
//                    } else {
//                        binding.reviewManageRv.visibility = View.VISIBLE
//                        binding.reviewEmptyLayout.visibility = View.GONE
//
//                        val adapter = ReviewAdapter(reviewList)
//                        binding.reviewManageRv.layoutManager = LinearLayoutManager(requireContext())
//                        binding.reviewManageRv.adapter = adapter
//                    }
//                }
//
////                override fun onCancelled(error: DatabaseError) {
////                    Toast.makeText(requireContext(), "리뷰 로딩 실패", Toast.LENGTH_SHORT).show()
////                }
//            })
    }
}