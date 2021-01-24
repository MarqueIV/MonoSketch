package mono.app

import kotlinx.browser.document
import mono.graphics.board.Highlight
import mono.graphics.board.MonoBoard
import mono.graphics.geo.MousePointer
import mono.graphics.geo.Rect
import mono.graphics.geo.Size
import mono.html.canvas.CanvasViewController
import mono.lifecycle.LifecycleOwner
import org.w3c.dom.HTMLDivElement

/**
 * Main class of the app to handle all kinds of events, UI, actions, etc.
 */
class MonoFlowApplication : LifecycleOwner() {
    private val model: MonoFlowAppModel = MonoFlowAppModel()

    private var canvasViewController: CanvasViewController? = null
    private val monoBoard: MonoBoard = MonoBoard()

    /**
     * The entry point for all actions. This is called after window is loaded (`window.onload`)
     */
    override fun onStartInternal() {
        val boardCanvasContainer =
            document.getElementById(CONTAINER_ID) as? HTMLDivElement ?: return
        canvasViewController =
            CanvasViewController(
                this,
                boardCanvasContainer,
                monoBoard,
                model.windowSizeLiveData
            )
        onResize()

        canvasViewController?.mousePointerLiveData?.observe(this) {
            if (it is MousePointer.Up) {
                // TODO: This is for testing. Remove then
                monoBoard.fill(
                    Rect.byLTRB(
                        it.mouseDownPoint.left,
                        it.mouseDownPoint.top,
                        it.point.left,
                        it.point.top
                    ), '+', Highlight.NO
                )
            }
        }
    }

    fun onResize() {
        val body = document.body ?: return
        val newSize = Size(body.clientWidth, body.clientHeight)
        model.setWindowSize(newSize)
    }

    companion object {
        private const val CONTAINER_ID = "monoboard-canvas-container"
    }
}
