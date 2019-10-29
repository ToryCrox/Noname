package com.tory.library.net

import android.content.Context
import com.tory.library.utils.IoUtils
import com.tory.library.utils.Md5Util
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.*
import java.util.concurrent.TimeUnit

/**
 * @author tory
 * @create 2019/10/29
 * @Describe
 */

public object NetHelper{
    const val DEFAULT_MILLISECONDS = 5000L
    val okHttpClient: OkHttpClient by lazy{

        val builder = OkHttpClient().newBuilder()
                .connectTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS)
                .writeTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS)
                .readTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS)
                .addInterceptor(HttpLoggingInterceptor())
        builder.build()
    }


    fun download(context: Context, url: String, onFinished: (filePath: String?) -> Unit): ()-> Unit {
        val request = Request.Builder()
                .url(url).build()
        val call = okHttpClient.newCall(request)
        call.enqueue(object : Callback{
                    override fun onFailure(call: Call, e: IOException) {
                        onFinished(null)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val targetFile = File(context.cacheDir, Md5Util.digest(url))
                        if (targetFile.exists()) targetFile.delete()

                        var input: InputStream?= null
                        var output: OutputStream? = null

                        try{
                            input = response.body()?.byteStream()
                            if (input == null){
                                onFinished(null)
                            }
                            output = FileOutputStream(targetFile)
                            val buf = ByteArray(1024)
                            var len = -1
                            while ((input?.read(buf)?.also { len = it }) != -1) {
                                output.write(buf, 0, len)
                            }
                            onFinished(targetFile.absolutePath)
                        } catch (e: Exception){

                        } finally {
                            IoUtils.closeSilently(input)
                            IoUtils.closeSilently(output)
                        }
                    }
                })
        return { call.cancel() }
    }
}