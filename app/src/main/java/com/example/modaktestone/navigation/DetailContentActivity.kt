package com.example.modaktestone.navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.modaktestone.R
import com.example.modaktestone.databinding.ActivityDetailContentBinding
import com.example.modaktestone.databinding.ItemCommentBinding
import com.example.modaktestone.navigation.model.AlarmDTO
import com.example.modaktestone.navigation.model.ContentDTO
import com.example.modaktestone.navigation.model.UserDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.android.ext.android.bind

class DetailContentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailContentBinding

    var destinationUserName: String? = null
    var destinationTitle: String? = null
    var destinationExplain: String? = null
    var destinationTimestamp: String? = null
    var destinationCommentCount: Int? = 0
    var destinationFavoriteCount: Int? = 0
    var destinationUid: String? = null
    var contentUid: String? = null

    var uid: String? = null

    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_detail_content)
        binding = ActivityDetailContentBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //변수 초기화
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        //컨텐츠 내용 띄우기
        binding.detailcontentTextviewUsername.text = intent.getStringExtra("destinationUsername")
        binding.detailcontentTextviewTitle.text = intent.getStringExtra("destinationTitle")
        binding.detailcontentTextviewExplain.text = intent.getStringExtra("destinationExplain")
        binding.detailcontentTextviewTimestamp.text = intent.getStringExtra("destinationTimestamp")
        binding.detailcontentTvCommentcount.text = intent.getStringExtra("destinationCommentCount")
        binding.detailcontentTvFavoritecount.text = intent.getStringExtra("destinationFavoriteCount")
        destinationUid = intent.getStringExtra("destinationUid")
        contentUid = intent.getStringExtra("contentUid")

        println(contentUid.toString())

        binding.detailcontentRecyclerview.adapter = DetailContentRecycleViewAdapter()
        binding.detailcontentRecyclerview.layoutManager = LinearLayoutManager(this)

        //댓글업로드 클릭 되었을 때
        binding.detailcontentBtnCommentupload.setOnClickListener {
            commentUpload()
            getCommentCount(contentUid!!)
            commentAlarm(destinationUid!!, binding.detailcontentEdittextComment.text.toString())
        }

        //좋아요 버튼 클릭되었을 때
        binding.detailcontentLinearFavoritebtn.setOnClickListener {
            favoriteEvent(contentUid!!)
        }

    }

    inner class DetailContentRecycleViewAdapter :
        RecyclerView.Adapter<DetailContentRecycleViewAdapter.CustomViewHolder>() {
        var comments: ArrayList<ContentDTO.Comment> = arrayListOf()

        init {
            FirebaseFirestore.getInstance().collection("contents").document(contentUid!!)
                .collection("comments").orderBy("timestamp")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    comments.clear()
                    if (querySnapshot == null) return@addSnapshotListener

                    for (snapshot in querySnapshot.documents!!) {
                        comments.add(snapshot.toObject(ContentDTO.Comment::class.java)!!)
                    }
                    notifyDataSetChanged()
                }
        }

        inner class CustomViewHolder(val binding: ItemCommentBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): DetailContentRecycleViewAdapter.CustomViewHolder {
            val binding =
                ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return CustomViewHolder(binding)
        }

        override fun onBindViewHolder(
            holder: DetailContentRecycleViewAdapter.CustomViewHolder,
            position: Int
        ) {
            holder.binding.commentitemTextviewUsername.text = comments[position].userName
            holder.binding.commentitemTextviewComment.text = comments[position].comment
            holder.binding.commentitemTextviewTimestamp.text =
                comments[position].timestamp.toString()
        }

        override fun getItemCount(): Int {
            return comments.size
        }

    }

    fun commentUpload() {
        var uid = auth?.currentUser?.uid
        var username: String? = null
        var region: String? = null
        firestore?.collection("users")?.document(uid!!)
            ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (documentSnapshot == null) return@addSnapshotListener
                var userDTO = documentSnapshot.toObject(UserDTO::class.java)
                username = userDTO?.userName
                region = userDTO?.region

                var comment = ContentDTO.Comment()
                comment.uid = FirebaseAuth.getInstance().currentUser?.uid
                comment.comment = binding.detailcontentEdittextComment.text.toString()
                comment.timestamp = System.currentTimeMillis()
                comment.userName = username

                FirebaseFirestore.getInstance().collection("contents").document(contentUid!!)
                    .collection("comments").document().set(comment)

                binding.detailcontentEdittextComment.setText("")
            }
    }

    fun favoriteEvent(contentUid: String) {
        var tsDoc = firestore?.collection("contents")?.document(contentUid)
        firestore?.runTransaction { transaction ->
            var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)
            //만약 현재 유저 유아디가 페이버릿을 누른적이 있따면
            if (contentDTO!!.favorites.containsKey(uid)) {
                contentDTO?.favoriteCount = contentDTO?.favoriteCount - 1
                contentDTO?.favorites.remove(uid)
            } else {
                //현재 유저가 페이버릿 누른적이 없다면
                contentDTO?.favoriteCount = contentDTO?.favoriteCount + 1
                contentDTO?.favorites[uid!!] = true
                favoriteAlarm(destinationUid!!)
            }
            transaction.set(tsDoc, contentDTO)
            return@runTransaction
        }
    }

    fun getCommentCount(contentUid: String) {
        var tsDoc = firestore?.collection("contents")?.document(contentUid)
        firestore?.runTransaction { transaction ->
            var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)
            if(contentDTO != null){
                contentDTO?.commentCount = contentDTO?.commentCount!! + 1
            }
            transaction.set(tsDoc, contentDTO!!)
            return@runTransaction
        }
    }

    fun favoriteAlarm(destinationUid: String) {
        firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null)return@addSnapshotListener
            var userDTO = documentSnapshot.toObject(UserDTO::class.java)
            var alarmDTO = AlarmDTO()
            alarmDTO.destinationUid = destinationUid
            alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
            alarmDTO.userName = userDTO?.userName
            alarmDTO.kind = 0
            alarmDTO.timestamp = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
        }

    }

    fun commentAlarm(destinationUid: String, message: String) {
        firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null)return@addSnapshotListener
            var userDTO = documentSnapshot.toObject(UserDTO::class.java)
            var alarmDTO = AlarmDTO()
            alarmDTO.destinationUid = destinationUid
            alarmDTO.message = message
            alarmDTO.userName = userDTO?.userName
            alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
            alarmDTO.kind = 1
            alarmDTO.timestamp = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
        }
    }
}


