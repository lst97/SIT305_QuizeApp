package com.example.quizeapp
import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import java.time.Duration
import java.time.LocalTime

interface IViewHook{
    val name:String
    val container:ConstraintLayout
}
class ViewHook(override val name: String, override val container: ConstraintLayout) : IViewHook

// UI
interface IView {
    // views that can be navigate
    var hooks : MutableList<ViewHook>
    // current view
    val container: ConstraintLayout
    fun hidden() {
        container.visibility = View.INVISIBLE
    }

    fun show() {
        container.visibility = View.VISIBLE
    }
    fun getContainerByName(name:String) : ConstraintLayout? {
        val hook = hooks.find { it.name == name }
        if (hook != null){
            return hook.container
        }
        return null
    }

    fun getHookByName(name: String) : ViewHook?{
        return hooks.find { it.name == name }
    }

    fun addHook(name: String, newContainer : ConstraintLayout){
        val container : ConstraintLayout? = getContainerByName(name)
        if(container == null){
            hooks.add(ViewHook(name, newContainer))
        }
    }
    fun removeHook (name:String){
        val hook = getHookByName(name)
        hooks.remove(hook)
    }

    fun setup()
}

interface IHomePageUI: IView{
    val appTitle : TextView
    val nameInputTitle: TextView
    val nameInput : EditText
    val startBtn : Button
    val quizData: Quiz
    val context: Context
}

interface IQuestionUI: IView{
    val welcomeLabel : TextView
    val progressLabel : TextView
    val progressBar : ProgressBar
    val questionTitleLabel : TextView
    val questionContentLabel : TextView

    val optionBtns: List<Button>
    val nextBtn : Button

    // quiz data
    val quiz : Quiz

    var currentQuestion : Int

    fun renderQuestion()
}
interface IResultUI: IView{
    val titleLabel : TextView
    val scoreTitleLabel : TextView
    val scoreLabel : TextView
    val newQuizBtn : Button
    val finishQuizBtn : Button
    val context : Context
}

class HomePageUI(
    override val appTitle: TextView,
    override val nameInputTitle: TextView,
    override val nameInput: EditText,
    override val startBtn: Button,
    override val container: ConstraintLayout,
    override val quizData: Quiz,
    override var hooks: MutableList<ViewHook> = mutableListOf(),
    override val context: Context
) : IHomePageUI{

    init {
        setup()
    }


    private fun nextPage(){
        hidden()
        val questionContainer = getContainerByName("question")
        questionContainer?.visibility = View.VISIBLE
    }

    private fun inputOnChange(){
        nameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Check if the EditText is empty
                val isEmpty = s?.isEmpty() ?: true

                // Enable or disable the button based on the EditText's contents
                startBtn.isEnabled = !isEmpty
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    @SuppressLint("SetTextI18n")
    override fun setup() {
        nameInput.hint = "Enter your name"

        startBtn.isEnabled = false
        inputOnChange()

        startBtn.setOnClickListener{
            val name = nameInput.text?.toString()
            if (name != "" && name != null){

                // Set welcome msg from homepage view to question view
                quizData.name = name
                val question = getContainerByName("question")
                val welcomeText = question?.findViewById<TextView>(R.id.questionWelcomeText)
                welcomeText?.text = context.resources.getString(R.string.home_welcome_text, quizData.name)

                val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(nameInput.windowToken, 0)
                inputMethodManager.hideSoftInputFromWindow(startBtn.windowToken, 0)

                val result = getContainerByName("result")
                val resultCongraText = result?.findViewById<TextView>(R.id.resultCongraText)
                resultCongraText?.text ="Congratulation ${quizData.name}"

            }

            nextPage()
            quizData.start()
        }

    }
}
class Bind(optionBtns: List<Button>) {
    var map = mutableMapOf<Int, Int>()

    init {
        for ((index, btn) in optionBtns.withIndex()){
            map[btn.id] = index
        }
    }
}
class QuestionUI(
    override val welcomeLabel: TextView,
    override val progressLabel: TextView,
    override val progressBar: ProgressBar,
    override val questionTitleLabel: TextView,
    override val questionContentLabel: TextView,
    override val optionBtns: List<Button>,
    override val nextBtn: Button,
    override val container: ConstraintLayout,
    override val quiz: Quiz,
    override var hooks: MutableList<ViewHook> = mutableListOf(),
    private  val context : Context

) : IQuestionUI{

    private lateinit var btnBinding : Bind
    override var currentQuestion: Int = 0

    private fun register(){
        // bind btn id with question value
        btnBinding = Bind(optionBtns)
    }

    private fun correct(btn: View){
        val button = btn as Button
        button.setBackgroundColor(ContextCompat.getColor(context, R.color.green_200))
        quiz.correctCount += 1

    }
    private fun disableOptionBtns(){
        for(btn in optionBtns){
            btn.isEnabled = false
        }
    }
    private fun enableOptionBtns(){
        for(btn in optionBtns){
            btn.isEnabled = true
        }
    }
    private fun optionBtnsCallback(){
        nextBtn.isEnabled = true
        for(optionBtn in optionBtns){
            optionBtn.setOnClickListener{
                nextBtn.isEnabled = true
                val selectedOptionId = this.btnBinding.map[it.id]
                if (selectedOptionId != null) {
                    disableOptionBtns()
                    if (quiz.questions[currentQuestion].check(selectedOptionId)){
                        correct(it)
                    }else{
                        incorrect(it)
                    }
                    if (currentQuestion == quiz.questions.size){
                        quiz.end()
                    }
                }
            }
        }
    }

    override fun renderQuestion() {
        questionTitleLabel.text = quiz.questions[currentQuestion].title
        questionContentLabel.text = quiz.questions[currentQuestion].content

        for((index, option) in quiz.questions[currentQuestion].options.withIndex()){
            optionBtns[index].setBackgroundColor(ContextCompat.getColor(context, R.color.purple_500))
            optionBtns[index].setTextColor(ContextCompat.getColor(context, R.color.white))
            optionBtns[index].text = option.content
        }
    }

    @SuppressLint("SetTextI18n")
    private fun renderProgress(){
        val currentProgress = currentQuestion + 1
        progressLabel.text = "$currentProgress / ${quiz.totalQuestions}"
        val progressPercentage = ((currentProgress / quiz.totalQuestions.toDouble()) * 100).toInt()
        progressBar.setProgress(progressPercentage, true)

    }

    private fun renderWelcome(){
        questionTitleLabel.text = context.getString(R.string.home_welcome_text)
    }

    private fun pageContent(){
        renderWelcome()
        renderProgress()
        renderQuestion()
    }

    @SuppressLint("SetTextI18n")
    private fun resultPage(){
        val resultContainer = getContainerByName("result")
        hidden()
        resultContainer?.visibility = View.VISIBLE

        val scoreText = resultContainer?.findViewById<TextView>(R.id.resultScoreText)
        scoreText?.text = "${quiz.correctCount} / ${quiz.totalQuestions}"
    }

    private fun nextBtnCallback(){
        nextBtn.setOnClickListener{
            if(currentQuestion == quiz.questions.size - 1){
                currentQuestion = 0
                resultPage()
            }else{
                currentQuestion += 1
                nextBtn.isEnabled = false
            }
            renderQuestion()
            renderProgress()
            enableOptionBtns()
        }
    }
    private fun incorrect(btn : View){
        val button = btn as Button
        button.setBackgroundColor(ContextCompat.getColor(context, R.color.red_200))

        // show correct answer
        val buttonIndex = quiz.questions[currentQuestion].correctOption
        val correctAnswerButton = optionBtns[buttonIndex]
        correctAnswerButton.setBackgroundColor(ContextCompat.getColor(context, R.color.green_200))
    }
    override fun setup() {
        // register btn with id
        register()
        optionBtnsCallback()
        pageContent()

        nextBtn.text = context.getString(R.string.btn_next)
        nextBtn.isEnabled = false
        nextBtnCallback()
    }

    init {
        setup()
    }
}

class ResultUI(
    override val titleLabel: TextView,
    override val scoreTitleLabel: TextView,
    override val scoreLabel: TextView,
    override val newQuizBtn: Button,
    override val finishQuizBtn: Button,
    override val container: ConstraintLayout,
    override var hooks: MutableList<ViewHook> = mutableListOf(),
    override val context: Context

) : IResultUI{
    private fun pageContent(){
        scoreTitleLabel.text = context.getString(R.string.result_score_title)
        newQuizBtn.text =  context.getString(R.string.result_btn_new_quiz)
        finishQuizBtn.text = context.getString(R.string.result_btn_finish_quiz)
    }

    private fun btnCallbacks(){
        newQuizBtn.setOnClickListener{
            val homeContainer = getContainerByName("home")
            hidden()
            homeContainer?.visibility = View.VISIBLE
        }
    }

    override fun setup() {
        pageContent()
        btnCallbacks()
    }

    init {
        setup()
    }
}
// Model
interface IOption{
    var id : Int
    var content : String

}

interface IQuestion{
    var title : String
    var content: String

    var options: List<Option>
    var correctOption : Int

    fun check(id: Int) : Boolean
}

interface IQuiz {
    var title : String
    var name : String
    var time : LocalTime
    var isStarted : Boolean
    var isFinished : Boolean
    var totalQuestions : Int
    var correctCount : Int

    // initial questions
    var questions: MutableList<Question>
    fun addQuestion(question:Question){
        questions.add(question)
        totalQuestions = questions.size
    }

    // not recommend to use, too much prams
    fun addQuestion(title:String, content:String, option: Option, correctOption: Option)

    // start quiz
    fun start()

    // quiz ended
    fun end() : Duration
}

class Option(override var id: Int, override var content: String) : IOption

class Question// index from the list
    (
    override var title: String,
    override var content: String,
    override var options: List<Option>,
    correctOptionID: Int
) : IQuestion{
    override var correctOption : Int = correctOptionID

    override fun check(id : Int): Boolean {
        if (id == correctOption){
            return true
        }
        return false
    }

}
class Quiz(
    override var title: String,
    override var name: String,
    override var time: LocalTime,
    override var isStarted: Boolean,
    override var isFinished: Boolean
) :IQuiz {
    override var questions: MutableList<Question> = mutableListOf()
    override var totalQuestions = questions.size
    override var correctCount: Int = 0

    override fun addQuestion(
        title: String,
        content: String,
        option: Option,
        correctOption: Option
    ) {
        TODO("Not yet implemented")
    }

    override fun start() {
        correctCount = 0
        time = LocalTime.now()
    }

    override fun end() : Duration{
        return Duration.between(time, LocalTime.now())
    }
}
