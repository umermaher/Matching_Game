package com.example.matchinggame

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.RELATIVE_TO_PARENT
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.recyclerview.widget.GridLayoutManager
import com.example.matchinggame.models.BoardSize
import com.example.matchinggame.models.MemoryCard
import com.example.matchinggame.models.MemoryGame
import com.example.matchinggame.models.UserImageList
import com.example.matchinggame.utils.DEFAULT_ICONS
import com.example.matchinggame.utils.EXTRA_BOARD_SIZE
import com.example.matchinggame.utils.EXTRA_GAME_NAME
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

@SuppressLint("SetTextI18n","ResourceAsColor")
class MainActivity : AppCompatActivity() {
    private lateinit var adapter: MemoryCardAdapter
    private lateinit var memoryGame: MemoryGame
    private var boardSize = BoardSize.EASY
    private val db=Firebase.firestore
    private var gameName:String?=null
    private var customGameImages:List<String>?=null

    companion object{
        private const val CREATE_REQUEST_CODE=99
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpBoard()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_items,menu)
        //here you can change the color manually.
//        menu?.get(0)?.icon?.setTint(ContextCompat.getColor(this,R.color.white))
        return true
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
                showCreationDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCreationDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        val radioGroupSize= boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== CREATE_REQUEST_CODE && resultCode== Activity.RESULT_OK){
            val customGameName= data?.getStringExtra(EXTRA_GAME_NAME) ?: return
            downloadGame(customGameName)
        }
    }

    private fun downloadGame(customGameName: String) {
        db.collection("games").document(customGameName).get().addOnSuccessListener {
            //it documentSnapshot
            val userImageList=it.toObject(UserImageList::class.java)
            if(userImageList?.images==null){
                Snackbar.make(parentLayout,"Sorry we couldn't found any game, '$customGameName'",Snackbar.LENGTH_SHORT)
                return@addOnSuccessListener
            }
            val numCards=userImageList.images.size*2
            boardSize=BoardSize.getByValue(numCards)
            customGameImages=userImageList.images
            setUpBoard()
            gameName=customGameName
        }
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

        adapter=MemoryCardAdapter(this, boardSize, memoryGame.cards, createCardClickListener())
        rvBoard.layoutManager=GridLayoutManager(this,boardSize.getWidth())
        rvBoard.adapter=adapter
        rvBoard.setHasFixedSize(true)
    }

    private fun createCardClickListener() = object : MemoryCardAdapter.OnCardClickListener{
        override fun onCardClicked(position: Int) {
            //Error checking
            if(memoryGame.haveWonGame()){
                //Alert the user of an invalid move
                Snackbar.make(parentLayout,"You already won!",Snackbar.LENGTH_LONG).show()
                return
            }
            if(memoryGame.isCardFaceUp(position)){
                //Alert the user of an invalid move
                Snackbar.make(parentLayout,"Invalid move!",Snackbar.LENGTH_SHORT).show()
                return
            }
            if(memoryGame.flipCard(position)){
                val color = ArgbEvaluator().evaluate(
                    memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs().toFloat(),
                    ContextCompat.getColor(this@MainActivity,R.color.color_progress_none),
                    ContextCompat.getColor(this@MainActivity,R.color.color_progress_full)
                ) as Int
                numPairsText.setTextColor(color)
                numPairsText.text="Pairs : ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
                if(memoryGame.haveWonGame()) {
                    Snackbar.make(parentLayout, "You won!", Snackbar.LENGTH_LONG).show()
                }
            }
            numMovesText.text="Moves: ${memoryGame.getNumMoves()}"
            adapter.notifyDataSetChanged()
        }
    }
}
