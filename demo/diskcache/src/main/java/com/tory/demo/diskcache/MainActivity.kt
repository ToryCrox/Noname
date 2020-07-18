package com.tory.demo.diskcache

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/7/10
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/7/10 xutao 1.0
 * Why & What is modified:
 */
class MainActivity : AppCompatActivity() {

    val handlerThread = HandlerThread("file-thread")
    lateinit var tHandler: Handler

    var fileOperator: IFileOperator = NioFileOperator()

    var TIMES = 10

    val parentDir: File by lazy {
        externalCacheDir
    }

    var dataText = DATA_STR_1


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        handlerThread.start()
        tHandler = Handler(handlerThread.looper)
        TimeRecorder.setPrinter {
            runOnUiThread {
                textView.append(it)
            }
        }

        switchOperator.setOnClickListener {
            fileOperator = if (fileOperator is NioFileOperator) {
                ChannelFileOperator()
            } else if (fileOperator is ChannelFileOperator) IoFileOperator()
            else NioFileOperator()
            switchOperator.text = "当前状态: ${getOperatorTag()}"
        }

        switchData.text = "当前数据: ${if (dataText == DATA_STR) "DATA_STR" else "DATA_STR_1"}"
        switchData.setOnClickListener {
            dataText = if (dataText == DATA_STR) DATA_STR_1 else DATA_STR
            switchData.text = "当前数据: ${if (dataText == DATA_STR) "DATA_STR" else "DATA_STR_1"}"
        }

        writeBtn.setOnClickListener {
            tHandler.post {
                writeAll()
            }
        }

        readBtn.setOnClickListener {
            tHandler.post {
                readAll()
            }
        }

        deleteBtn.setOnClickListener {
            tHandler.post {
                removeAll()
            }
        }

        writeCache.setOnClickListener {
            tHandler.post {
                writeCacheAll()
            }
        }
        readCache.setOnClickListener {
            tHandler.post {
                getCacheAll()
            }
        }

    }

    private fun writeCacheAll() {
        DiskCache.getInstance().write("cache_1", DATA_STR)
    }

    private fun getCacheAll() {
        val result = DiskCache.getInstance().read("cache_1", String::class.java)
        runOnUiThread {
            Log.d("Activity", "mmm getCacheAll $result")
        }
    }

    fun getOperatorTag() = if (fileOperator is IoFileOperator) "IO"
    else if (fileOperator is NioFileOperator) "NIO"
    else "Channel"

    fun writeAll() {
        val dataList = mutableListOf<String>()
        repeat(100){
            dataList.add(DATA_STR)
        }

        val data = dataList.joinToString()
        //val data = dataText
        TimeRecorder.begin("write")
        for (index in 0 until TIMES) {
            val file = getFile(index)
            fileOperator.write(file, data)
        }
        TimeRecorder.end("write", "${getOperatorTag()} write ${TIMES} times file")
        runOnUiThread {
            textView.append("file_size:%.2fKB\n".format(getFile(0).length() / 1024f))
        }
    }

    fun readAll() {
        TimeRecorder.begin("read")
        var result = ""
        for (index in 0 until TIMES) {
            val file = getFile(index)
            if (index == 0) {
                result = fileOperator.read(file)
            }
        }
        TimeRecorder.end("read", "${getOperatorTag()} read ${TIMES} times file")
        runOnUiThread {
            textView.append("file_size:%.2fKB\n".format(getFile(0).length() / 1024f))
        }
        Log.d("MainActivity", "mmm read data: $result")
    }

    fun removeAll() {
        TimeRecorder.begin("remove")
        var successCount = 0
        for (index in 0 until TIMES) {
            val file = getFile(index)
            if (file.delete()) {
                successCount++
            }
        }
        TimeRecorder.end("remove", "remove ${TIMES} times file successCount:$successCount")
    }

    fun getFile(index: Int): File {
        return File(parentDir, "file_%04d.txt".format(index))
    }


    override fun onDestroy() {
        TimeRecorder.setPrinter(null)
        handlerThread.quitSafely()
        super.onDestroy()
    }
}
