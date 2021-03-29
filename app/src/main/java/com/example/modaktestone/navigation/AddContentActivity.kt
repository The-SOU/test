package com.example.modaktestone.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.modaktestone.databinding.ActivityAddContentBinding
import com.example.modaktestone.navigation.model.ContentDTO
import com.example.modaktestone.navigation.model.UserDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.kakao.sdk.common.KakaoSdk.init
import org.koin.android.ext.android.bind
import java.text.SimpleDateFormat
import java.util.*

class AddContentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddContentBinding

    var selectedCategory: String? = null

    var PICK_IMAGE_FROM_ALBUM = 0
    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddContentBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //인텐트 값 받기
        if(intent.hasExtra("selectedCategory")){
            selectedCategory = intent.getStringExtra("selectedCategory")
        } else {
            Toast.makeText(this, "전달된 이름이 없다", Toast.LENGTH_SHORT).show()
        }


        //초기화
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        //카메라 버튼 클릭했을 때 사진 선택하는 곳으로 넘어가기.
        binding.addcontentImageviewCamera.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)
        }

        binding.addcontentImageviewImage.visibility = View.INVISIBLE

        binding.addcontentButtonUpload.setOnClickListener {

            contentUpload()

            //username, region 얻기.

        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if (resultCode == Activity.RESULT_OK) {
                photoUri = data?.data
                binding.addcontentImageviewImage.setImageURI(photoUri)
                binding.addcontentImageviewImage.visibility = View.VISIBLE
            } else {
                finish()
            }
        }
    }

    // ----- 펑션 모음 -----
    fun contentUpload() {
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"
        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        var uid = auth?.currentUser?.uid
        var username : String? = null
        var region : String? = null
        firestore?.collection("users")?.document(uid!!)
            ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (documentSnapshot == null) return@addSnapshotListener
                var userDTO = documentSnapshot.toObject(UserDTO::class.java)
                username = userDTO?.userName
                region = userDTO?.region


                //컨텐츠업로드
                //이미지 유알아이가 존재 유뮤에 따라 구분
                if (photoUri != null) {
                    //image uri가 존재할 때
                    storageRef?.putFile(photoUri!!)
                        ?.continueWith { task: com.google.android.gms.tasks.Task<UploadTask.TaskSnapshot> ->
                            return@continueWith storageRef.downloadUrl
                        }?.addOnCompleteListener { uri ->
                            var contentDTO = ContentDTO()

                            contentDTO.imageUrl = uri.toString()

                            contentDTO.uid = auth?.currentUser?.uid

                            contentDTO.userName = username

                            contentDTO.region = region

                            contentDTO.title = binding.addcontentEdittextTitle.text.toString()

                            contentDTO.explain = binding.addcontentEdittextExplain.text.toString()

                            contentDTO.contentCategory = selectedCategory

                            contentDTO.postCount = contentDTO.postCount + 1

                            contentDTO.timestamp = System.currentTimeMillis()

                            firestore?.collection("contents")?.add(contentDTO)?.addOnSuccessListener { documentReference ->
                                Log.d("TAG", "DocumentSnapshot written with ID: ${documentReference.id}")
                            }?.addOnFailureListener { e ->
                                Log.w("TAG", "Error adding document", e)
                            }
                            setResult(Activity.RESULT_OK)

                            finish()
                        }
                } else {
                    //image uri가 존재하지 않을 때
                    var contentDTO = ContentDTO()

                    contentDTO.uid = auth?.currentUser?.uid

                    contentDTO.userName = username

                    contentDTO.region = region

                    contentDTO.title = binding.addcontentEdittextTitle.text.toString()

                    contentDTO.explain = binding.addcontentEdittextExplain.text.toString()

                    contentDTO.contentCategory = selectedCategory

                    contentDTO.timestamp = System.currentTimeMillis()

                    firestore?.collection("contents")?.add(contentDTO)?.addOnSuccessListener { documentReference ->
                        Log.d("TAG", "DocumentSnapshot written with ID: ${documentReference.id}")
                    }?.addOnFailureListener { e ->
                        Log.w("TAG", "Error adding document", e)
                    }
                    setResult(Activity.RESULT_OK)

                    finish()
                }

            }
    }
}