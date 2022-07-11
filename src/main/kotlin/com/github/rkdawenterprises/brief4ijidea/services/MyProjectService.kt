package com.github.rkdawenterprises.brief4ijidea.services

import com.intellij.openapi.project.Project
import com.github.rkdawenterprises.brief4ijidea.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
