package ru.jr2.edit.presentation.view

import ru.jr2.edit.presentation.view.group.GroupListView
import ru.jr2.edit.presentation.view.moji.MojiListView
import ru.jr2.edit.presentation.view.sentence.SentenceListView
import ru.jr2.edit.presentation.view.word.WordListView
import tornadofx.View
import tornadofx.tab
import tornadofx.tabpane

class RootView : View("JR2-Edit") {
    private val mojiListView: MojiListView by inject()
    private val wordListView: WordListView by inject()
    private val sentenceListView: SentenceListView by inject()
    private val groupListView: GroupListView by inject()

    override val root = tabpane {
        tab("Кана") {
            isClosable = false
        }
        tab("Моджи") {
            isClosable = false
            add(mojiListView)
        }
        tab("Слова") {
            isClosable = false
            add(wordListView)
        }
        tab("Предложения") {
            isClosable = false
            add(sentenceListView)
        }
        tab("Группы") {
            isClosable = false
            add(groupListView)
        }
    }
}