package com.example.modaktestone.navigation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.modaktestone.MainActivity
import com.example.modaktestone.R
import com.example.modaktestone.databinding.ActivitySelectRegionBinding
import com.example.modaktestone.navigation.model.UserDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SelectRegionActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySelectRegionBinding

    var uid : String? = null
    var region : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectRegionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        uid = FirebaseAuth.getInstance().currentUser?.uid

        val data = listOf("서울", "부산", "대구", "인천", "광주", "대전", "울산", "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주")

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, data)

        binding.selectregionSpinner.adapter = adapter
        binding.selectregionSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                region = data[position]
                //여기에 이 스트링값을 보내면 된다.
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.selectregionBtn.setOnClickListener {
            usernameAndRegion()
            moveMainPage()

        }
    }

    fun usernameAndRegion() {
        var userDTO = UserDTO()
        userDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
        userDTO.region = region
        userDTO.userName = binding.selectregionEdittextName.text.toString()
        FirebaseFirestore.getInstance().collection("users").document(uid!!).set(userDTO)
    }

    fun moveMainPage() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}