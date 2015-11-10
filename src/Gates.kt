import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.shape.ArcType
import java.util.*

const val GATE_WIDTH = 30.0
const val GATE_HEIGHT = 30.0
const val CONNECTION_WIDTH = GATE_WIDTH/8.0

data class Connection(val output: String, val gate: Gate, val input: String)

abstract class Gate(val x: Double, val y: Double) {
    abstract var inputs: HashMap<String, Boolean>
    abstract var outputs: HashMap<String, Boolean>
    abstract var inputPositions: HashMap<String, Pair<Double, Double>>
    public var connections: ArrayList<Connection> = arrayListOf()

    abstract public fun draw(gc: GraphicsContext)
    abstract public fun calculateOutput()

    public fun drawConnections(gc: GraphicsContext) {
        for ((output, gate, input) in connections) {
            gc.stroke = if (outputs[output] as Boolean) { Color.GREEN } else { Color.RED }
            val (x1, y1) = gate.inputPositions[input] as Pair<Double, Double>
            gc.strokeLine(x + GATE_WIDTH, y + GATE_HEIGHT/2, x1, y1)
        }
    }

    public fun collides(x1: Double, y1: Double): Boolean {
        return x1 >= x && x1 <= x + GATE_WIDTH && y1 >= y && y1 <= y + GATE_HEIGHT
    }
}

class InputGate(x: Double, y: Double, public var name: String) : Gate(x, y) {
    override var inputs = hashMapOf("A" to false)
    override var inputPositions: HashMap<String, Pair<Double, Double>> = hashMapOf()
    override var outputs = hashMapOf("Z" to false)

    override fun draw(gc: GraphicsContext) {
        val output = outputs["Z"] as Boolean
        gc.stroke = if (output) { Color.GREEN } else { Color.RED }
        gc.strokeRect(x, y, GATE_WIDTH, GATE_HEIGHT)
        val text = if (output) { "1" } else { "0" }
        gc.fill = Color.BLACK
        gc.fillText(text, x + 3*GATE_WIDTH/8, y  + 3*GATE_HEIGHT/5)
    }

    override fun calculateOutput() {
        outputs["Z"] = inputs["A"] as Boolean
    }
}

class AndGate(x: Double, y: Double) : Gate(x, y) {
    override var inputs = hashMapOf("A" to false, "B" to false)
    override var inputPositions = hashMapOf("A" to (x to y + GATE_HEIGHT/4),
                                            "B" to (x to y + 3 * GATE_HEIGHT/4))
    override var outputs = hashMapOf("Z" to false)

    constructor(x: Double, y: Double, nInputs: Int) : this(x, y) {
        var letters = "ABCDEFGHIJKLM".split("")
        var n = nInputs - 1
        for (i in 0..n) {
            inputs[letters[i]] = true
        }
    }

    override fun draw(gc: GraphicsContext) {
        gc.stroke = Color.BLACK
        val sideLength = GATE_WIDTH / 3.0
        gc.strokeLine(x, y, x, y + GATE_HEIGHT)
        gc.strokeLine(x, y, x + sideLength + 5, y)
        gc.strokeLine(x, y + GATE_HEIGHT, x + sideLength + 5, y + GATE_HEIGHT)
        gc.strokeArc(x, y, GATE_WIDTH, GATE_HEIGHT, 90.0, -180.0, ArcType.OPEN)
    }

    override fun calculateOutput() {
        if (inputs.size != 2) {
            throw Exception("There must be two inputs only to an and gate")
        }
        outputs["Z"] = inputs["A"] as Boolean && inputs["B"] as Boolean
    }
}

class OrGate(x: Double, y: Double) : Gate(x, y) {
    override var inputs = hashMapOf("A" to false, "B" to false)
    override var inputPositions = hashMapOf("A" to (x to y + GATE_HEIGHT/4),
                                            "B" to (x to y + 3 * GATE_HEIGHT))
    override var outputs = hashMapOf("Z" to false)

    constructor(x: Double, y: Double, nInputs: Int) : this(x,y) {
        var letters = "ABCDEFGHIJKLMNOPQRSTUV".split("")
        var n = nInputs - 1
        for (i in 0..n) {
            inputs[letters[i]] = false
        }
    }

    override fun draw(gc: GraphicsContext) {}
    override fun calculateOutput() {
        if (inputs.size != 2) {
            throw Exception("There must be two inputs only to an or gate")
        }
        outputs["Z"] = inputs["A"] as Boolean || inputs["B"] as Boolean
    }
}

class NotGate(x: Double, y: Double) : Gate(x, y) {
    override var inputs = hashMapOf("A" to false)
    override var inputPositions = hashMapOf("A" to (x to y + GATE_HEIGHT/2))
    override var outputs = hashMapOf("Z" to false)

    override fun draw(gc: GraphicsContext) {
        val length = GATE_WIDTH/5.0
        val sideLength = 4 * length
        gc.stroke = Color.BLACK
        gc.strokeLine(x, y, x + sideLength, y + GATE_HEIGHT/2.0)
        gc.strokeLine(x + sideLength, y + GATE_HEIGHT/2.0, x, y + GATE_HEIGHT)
        gc.strokeLine(x, y, x, y + GATE_HEIGHT)
        gc.strokeOval(x + sideLength, y + GATE_HEIGHT/2.0 - length/2.0, length, length)
    }

    override fun calculateOutput() {
        if (inputs.size != 1) {
            throw Exception("There must be one input only to not gate")
        }
        outputs["Z"] = !(inputs["A"] as Boolean)
    }
}

class OutputGate(x: Double, y: Double, name: String) : Gate(x, y) {
    override var inputs = hashMapOf("A" to false)
    override var inputPositions = hashMapOf("A" to (x to y + GATE_HEIGHT/2))
    override var outputs = hashMapOf("Z" to false)

    override fun draw(gc: GraphicsContext) {
        val output = outputs["Z"] as Boolean
        gc.stroke = if (output) { Color.GREEN } else { Color.RED }
        gc.strokeRect(x, y, GATE_WIDTH, GATE_HEIGHT)
    }

    override fun calculateOutput() {
        if (inputs.size != 1) {
            throw Exception("There must be one input only to outputs")
        }
        outputs["Z"] = inputs["A"] as Boolean
    }
}