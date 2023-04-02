/*
 * Copyright (c) 2023, tuanchauict
 */

package mono.html.modal

import kotlinx.browser.document
import mono.common.Cancelable
import mono.common.Key
import mono.common.onKeyDown
import mono.common.setTimeout
import mono.html.Div
import mono.html.Input
import mono.html.InputType
import mono.html.Li
import mono.html.Ul
import mono.html.setOnClickListener
import mono.html.setOnFocusOut
import org.w3c.dom.Element

/**
 * A modal for dropdown menu
 */
class DropDownMenu(id: String, items: List<Item>, private val onClickAction: (Item) -> Unit) {
    private val menu: Element?

    private var dismissTimeout: Cancelable? = null

    init {
        println(items.filterIsInstance<Item.Text>().map { it.title })
        menu = document.body?.Div(classes = "drop-down-menu") {
            this.id = id

            initItems(items)

            val hiddenInput = Input(inputType = InputType.CHECK_BOX, classes = "hidden-input") {
                setOnFocusOut { dismiss() }
                focus()

                onKeyDown {
                    if (it.which == Key.KEY_ESC) {
                        dismiss()
                    }
                }
            }

            onmousedown = {
                setTimeout(5) {
                    dismissTimeout?.cancel()
                    hiddenInput.focus()
                }
            }
        }
    }

    private fun Element.initItems(items: List<Item>) {
        Ul {
            for (item in items.filter { it.isVisible() }) {
                when (item) {
                    is Item.Divider -> Li(classes = "drop-down-divider")
                    is Item.Text -> Li(classes = "drop-down-item") {
                        innerText = item.title
                        setOnClickListener {
                            onClickAction(item)
                            dismiss()
                        }
                    }
                }
            }
        }
    }

    private fun dismiss() {
        dismissTimeout = setTimeout(20) {
            menu?.remove()
        }
    }

    sealed class Item(internal val isVisible: () -> Boolean) {
        class Divider(isVisible: () -> Boolean = { true }) : Item(isVisible)
        class Text(val title: String, val key: Any, isVisible: () -> Boolean = { true }) :
            Item(isVisible)
    }
}
