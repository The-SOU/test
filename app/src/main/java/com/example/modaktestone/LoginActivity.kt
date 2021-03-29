package com.example.modaktestone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.modaktestone.databinding.ActivityLoginBinding
import com.example.modaktestone.navigation.SelectRegionActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.kakao.sdk.auth.LoginClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.get
import retrofit2.Retrofit
import java.lang.reflect.Array.get

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    var auth: FirebaseAuth? = null
    var googleSignInClient: GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
//        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("107280891504-iambicb740bfiffs3ckmcf31fv0rkar8.apps.googleusercontent.com")
            .requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.googleLoginButton.setOnClickListener {
            //First step
            googleLogin()
        }
        binding.kakaoLoginButton.setOnClickListener {
            signinEmail()
        }
        binding.emailLoginButton.setOnClickListener {
            signInAndSignUp()
        }
    }

//    private fun getFirebaseJwt(accessToken: String, type:Int) {
//        val firebaseCustomTokenApi : Retrofit = get()
//
//        val observable: Disposable =
//            firebaseCustomTokenApi
//                .create(FirebaseCustomTokenAPI)
//                .getFirebaseKakaoCustomToken(accessToken)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ i ->     /*성공*/
//                }, { error -> /*에러*/
//                }).addTo(disposables)
//    }

    fun signInAndSignUp() {
        auth?.createUserWithEmailAndPassword(
            binding.emailEdtitext.text.toString(),
            binding.passwordEdtitext.text.toString()
        )?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //Creating a user Account
                moveSelectRegionPage(task.result?.user)
            } else if (!task.exception?.message.isNullOrEmpty()) {
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            } else {
                signinEmail()
            }
        }
    }

    fun signinEmail() {
        auth?.signInWithEmailAndPassword(
            binding.emailEdtitext.text.toString(),
            binding.passwordEdtitext.text.toString()
        )?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                moveMainpage(task.result?.user)
            } else {
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun kakaoLogin() {
// 로그인 공통 callback 구성
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e("TAG", "로그인 실패", error)
            } else if (token != null) {
                Log.i("TAG", "로그인 성공 ${token.accessToken}")
            }
        }

// 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (LoginClient.instance.isKakaoTalkLoginAvailable(this)) {
            LoginClient.instance.loginWithKakaoTalk(this, callback = callback)
        } else {
            LoginClient.instance.loginWithKakaoAccount(this, callback = callback)
        }

    }

    fun moveMainpage(user: FirebaseUser?) {
        if(user != null){
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    fun moveSelectRegionPage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, SelectRegionActivity::class.java))
            finish()
        }
    }

    fun googleLogin() {
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }


    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //Login
                moveSelectRegionPage(task.result?.user)
            } else {
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_LOGIN_CODE) {
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result!!.isSuccess) {
                //이 값을 파이어베이스에 넘길 수 있게끔 만들어주도록 하겠다.
                var account = result.signInAccount
                //두번째
                firebaseAuthWithGoogle(account)

            }
        }
    }
}