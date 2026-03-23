package com.github.nicetyone.jetbrainsjaspr.wizard

import com.github.nicetyone.jetbrainsjaspr.JasprBundle
import com.github.nicetyone.jetbrainsjaspr.runconfig.JasprConfigurationType
import com.github.nicetyone.jetbrainsjaspr.runconfig.JasprRunConfiguration
import com.github.nicetyone.jetbrainsjaspr.services.JasprSettings
import com.intellij.execution.RunManager
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.ide.wizard.AbstractNewProjectWizardStep
import com.intellij.ide.wizard.NewProjectWizardBaseData
import com.intellij.ide.wizard.NewProjectWizardStep
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.jetbrains.lang.dart.sdk.DartSdkLibUtil
import com.jetbrains.lang.dart.sdk.DartSdkUtil
import com.intellij.ui.dsl.builder.COLUMNS_MEDIUM
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import java.io.File

class JasprNewProjectWizardStep(parent: NewProjectWizardStep) : AbstractNewProjectWizardStep(parent) {

    val jasprPathProperty = propertyGraph.property("")
    val modeProperty = propertyGraph.property("server")
    val routingProperty = propertyGraph.property("multi-page")
    val flutterProperty = propertyGraph.property("none")
    val backendProperty = propertyGraph.property("none")

    var jasprPath by jasprPathProperty
    var mode by modeProperty
    var routing by routingProperty
    var flutter by flutterProperty
    var backend by backendProperty

    override fun setupUI(builder: Panel) {
        with(builder) {
            row(JasprBundle.message("jaspr.new.project.jaspr.path")) {
                val descriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
                    .withTitle(JasprBundle.message("jaspr.new.project.jaspr.path"))
                textFieldWithBrowseButton(descriptor, null)
                    .columns(COLUMNS_MEDIUM)
                    .bindText(jasprPathProperty)
            }.rowComment(JasprBundle.message("jaspr.new.project.jaspr.path.comment"))

            row(JasprBundle.message("jaspr.new.project.mode")) {
                comboBox(MODES)
                    .bindItem({ mode }, { mode = it ?: "server" })
            }.rowComment(JasprBundle.message("jaspr.new.project.mode.comment"))

            row(JasprBundle.message("jaspr.new.project.routing")) {
                comboBox(ROUTING_OPTIONS)
                    .bindItem({ routing }, { routing = it ?: "multi-page" })
            }

            row(JasprBundle.message("jaspr.new.project.flutter")) {
                comboBox(FLUTTER_OPTIONS)
                    .bindItem({ flutter }, { flutter = it ?: "none" })
            }

            row(JasprBundle.message("jaspr.new.project.backend")) {
                comboBox(BACKEND_OPTIONS)
                    .bindItem({ backend }, { backend = it ?: "none" })
            }.rowComment(JasprBundle.message("jaspr.new.project.backend.comment"))
        }
    }

    override fun setupProject(project: Project) {
        val baseData = data.getUserData(NewProjectWizardBaseData.KEY) ?: return
        val projectName = baseData.name
        val projectPath = baseData.contentEntryPath

        if (jasprPath.isNotEmpty()) {
            JasprSettings.getInstance(project).jasprCliPath = jasprPath
        }

        val resolvedJasprPath = jasprPath.ifEmpty { "jaspr" }
        val capturedMode = mode
        val capturedRouting = routing
        val capturedFlutter = flutter
        val capturedBackend = backend

        StartupManager.getInstance(project).runAfterOpened {
            ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Creating Jaspr project...") {
                override fun run(indicator: ProgressIndicator) {
                    indicator.text = "Running jaspr create..."

                    // Create in a temp directory to avoid "already exists" error,
                    // then copy generated files into the real project directory
                    val tempDir = createTempDir("jaspr_create_")

                    try {
                        val args = mutableListOf(
                            "create",
                            "-m", capturedMode,
                            "-r", capturedRouting,
                            "-f", capturedFlutter,
                            "-b", capturedBackend,
                            projectName
                        )

                        val commandLine = GeneralCommandLine(resolvedJasprPath)
                        commandLine.withParameters(args)
                        commandLine.withWorkDirectory(tempDir.absolutePath)
                        commandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)

                        val handler = CapturingProcessHandler(commandLine)
                        val result = handler.runProcess(120_000)

                        if (result.exitCode != 0) {
                            val error = result.stderr.ifEmpty { result.stdout }
                            LOG.warn("jaspr create failed (exit ${result.exitCode}): $error")
                            notifyError(project, error)
                            return
                        }

                        // Copy generated files into the real project directory
                        indicator.text = "Setting up project files..."
                        val generatedDir = File(tempDir, projectName)
                        val projectDir = File(projectPath)
                        projectDir.mkdirs()

                        generatedDir.listFiles()?.forEach { file ->
                            file.copyRecursively(File(projectDir, file.name), overwrite = true)
                        }
                    } finally {
                        tempDir.deleteRecursively()
                    }

                    indicator.text = "Refreshing project..."
                    val vfsDir = LocalFileSystem.getInstance().refreshAndFindFileByPath(projectPath)
                    vfsDir?.refresh(false, true)

                    ApplicationManager.getApplication().invokeLater {
                        // Configure Dart SDK
                        configureDartSdk(project)

                        // Create run configurations
                        createRunConfiguration(project)

                        // Open app.dart in editor
                        val appDart = LocalFileSystem.getInstance()
                            .refreshAndFindFileByPath("$projectPath/lib/app.dart")
                        if (appDart != null) {
                            FileEditorManager.getInstance(project).openFile(appDart, true)
                        }
                    }
                }
            })
        }
    }

    private fun configureDartSdk(project: Project) {
        val sdkPath = DartSdkUtil.getFirstKnownDartSdkPath() ?: return
        val projectPath = project.basePath ?: return

        WriteAction.run<Throwable> {
            // Create module if none exists (GeneratorNewProjectWizard doesn't create one)
            val moduleManager = ModuleManager.getInstance(project)
            if (moduleManager.modules.isEmpty()) {
                val imlPath = "$projectPath/${project.name}.iml"
                val modifiableModel = moduleManager.getModifiableModel()
                val module = modifiableModel.newModule(imlPath, "WEB_MODULE")

                // Set content root
                val rootModel = com.intellij.openapi.roots.ModuleRootManager.getInstance(module).modifiableModel
                val contentEntry = rootModel.addContentEntry(
                    LocalFileSystem.getInstance().refreshAndFindFileByPath(projectPath)!!
                )
                contentEntry.addExcludeFolder("${contentEntry.url}/build")
                contentEntry.addExcludeFolder("${contentEntry.url}/.dart_tool")
                rootModel.commit()
                modifiableModel.commit()
            }

            // Configure Dart SDK and enable for all modules
            DartSdkLibUtil.ensureDartSdkConfigured(project, sdkPath)
            for (module in moduleManager.modules) {
                if (!DartSdkLibUtil.isDartSdkEnabled(module)) {
                    DartSdkLibUtil.enableDartSdk(module)
                }
            }
        }
    }

    private fun createRunConfiguration(project: Project) {
        val runManager = RunManager.getInstance(project)
        val configType = JasprConfigurationType.getInstance()
        val factory = configType.configurationFactories.first()

        // "jaspr serve"
        val serveSettings = runManager.createConfiguration("jaspr serve", factory)
        (serveSettings.configuration as JasprRunConfiguration).apply {
            command = "serve"
        }
        runManager.addConfiguration(serveSettings)

        // "jaspr build"
        val buildSettings = runManager.createConfiguration("jaspr build", factory)
        (buildSettings.configuration as JasprRunConfiguration).apply {
            command = "build"
        }
        runManager.addConfiguration(buildSettings)

        // Select "jaspr serve" as the active configuration
        runManager.selectedConfiguration = serveSettings
    }

    private fun notifyError(project: Project, details: String) {
        ApplicationManager.getApplication().invokeLater {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Jaspr")
                .createNotification(
                    "Failed to create Jaspr project",
                    details.take(500),
                    NotificationType.ERROR
                )
                .notify(project)
        }
    }

    companion object {
        private val LOG = logger<JasprNewProjectWizardStep>()

        private val MODES = listOf("static", "server", "client")
        private val ROUTING_OPTIONS = listOf("multi-page", "single-page", "none")
        private val FLUTTER_OPTIONS = listOf("none", "embedded", "plugins-only")
        private val BACKEND_OPTIONS = listOf("none", "shelf")
    }
}
