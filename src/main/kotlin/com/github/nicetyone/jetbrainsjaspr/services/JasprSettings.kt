package com.github.nicetyone.jetbrainsjaspr.services

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(
    name = "JasprSettings",
    storages = [Storage("jaspr.xml")]
)
class JasprSettings : PersistentStateComponent<JasprSettings.State> {

    class State : BaseState() {
        var jasprCliPath by string("")
    }

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    var jasprCliPath: String
        get() = myState.jasprCliPath ?: ""
        set(value) {
            myState.jasprCliPath = value
        }

    companion object {
        fun getInstance(project: Project): JasprSettings =
            project.getService(JasprSettings::class.java)
    }
}
