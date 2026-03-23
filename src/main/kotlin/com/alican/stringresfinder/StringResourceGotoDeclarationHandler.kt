package com.alican.stringresfinder

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag

class StringResourceGotoDeclarationHandler : GotoDeclarationHandler {

    override fun getGotoDeclarationTargets(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor?
    ): Array<PsiElement>? {
        if (sourceElement == null) return null

        val xmlAttrValue = PsiTreeUtil.getParentOfType(sourceElement, XmlAttributeValue::class.java)
            ?: return null

        if (!isStringResourceNameAttribute(xmlAttrValue)) return null

        val dotName = xmlAttrValue.value
        if (!dotName.contains('.')) return null

        val underscoreName = dotName.replace('.', '_')
        val project = sourceElement.project

        val results = findAllUsages(project, underscoreName)

        if (results.isEmpty()) {
            // No usages found → fallback to default Android Studio behavior
            return null
        }

        if (results.size == 1) {
            // Single usage → navigate directly
            return results.toTypedArray()
        }

        // Multiple usages → return a single fake element that opens Find in Files on navigate
        val fakeElement = FindInFilesNavigationElement(
            project,
            sourceElement,
            "R.string.$underscoreName"
        )
        return arrayOf(fakeElement)
    }

    private fun isStringResourceNameAttribute(attrValue: XmlAttributeValue): Boolean {
        val attr = attrValue.parent as? XmlAttribute ?: return false
        if (attr.name != "name") return false

        val tag = attr.parent as? XmlTag ?: return false
        if (tag.name != "string") return false

        val file = attrValue.containingFile ?: return false
        val filePath = file.virtualFile?.path ?: return false
        return filePath.contains("/res/") && file.name.startsWith("strings")
    }

    private fun findAllUsages(project: Project, underscoreName: String): List<PsiElement> {
        val results = mutableListOf<PsiElement>()
        val scope = GlobalSearchScope.projectScope(project)
        val psiManager = PsiManager.getInstance(project)

        // Search Kotlin/Java files for R.string.<name>
        for (ext in listOf("kt", "java")) {
            val files = FilenameIndex.getAllFilesByExt(project, ext, scope)
            for (vFile in files) {
                val psiFile = psiManager.findFile(vFile) ?: continue
                val text = psiFile.text
                val rStringRef = "R.string.$underscoreName"
                var searchFrom = 0
                while (true) {
                    val idx = text.indexOf(rStringRef, searchFrom)
                    if (idx == -1) break
                    val endIdx = idx + rStringRef.length
                    if (endIdx < text.length && (text[endIdx].isLetterOrDigit() || text[endIdx] == '_')) {
                        searchFrom = endIdx
                        continue
                    }
                    val leaf = psiFile.findElementAt(idx)
                    if (leaf != null) results.add(leaf)
                    searchFrom = endIdx
                }
            }
        }

        // Search XML layout/menu files for @string/<name>
        val xmlFiles = FilenameIndex.getAllFilesByExt(project, "xml", scope)
        for (vFile in xmlFiles) {
            val path = vFile.path
            if (!path.contains("/res/")) continue
            if (path.contains("/values")) continue

            val psiFile = psiManager.findFile(vFile) ?: continue
            val text = psiFile.text
            val xmlRef = "@string/$underscoreName"
            var searchFrom = 0
            while (true) {
                val idx = text.indexOf(xmlRef, searchFrom)
                if (idx == -1) break
                val endIdx = idx + xmlRef.length
                if (endIdx < text.length && (text[endIdx].isLetterOrDigit() || text[endIdx] == '_')) {
                    searchFrom = endIdx
                    continue
                }
                val leaf = psiFile.findElementAt(idx)
                if (leaf != null) results.add(leaf)
                searchFrom = endIdx
            }
        }

        return results
    }

    override fun getActionText(context: DataContext): String {
        return "Find String Resource Usages (dot→underscore)"
    }
}
