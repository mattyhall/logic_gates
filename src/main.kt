import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.Toggle
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToggleGroup
import javafx.scene.control.ToolBar
import javafx.scene.input.MouseEvent
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.util.*

class App : Application() {
    var gates: ArrayList<Gate> = arrayListOf()
    var state = State.POINTER
    var target: Gate? = null

    fun initCircuit() {
        var input1 = InputGate(50.0, 50.0, "A")
        var input2 = InputGate(50.0, 200.0, "B")
        var notGate = NotGate(200.0, 50.0)
        var andGate = AndGate(400.0, 125.0)
        var output = OutputGate(500.0, 125.0, "Z")
        input1.connections.add(Connection("Z", notGate, "A"))
        notGate.connections.add(Connection("Z", andGate, "A"))
        input2.connections.add(Connection("Z", andGate, "B"))
        andGate.connections.add(Connection("Z", output, "A"))
        input1.inputs["A"] = false
        input2.inputs["A"] = true
        gates.addAll(arrayOf(input1, input2, andGate, notGate, output))
    }

    fun walkCircuit() {
        var queue: LinkedList<Gate> = linkedListOf()
        gates.filter({ it is InputGate }).toCollection(queue)
        while (queue.size != 0) {
            val gate = queue.poll()
            gate.calculateOutput()
            for ((output, toGate, input) in gate.connections) {
                if (!queue.contains(toGate)) {
                    queue.add(toGate)
                }
                toGate.inputs[input] = gate.outputs[output] as Boolean
            }
        }
    }

    override fun start(primaryStage: Stage) {
        initCircuit()
        walkCircuit()
        val root = VBox()
        val scene = Scene(root)
        val group = ToggleGroup()
        val pointer = ToggleButton("Pointer")
        val move = ToggleButton("Move")
        val and = ToggleButton("AND")
        pointer.toggleGroup = group
        move.toggleGroup = group
        and.toggleGroup = group
        val toolbar = ToolBar(pointer, move, and)
        val canvas = Canvas(800.0, 600.0)
        root.children.add(toolbar)
        root.children.add(canvas)
        group.selectedToggleProperty().addListener(object : ChangeListener<Toggle> {
            override fun changed(ov: ObservableValue<out Toggle>?, old: Toggle?, new: Toggle) {
                state = when (new) {
                    and -> State.AND
                    pointer -> State.POINTER
                    move -> State.MOVE
                    else -> State.POINTER
                }
            }
        })
        val timeline = object : AnimationTimer() {
            override fun handle(now: Long) {
                walkCircuit()
                canvas.graphicsContext2D.fill = Color.WHITE
                canvas.graphicsContext2D.fillRect(0.0, 0.0, 800.0, 600.0)
                for (gate in gates) {
                    gate.drawConnections(canvas.graphicsContext2D)
                    gate.draw(canvas.graphicsContext2D)
                }
            }
        }
        canvas.onMouseClicked = object : EventHandler<MouseEvent> {
            override fun handle(event: MouseEvent) {
                when (state) {
                    State.POINTER -> pointerClicked(event)
                    State.MOVE -> moveClicked(event)
                }
            }
        }
        canvas.onMouseDragged = object : EventHandler<MouseEvent> {
            override fun handle(event: MouseEvent) {
                when (state) {
                    State.MOVE -> moveDragged(event)
                }
            }
        }
        timeline.start()
        primaryStage.scene = scene
        primaryStage.show()
    }

    private fun pointerClicked(event: MouseEvent) {
        for (gate in gates) {
            if (gate.collides(event.x, event.y) && gate is InputGate) {
                gate.inputs["A"] = !(gate.inputs["A"] as Boolean)
                break
            }
        }
    }

    private fun moveClicked(event: MouseEvent) {
        target = null
    }

    private fun moveDragged(event: MouseEvent) {
        if (target == null) {
            for (gate in gates) {
                if (gate.collides(event.x, event.y)) {
                    target = gate
                }
            }
        }
        target?.x = event.x
        target?.y = event.y
    }
}

fun main(args: Array<String>) {
    Application.launch(App::class.java, *args)
}