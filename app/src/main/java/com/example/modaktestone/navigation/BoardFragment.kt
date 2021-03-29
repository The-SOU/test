package com.example.modaktestone.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.modaktestone.R
import com.example.modaktestone.databinding.FragmentBoardBinding
import com.example.modaktestone.databinding.ItemBoardBinding

class BoardFragment : Fragment() {
    private var _binding : FragmentBoardBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBoardBinding.inflate(inflater, container, false)
        val view = binding.root


        binding.boardfragmentRecyclerview.adapter = BoardRecyclerViewAdapter()
        binding.boardfragmentRecyclerview.layoutManager = LinearLayoutManager(this.context)

        return view
//        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_board, container, false)
//        return super.onCreateView(inflater, container, savedInstanceState)


    }

    inner class BoardRecyclerViewAdapter : RecyclerView.Adapter<BoardRecyclerViewAdapter.CustomViewHolder>() {
        var busanDTO : List<String> = listOf("자유게시판", "비밀게시판", "HOT게시판", "정보게시판", "건강게시판", "트로트게시판", "재취업게시판", "반려견게시판")

        inner class CustomViewHolder(val binding : ItemBoardBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): BoardRecyclerViewAdapter.CustomViewHolder {
            val binding = ItemBoardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return CustomViewHolder(binding)
        }



        override fun onBindViewHolder(
            holder: BoardRecyclerViewAdapter.CustomViewHolder,
            position: Int
        ) {

            holder.binding.boardviewitemTextviewBoard.text = busanDTO[position]

            //게시판이 클릭 되었을 때
            holder.binding.boardviewitemTextviewBoard.setOnClickListener { v ->
                var intent = Intent(v.context, BoardContentActivity::class.java)
                intent.putExtra("destinationCategory", busanDTO[position])
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return busanDTO.size
        }

    }
}