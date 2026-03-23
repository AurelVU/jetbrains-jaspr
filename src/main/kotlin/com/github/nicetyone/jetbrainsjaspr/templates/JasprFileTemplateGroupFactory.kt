package com.github.nicetyone.jetbrainsjaspr.templates

import com.github.nicetyone.jetbrainsjaspr.JasprBundle
import com.github.nicetyone.jetbrainsjaspr.JasprIcons
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory

class JasprFileTemplateGroupFactory : FileTemplateGroupDescriptorFactory {

    override fun getFileTemplatesDescriptor(): FileTemplateGroupDescriptor {
        val group = FileTemplateGroupDescriptor(
            JasprBundle.message("jaspr.file.template.group"),
            JasprIcons.JASPR
        )
        group.addTemplate(STATELESS_COMPONENT)
        group.addTemplate(STATEFUL_COMPONENT)
        group.addTemplate(CLIENT_COMPONENT)
        group.addTemplate(PAGE)
        return group
    }

    companion object {
        const val STATELESS_COMPONENT = "Jaspr StatelessComponent"
        const val STATEFUL_COMPONENT = "Jaspr StatefulComponent"
        const val CLIENT_COMPONENT = "Jaspr ClientComponent"
        const val PAGE = "Jaspr Page"
    }
}
