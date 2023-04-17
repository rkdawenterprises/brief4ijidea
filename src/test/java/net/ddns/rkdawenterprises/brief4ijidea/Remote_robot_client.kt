@file:Suppress("PrivatePropertyName",
               "ClassName",
               "HardCodedStringLiteral")

package net.ddns.rkdawenterprises.brief4ijidea

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.StepLogger
import com.intellij.remoterobot.stepsProcessing.StepWorker
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicBoolean
import javax.imageio.ImageIO

class Remote_robot_client : AfterTestExecutionCallback, ParameterResolver
{
    private val url: String = System.getProperty("remote-robot-url") ?: "http://127.0.0.1:22224"

    private var initialized = AtomicBoolean(false)

    private val client = OkHttpClient()

    init
    {
        if(!initialized.get())
        {
            StepWorker.registerProcessor(StepLogger())
            initialized.set(true)
        }
    }

    private val remoteRobot: RemoteRobot = if(System.getProperty("debug-retrofit")
            ?.equals("enable") == true)
    {
        val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .apply {
                this.addInterceptor(interceptor)
            }
            .build()
        RemoteRobot(url,
                    client)
    }
    else
    {
        RemoteRobot(url)
    }

    override fun supportsParameter(parameterContext: ParameterContext?,
                                   extensionContext: ExtensionContext?): Boolean
    {
        return parameterContext?.parameter?.type?.equals(RemoteRobot::class.java) ?: false
    }

    override fun resolveParameter(parameterContext: ParameterContext?,
                                  extensionContext: ExtensionContext?): Any
    {
        return remoteRobot
    }

    override fun afterTestExecution(context: ExtensionContext?)
    {
        val testMethod: Method = context?.requiredTestMethod ?: throw IllegalStateException("test method is null")
        val testMethodName = testMethod.name
        val testFailed: Boolean = context.executionException?.isPresent ?: false
        if(testFailed)
        {
            saveIdeaFrames(testMethodName)
            saveHierarchy(testMethodName)
        }
    }

    private fun saveHierarchy(testName: String)
    {
        val hierarchySnapshot =
            saveFile(url,
                     "build/reports",
                     "hierarchy-$testName.html")
        if(File("build/reports/styles.css").exists()
                .not())
        {
            saveFile("$url/styles.css",
                     "build/reports",
                     "styles.css")
        }
        println("Hierarchy snapshot: ${hierarchySnapshot.absolutePath}")
    }

    private fun saveFile(url: String,
                         folder: String,
                         name: String): File
    {
        val response = client.newCall(Request.Builder()
                                          .url(url)
                                          .build())
            .execute()
        return File(folder).apply {
            mkdirs()
        }
            .resolve(name)
            .apply {
                writeText(response.body?.string() ?: "")
            }
    }

    private fun BufferedImage.save(name: String)
    {
        val bytes = ByteArrayOutputStream().use { b ->
            ImageIO.write(this,
                          "png",
                          b)
            b.toByteArray()
        }
        File("build/reports").apply { mkdirs() }
            .resolve("$name.png")
            .writeBytes(bytes)
    }

    private fun saveIdeaFrames(testName: String)
    {
        remoteRobot.findAll<ContainerFixture>(byXpath("//div[@class='IdeFrameImpl']"))
            .forEachIndexed { n, frame ->
                val pic = try
                {
                    frame.callJs<ByteArray>(
                        """
                        importPackage(java.io)
                        importPackage(javax.imageio)
                        importPackage(java.awt.image)
                        const screenShot = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_ARGB);
                        component.paint(screenShot.getGraphics())
                        let pictureBytes;
                        const baos = new ByteArrayOutputStream();
                        try {
                            ImageIO.write(screenShot, "png", baos);
                            pictureBytes = baos.toByteArray();
                        } finally {
                          baos.close();
                        }
                        pictureBytes;   
            """,
                        true
                                           )
                }
                catch(e: Throwable)
                {
                    e.printStackTrace()
                    throw e
                }
                pic.inputStream()
                    .use {
                        ImageIO.read(it)
                    }
                    .save(testName + "_" + n)
            }
    }
}

