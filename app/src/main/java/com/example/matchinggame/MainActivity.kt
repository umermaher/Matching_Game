package com.example.matchinggame

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import android.window.SplashScreen
import android.window.SplashScreenView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.matchinggame.adapter.MemoryCardAdapter
import com.example.matchinggame.models.BoardSize
import com.example.matchinggame.models.MemoryGame
import com.example.matchinggame.models.UserImageList
import com.example.matchinggame.utils.EXTRA_BOARD_SIZE
import com.example.matchinggame.utils.EXTRA_GAME_NAME
import com.example.matchinggame.utils.PrefsData
import com.example.matchinggame.viewmodel.GameViewModel
import com.example.matchinggame.viewmodel.MainViewModel
import com.github.jinatonic.confetti.CommonConfetti
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

@SuppressLint("SetTextI18n","ResourceAsColor")
class MainActivity : AppCompatActivity() {
    private lateinit var adapter: MemoryCardAdapter
    private lateinit var memoryGame: MemoryGame
    private var boardSize = BoardSize.MEDIUM
    private val db=Firebase.firestore
    private var gameName:String?=null
    private var customGameImages:List<String>?=null
    private val viewModel:MainViewModel by viewModels()
    private lateinit var musIntent:Intent

    companion object{
        // these are for activity results
        private const val TAG="MainActivity"
        private const val CREATE_REQUEST_CODE=99
        private const val DOWNLOAD_REQUEST_CODE=98
        private const val RC_SIGN_IN = 97
    }

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition{
                viewModel.isLoading.value
            }
        }

        if(!PrefsData.restorePrefsData(this)){
            startActivity(Intent(this,IntroScreen::class.java))
            finish()
        }

        setContentView(R.layout.activity_main)

        configureGoogleSignIn()

        musIntent=Intent(this,SoundService::class.java)
        if(PrefsData.restorePrefsData(this)){
            startService(musIntent)
        }

        setUpBoard()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_items,menu)
        //here you can change the color manually.
//        menu?.get(0)?.icon?.setTint(ContextCompat.getColor(this,R.color.white))
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_refresh -> {
                if(memoryGame.getNumMoves()>0 && !memoryGame.haveWonGame()){
                    showAlertDialog("Quit your current game?",null, View.OnClickListener {
                        setUpBoard()
                    })
                }else{
                    setUpBoard()
                }
            }
            R.id.action_new_size -> {
                showNewSizeDialog()
            }
            R.id.action_custom_game -> {
                val currentUser=auth.currentUser
                if(currentUser!=null)
                    showCreationDialog()
                else
                    signIn()
            }
            R.id.action_download_game -> {
                startActivityForResult(Intent(this,DownloadGameActivity::class.java),
                    DOWNLOAD_REQUEST_CODE)
            }
            R.id.action_music ->{
                if(item.title.equals("Mute")){
                    stopService(musIntent)
                    item.icon=ContextCompat.getDrawable(this,R.drawable.ic_music)
                    item.title="Unmute"
                }else{
                    startService(musIntent)
                    item.icon = ContextCompat.getDrawable(this, com.example.matchinggame.R.drawable.ic_music_off)
                    item.title="Mute"
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode== Activity.RESULT_OK) {
            if (requestCode == CREATE_REQUEST_CODE || requestCode == DOWNLOAD_REQUEST_CODE) {
                val customGameName = data?.getStringExtra(EXTRA_GAME_NAME) ?: return
                downloadGame(customGameName)
            }
            //when creating game user need to sign in for authentication
            if (requestCode == RC_SIGN_IN) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInTask(task)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun downloadGame(customGameName: String) {
        mainProgressBar.visibility = View.VISIBLE
        db.collection("games").document(customGameName).get().addOnSuccessListener {
            //it documentSnapshot
            val userImageList=it.toObject(UserImageList::class.java)
            if(userImageList?.images==null){
                Snackbar.make(parentLayout,"Sorry we couldn't found any game, '$customGameName'",Snackbar.LENGTH_SHORT)
                    .setAction("Ok"){}
                    .setActionTextColor(ContextCompat.getColor(this,R.color.white))
                    .show()
                return@addOnSuccessListener
            }
            val numCards=userImageList.images.size*2
            boardSize=BoardSize.getByValue(numCards)
            customGameImages=userImageList.images
            gameName=customGameName

            for(imageUrl in userImageList.images){
                Picasso.get().load(imageUrl).fetch()
            }

            Toast.makeText(this,"Just a second!",Toast.LENGTH_SHORT).show()
            setUpBoard()

            lifecycleScope.launch {
                timeForLoad()
            }
        }.addOnFailureListener {
            Toast.makeText(this,"Failed to download!",Toast.LENGTH_LONG).show()
            mainProgressBar.visibility=View.GONE
        }
    }

    //sometimes images load very slow due to network connection
    private suspend fun timeForLoad(){
        delay(3000)
        mainProgressBar.visibility = View.GONE
        Toast.makeText(this@MainActivity,"You're now playing $gameName!",Toast.LENGTH_LONG).show()
    }

    //for custom game user need to sign in
    private fun configureGoogleSignIn() {
        //configure google sign in
        val gso= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)
        auth= Firebase.auth
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun handleSignInTask(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mainProgressBar.visibility = View.VISIBLE
        GlobalScope.launch(Dispatchers.IO) {
            val auth = auth.signInWithCredential(credential).await()
//            val firebaseUser = auth.user
            withContext(Dispatchers.Main) {
                mainProgressBar.visibility = View.GONE
                showCreationDialog()
            }
        }
    }


    private fun showCreationDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        val radioGroupSize= boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)

        when (boardSize){
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
        }

        showAlertDialog("Create your own memory board",boardSizeView,View.OnClickListener {
            // Set a new value for the board size
            val desiredBoardSize = when(radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            val intent=Intent(this,CreateCustomGameActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE,desiredBoardSize)
            startActivityForResult(intent,CREATE_REQUEST_CODE)
        })
    }

    @SuppressLint("InflateParams")
    private fun showNewSizeDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        val radioGroupSize= boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        when (boardSize){
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
        }

        showAlertDialog("Choose new size",boardSizeView,View.OnClickListener {
            // Set a new value for the board size
            boardSize = when(radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            gameName=null
            customGameImages=null
            setUpBoard()
        })
    }

    private fun showAlertDialog(title: String, view: View?, positiveClickListener: View.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("cancel",null)
            .setPositiveButton("ok"){_,_ ->
                positiveClickListener.onClick(null)
            }.show()
    }

    private fun setUpBoard() {
        supportActionBar?.title=gameName?:getString(R.string.app_name)

        bestText.text="${PrefsData.getBest(this,boardSize)} moves"

        when(boardSize){
            BoardSize.EASY -> {
                numMovesText.text="Easy: 4 x 2"
                numPairsText.text="Pairs: 0 / 4"
            }
            BoardSize.MEDIUM -> {
                numMovesText.text="Medium: 6 x 3"
                numPairsText.text="Pairs: 0 / 9"
            }
            BoardSize.HARD -> {
                numMovesText.text="Hard: 6 x 4"
                numPairsText.text="Pairs: 0 / 12"
            }
        }
        memoryGame=MemoryGame(boardSize, customGameImages)

        numPairsText.setTextColor(ContextCompat.getColor(this,R.color.color_progress_none))

        adapter= MemoryCardAdapter(this, boardSize, memoryGame.cards, createCardClickListener())
        rvBoard.layoutManager=GridLayoutManager(this,boardSize.getWidth())
        rvBoard.adapter=adapter
        rvBoard.setHasFixedSize(true)
    }

    private fun createCardClickListener() = object : MemoryCardAdapter.OnCardClickListener{
        override fun onCardClicked(position: Int) {
            //Error checking
            if(memoryGame.haveWonGame()){
                //Alert the user of an invalid move
                Snackbar.make(parentLayout,"You already won!",Snackbar.LENGTH_LONG)
                    .setAction("Ok"){}
                    .setActionTextColor(ContextCompat.getColor(this@MainActivity,R.color.white))
                    .show()
                return
            }
            if(memoryGame.isCardFaceUp(position)){
                //Alert the user of an invalid move
                Snackbar.make(parentLayout,"Invalid move!",Snackbar.LENGTH_SHORT)
                    .setAction("Ok"){}
                    .setActionTextColor(ContextCompat.getColor(this@MainActivity,R.color.white))
                    .show()
                return
            }
            if(memoryGame.flipCard(position)){
                //Color will change from red to green
                val color = ArgbEvaluator().evaluate(
                    memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs().toFloat(),
                    ContextCompat.getColor(this@MainActivity,R.color.color_progress_none),
                    ContextCompat.getColor(this@MainActivity,R.color.color_progress_full)
                ) as Int

                numPairsText.setTextColor(color)
                numPairsText.text="Pairs : ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
                if(memoryGame.haveWonGame()) {
                    Snackbar.make(parentLayout, "You won!", Snackbar.LENGTH_LONG).show()
                    CommonConfetti.rainingConfetti(parentLayout, intArrayOf(Color.YELLOW,Color.BLUE,Color.RED,Color.GREEN,Color.MAGENTA)).oneShot()

                    if(PrefsData.isFirstTime(this@MainActivity,boardSize)){
                        bestText.text="${memoryGame.getNumMoves()} moves"
                        setBest()
                        PrefsData.notFirstTime(this@MainActivity,boardSize)
                    }

                    //storing best record if current game has best score
                    if(PrefsData.getBest(this@MainActivity,boardSize) > memoryGame.getNumMoves()){
                        bestText.text="${memoryGame.getNumMoves()} moves"
                        setBest()
                    }
                }
            }
            numMovesText.text="Moves: ${memoryGame.getNumMoves()}"
            adapter.notifyDataSetChanged()
        }
    }

    private fun setBest()=PrefsData.setBest(this@MainActivity,boardSize,memoryGame.getNumMoves())

    override fun onDestroy() {
        stopService(musIntent)
        super.onDestroy()
    }
}
