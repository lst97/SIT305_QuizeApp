package com.example.quizeapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import java.time.LocalTime


class MainActivity : AppCompatActivity() {
    // starting point
    private fun initProgram(){
        val homePage = initHomePageUI()
        val questionPage = initQuestionUI(homePage.quizData)
        val resultPage = initResultUI()

        homePage.addHook("question", questionPage.container)
        homePage.addHook("result", resultPage.container)
        questionPage.addHook("result", resultPage.container)
        resultPage.addHook("home", homePage.container)

        homePage.show()
        questionPage.hidden()
        resultPage.hidden()
    }

    private fun initHomePageUI(): HomePageUI {
        val appTitle = findViewById<TextView>(R.id.mainAppTitle)
        val nameInputTitle = findViewById<TextView>(R.id.mainInputLabel)
        val nameInput = findViewById<EditText>(R.id.mainNameInput)
        val startBtn = findViewById<Button>(R.id.mainStartBtn)
        val container = findViewById<ConstraintLayout>(R.id.home)
        val currentTime = LocalTime.now()

        // Model
        val quiz = Quiz("SIT305 Quiz App", "", currentTime, isStarted = false, isFinished = false)
        // every ui component instances in home page
        return HomePageUI(appTitle, nameInputTitle, nameInput, startBtn, container, quiz, context = applicationContext)
    }

    // init Quiz
    private fun initQuestionUI(quiz: Quiz): QuestionUI {
        // UI
        val welcomeTitle: TextView = findViewById(R.id.questionWelcomeText)
        val progressIndex: TextView = findViewById(R.id.questionProgressIndexText)
        val progressBar: ProgressBar = findViewById(R.id.questionProgressBar)
        val questionTitle: TextView = findViewById(R.id.questionQuestionTitleText)
        val questionContent: TextView = findViewById(R.id.questionQuestionContentText)
        val option1: Button = findViewById(R.id.questionOption1)
        val option2: Button = findViewById(R.id.questionOption2)
        val option3: Button = findViewById(R.id.questionOption3)
        val option4: Button = findViewById(R.id.questionOption4)
        val optionBtns: List<Button> = listOf(option1, option2, option3, option4)
        val container: ConstraintLayout = findViewById(R.id.question)
        val next: Button = findViewById(R.id.questionNext)

        // Model
        initQuestions(quiz)

        return QuestionUI(
            welcomeTitle,
            progressIndex,
            progressBar,
            questionTitle,
            questionContent,
            optionBtns,
            next,
            container,
            quiz,
            context = applicationContext
        )

    }
    private fun initResultUI() : ResultUI{
        // UI
        val title:TextView = findViewById(R.id.resultCongraText)
        val scoreLabel: TextView = findViewById(R.id.resultScoreLabelText)
        val score: TextView = findViewById(R.id.resultScoreText)
        val newQuiz : Button = findViewById(R.id.resultNewQuiz)
        val finishBtn : Button = findViewById(R.id.resultFinish)
        val container : ConstraintLayout = findViewById(R.id.result)

        finishBtn.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.exit_dialog_title)
            builder.setMessage(R.string.exit_dialog_message)
            builder.setPositiveButton(R.string.exit_dialog_confirm) { _, _ ->
                finish()
            }
            builder.setNegativeButton(R.string.exit_dialog_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }

        return ResultUI(title, scoreLabel, score, newQuiz, finishBtn, container, context = applicationContext)
    }

    // init QuizData
    private fun initQuestions(quiz: Quiz){
        // add options here
        val options: MutableList<Option> = mutableListOf()

        // Q1
        options.clear()
        options.add(Option(0, "A language used to communicate with computers"))
        options.add(Option(1, "A language used to communicate with humans"))
        options.add(Option(2, "A language used to communicate with animals"))
        options.add(Option(3, "A language used to communicate with plants"))
        var question = Question("What is a programming language?", "", options.toList(), 0)
        quiz.addQuestion(question)

        // Q2
        options.clear()
        options.add(Option(0, "A computer program"))
        options.add(Option(1, "A step-by-step procedure for solving a problem"))
        options.add(Option(2, "A type of data structure"))
        options.add(Option(3, "An input/output device"))
        question = Question("What is an algorithm?", "", options.toList(), 1)
        quiz.addQuestion(question)

        // Q3
        options.clear()
        options.add(Option(0, "Integer"))
        options.add(Option(1, "String"))
        options.add(Option(2, "Boolean"))
        options.add(Option(3, "All of the above"))
        question = Question(
            "Which of the following is a fundamental data type in programming?",
            "",
            options.toList(),
            3
        )
        quiz.addQuestion(question)

        // Q4
        options.clear()
        options.add(Option(0, "A type of data structure"))
        options.add(Option(1, "A section of code that is executed repeatedly"))
        options.add(Option(2, "A way to connect two computers"))
        options.add(Option(3, "A type of error in programming"))
        question = Question("What is a loop in programming?", "", options.toList(), 1)
        quiz.addQuestion(question)

        // Q5
        options.clear()
        options.add(Option(0, "Assembly language"))
        options.add(Option(1, "C++"))
        options.add(Option(2, "Java"))
        options.add(Option(3, "Machine language"))
        question = Question(
            "Which of the following is a high-level programming language?",
            "",
            options.toList(),
            2
        )
        quiz.addQuestion(question)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initProgram()
    }
}