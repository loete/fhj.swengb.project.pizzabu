package fhj.swengb.project

import java.io.File
import java.net.URL
import java.nio.file.{Files, Paths, Path}
import java.util.ResourceBundle
import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.fxml.{FXML, FXMLLoader, Initializable}
import javafx.scene.control.{TextField, Button, TableColumn, TableView}
import javafx.scene.layout.{BorderPane, AnchorPane}
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage

import fhj.swengb.project.Highscore._

import scala.collection.immutable.IndexedSeq
import scala.util.Random
import scala.util.control.NonFatal

/**
 * Created by KnallerMJ on 14.01.16.
 */

// companion object
object PizzaBuApp {
  def main(args: Array[ String ]) {
    Application.launch(classOf[ PizzaBuAppFX ], args: _*)
  }
}

case class PizzaBuAppFX() extends Application {

  val loader = new FXMLLoader(getClass.getResource("GUI-Startscreen.fxml"))

  override def start(stage: Stage): Unit =
    try {
      stage.setTitle("PizzaBu - Die Pizza kommt in nu!")
      loader.load[Parent]()
      stage.setScene(new Scene(loader.getRoot[ Parent ]))

      stage.show()
    } catch {
      case NonFatal(e) => e.printStackTrace()
    }
}

case class CircleAnimation(circles: Seq[ Circle ]) extends AnimationTimer {

  // every tick this method is called - you are free to do whatever you want
  // in this method. maybe animate something, maybe something else ...
  override def handle(now: Long): Unit = {
    circles.foreach {
      case c =>
        c.setCenterX(c.getCenterX + Random.nextDouble * 4 - 2)
        c.setCenterY(c.getCenterY + Random.nextDouble * 4 - 2)
    }

  }
}


case class PizzaBuAppStartController() extends Initializable{

  @FXML var borderTop: BorderPane = _
  @FXML var start: Button = _
  @FXML var highscore: Button = _
  @FXML var help: Button = _
  @FXML var exit: Button = _

  def onStart(): Unit = {
    val loaderGame = new FXMLLoader(getClass.getResource("PizzaBude.fxml"))
    val gameStage = new Stage()

    gameStage.setTitle("PizzaBu - Die Pizza kommt in nu!")
    loaderGame.load[Parent]()
    gameStage.setScene(new Scene(loaderGame.getRoot[ Parent ]))
    gameStage.getScene.getStylesheets.add("/fhj/swengb/project/PizzaBude.css")

    gameStage.show()
    borderTop.getScene.getWindow.hide()

  }

  def goToHighScore(): Unit = {
    val loaderScore = new FXMLLoader(getClass.getResource("GUI-Highscore.fxml"))
    val highScoreStage = new Stage()

    highScoreStage.setTitle("PizzaBu - HighScore!")
    loaderScore.load[Parent]()
    highScoreStage.setScene(new Scene(loaderScore.getRoot[ Parent ]))

    highScoreStage.show()
    borderTop.getScene.getWindow.hide()

  }

  def onHelp(): Unit = {
    val loaderHelp = new FXMLLoader(getClass.getResource("GUI-Help.fxml"))
    val helpStage = new Stage()

    helpStage.setTitle("PizzaBu - Help!")
    loaderHelp.load[Parent]()
    helpStage.setScene(new Scene(loaderHelp.getRoot[ Parent ]))

    helpStage.show()
  }

  def onExit(): Unit = {
    borderTop.getScene.getWindow.hide()
  }

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    var path: Path = Paths.get("C:\\PizzaBu")

    if (Files.exists(path) == false){

      val dir: File = new File("C:\\PizzaBu")
      // attempt to create the directory
      dir.mkdir()

    }
  }
}


case class PizzaBuAppHighscoreController() extends Initializable {

  import JfxUtils._

  type ScoreTC[ T ] = TableColumn[ MutableScore, T ]

  @FXML var rootHighScore: BorderPane =_
  @FXML var tableView: TableView[ MutableScore ] = _

  @FXML var columnRang: ScoreTC[ Int ] = _
  @FXML var columnName: ScoreTC[ String ] = _
  @FXML var columnScore: ScoreTC[ Int ] = _

  @FXML var zurueck: Button = _

  // Wenn der Button Zurück gedrückt wird, soll das aktuelle Fenster geschlossen werden und
  // der Startbildschirm wieder angezeigt werden.
  def onZurueck(): Unit = {
    rootHighScore.getScene.getWindow.hide()

    val loader = new FXMLLoader(getClass.getResource("GUI-Startscreen.fxml"))
    val startStage = new Stage()
    startStage.setTitle("PizzaBu - Die Pizza kommt in nu!")
    loader.load[Parent]()
    startStage.setScene(new Scene(loader.getRoot[ Parent ]))

    startStage.show()
  }

  /**
   * provide a table column and a generator function for the value to put into
   * the column.
   *
   * @tparam T the type which is contained in the property
   * @return
   */

  def initTableViewColumn[ T ]: (ScoreTC[ T ], (MutableScore) => Any) => Unit =
    initTableViewColumnCellValueFactory[ MutableScore, T ]

  //läd die Daten von der Datenbank

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    //teilt den einzelnen Spalten einen Wert zu
    initTableViewColumn[String](columnName, _.nameProperty)
    initTableViewColumn[Int](columnScore, _.scoreProperty)



    if (new java.io.File("C:\\PizzaBu\\score.db").exists == true) {

      println("ich komme in die if schleife")
      for {
        con <- Db.maybeConnection
        s <- Score.fromDb(Score.queryAll(con))
      } {
        tableView.getItems().add(MutableScore(s))
      }

      println("Es musste keine Datenbank erstellt werden!")


    }
    else {

      for {
        con <- Db.maybeConnection
        _ = Score.reTable(con.createStatement())
        _ = Score.highscorelist.map(Score.toDb(con)(_))
        s <- Score.fromDb(Score.queryAll(con))
      } {
        tableView.getItems().add(MutableScore(s))
      }

      println("Es wurde eine neue Datenbank erstellt!")

    }

  }
}

case class PizzaBuAppHelpController() {

  @FXML var borderTop: BorderPane = _
  @FXML var zurueck: Button = _

  // Wenn der Button Zurück gedrückt wird, soll das aktuelle Fenster geschlossen werden und
  // der Startbildschirm wieder angezeigt werden.
  def onZurueck(): Unit = {
    borderTop.getScene.getWindow.hide()
  }
}


/**
 * GAME OVER CONTROLLER
 */


case class PizzaBuAppFXController() extends Initializable {

  @FXML var canvasAnchorPane: AnchorPane = _
  @FXML var pommes: Button = _
  @FXML var ofen: Button = _
  @FXML var cola: Button = _

  @FXML var reihe1: Button = _
  @FXML var reihe2: Button = _
  @FXML var reihe3: Button = _
  @FXML var reihe4: Button = _

  @FXML var stop: Button = _


  var animationTimer: CircleAnimation = _

  def randColor(): Color = Color.color(Random.nextDouble, Random.nextDouble(), Random.nextDouble)


  override def initialize(location: URL, resources: ResourceBundle): Unit = {

    val width = canvasAnchorPane.getMinWidth
    val height = canvasAnchorPane.getMinHeight

    val circles: IndexedSeq[ Circle ] =
      for (i <- 1 to 1) yield {
        mkCircle(width.toInt, height.toInt, Random.nextInt(20) + 1)
      }
    // wieso getChildren.addAll?
    canvasAnchorPane.getChildren.addAll(circles: _*)
    animationTimer = CircleAnimation(circles)
  }


  def mkCircle(width: Int, height: Int, maxRadius: Int): Circle = {
    val c = new Circle(Random.nextInt(width), Random.nextInt(height), Random.nextInt(maxRadius))
    c.setFill(randColor())
    c
  }

  // name der def() muss dem onAction Namen entsprechen
  def start(): Unit = {
    animationTimer.start()
  }

  def exit(): Unit = {
    animationTimer.stop()
  }


}


