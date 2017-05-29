/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2017 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.error

import com.demonwav.mcdev.asset.MCMessages
import com.intellij.diagnostic.IdeErrorsDialog
import com.intellij.diagnostic.LogMessageEx
import com.intellij.diagnostic.ReportMessages
import com.intellij.errorreport.bean.ErrorBean
import com.intellij.ide.DataManager
import com.intellij.ide.plugins.PluginManager
import com.intellij.idea.IdeaLogger
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.ex.ApplicationInfoEx
import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.util.Consumer
import java.awt.Component

class ErrorReporter : ErrorReportSubmitter() {
    val baseUrl = "https://github.com/minecraft-dev/MinecraftDev/issues"
    override fun getReportActionText() = MCMessages["reporter.action_text"]

    override fun submit(events: Array<out IdeaLoggingEvent>,
                        additionalInfo: String?,
                        parentComponent: Component,
                        consumer: Consumer<SubmittedReportInfo>): Boolean {

        val event = events[0]
        val bean = ErrorBean(event.throwable, IdeaLogger.ourLastActionId)
        val dataContext = DataManager.getInstance().getDataContext(parentComponent)

        bean.description = additionalInfo
        bean.message = event.message

        val throwable = event.throwable
        if (throwable != null) {
            val pluginId = IdeErrorsDialog.findPluginId(throwable)
            if (pluginId != null) {
                val ideaPluginDescriptor = PluginManager.getPlugin(pluginId)
                if (ideaPluginDescriptor != null && !ideaPluginDescriptor.isBundled) {
                    bean.pluginName = ideaPluginDescriptor.name
                    bean.pluginVersion = ideaPluginDescriptor.version
                }
            }
        }

        val data = event.data

        if (data is LogMessageEx) {
            bean.attachments = data.includedAttachments
        }

        val reportValues = IdeaITNProxy.getKeyValuePairs(bean, ApplicationInfoEx.getInstanceEx(), ApplicationNamesInfo.getInstance())

        val project = CommonDataKeys.PROJECT.getData(dataContext)

        val task = AnonymousFeedbackTask(project, MCMessages["reporter.status"], true, reportValues, { token ->
            val url = "$baseUrl/$token"
            val reportInfo = SubmittedReportInfo(url, "Issue #$token", SubmittedReportInfo.SubmissionStatus.NEW_ISSUE)
            consumer.consume(reportInfo)

            val message = MCMessages["reporter.message.success", token, url]

            ReportMessages.GROUP.createNotification(
                ReportMessages.ERROR_REPORT,
                message,
                NotificationType.INFORMATION,
                NotificationListener.URL_OPENING_LISTENER
            ).setImportant(false).notify(project)
        }, { e ->
            val message = MCMessages["reporter.message.error", e.message ?: MCMessages["reporter.unknown_error"]]
            ReportMessages.GROUP.createNotification(
                ReportMessages.ERROR_REPORT,
                message,
                NotificationType.ERROR,
                NotificationListener.URL_OPENING_LISTENER
            ).setImportant(false).notify(project)
        })

        if (project == null) {
            task.run(EmptyProgressIndicator())
        } else {
            ProgressManager.getInstance().run(task)
        }
        return true
    }
}
