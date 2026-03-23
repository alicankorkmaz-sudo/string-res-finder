package com.alican.stringresfinder

import com.intellij.find.FindModel
import com.intellij.find.findInProject.FindInProjectManager
import com.intellij.ide.DataManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement

/**
 * A fake PSI element that, when navigated to (on actual Cmd+Click),
 * opens the "Find in Files" search overlay instead of jumping to a file.
 */
class FindInFilesNavigationElement(
    private val project: Project,
    private val parentElement: PsiElement,
    private val searchTerm: String
) : FakePsiElement() {

    override fun getParent(): PsiElement = parentElement

    override fun getName(): String = searchTerm

    override fun navigate(requestFocus: Boolean) {
        val findModel = FindModel()
        findModel.stringToFind = searchTerm
        findModel.isRegularExpressions = false
        findModel.isCaseSensitive = true
        findModel.isProjectScope = true

        val frame = com.intellij.openapi.wm.WindowManager.getInstance().getFrame(project)
        if (frame != null) {
            val dataContext = DataManager.getInstance().getDataContext(frame)
            FindInProjectManager.getInstance(project).findInProject(dataContext, findModel)
        }
    }

    override fun canNavigate(): Boolean = true
    override fun canNavigateToSource(): Boolean = true
}
