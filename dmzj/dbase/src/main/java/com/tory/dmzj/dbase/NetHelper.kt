package com.tory.dmzj.dbase

import android.annotation.SuppressLint
import com.tory.dmzj.dbase.net.ProxySSLSocketFactory
import com.tory.library.log.LogUtils
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/8/30
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/8/30 xutao 1.0
 * Why & What is modified:
 */
object NetHelper {
    private const val DEFAULT_CONNECT_TIMEOUT = 5L
    private const val DEFAULT_READ_TIMEOUT = 10L
    private const val DEFAULT_WRITE_TIMEOUT = 20L
    private const val BASE_URL = "http://v3api.dmzj.com/"
    private const val BASE_COMMENT_URL = "http://v3comment.dmzj.com"
    private const val BASE_USER_URL = "https://user.dmzj.com/"
    private val API_IMAGE_BASE = "http://images.dmzj.com/"
    private val API_IMAGE_BASE_HTTPS = "https://images.dmzj.com/"
    private val API_IMAGE_BASE_AVATAR = "https://avatar.dmzj.com/"
    val COMMENT_IMAGE_BASE_URL = "http://images.dmzj.com/commentImg/"
    private val DMZJ_IMAGES = arrayOf(API_IMAGE_BASE,
        API_IMAGE_BASE_HTTPS, API_IMAGE_BASE_AVATAR)
    private val UID = 100013896
    private val BASE_URL_KONACHAN = "https://konachan.com/"
    private val BASE_URL_YANDE = "https://yande.re/"
    private val PROXY_URLS = arrayOf(BASE_URL_KONACHAN,
        BASE_URL_YANDE, "yande.re")

    var httpProxy: Proxy = Proxy(Proxy.Type.HTTP,
        InetSocketAddress("127.0.0.1", 10808))
    var socketProxy: Proxy = Proxy(Proxy.Type.HTTP,
        InetSocketAddress("127.0.0.1", 10809))

    val okHttpClient: OkHttpClient by lazy {
        val logInterceptor = HttpLoggingInterceptor()
        logInterceptor.level = HttpLoggingInterceptor.Level.BODY
        OkHttpClient.Builder()
            .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(DMZJInterceptor())
            .addNetworkInterceptor(logInterceptor)
            //.sslSocketFactory(ProxySSLSocketFactory(socketProxy, createSSLSocketFactory()), TrustAllManager())
            .sslSocketFactory(createSSLSocketFactory(), TrustAllManager())
            .hostnameVerifier(TrustAllHostnameVerifier())
            .proxySelector(MProxyProxySelector())
            .build()
    }
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val commentRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_COMMENT_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val userRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_USER_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val konachanRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_KONACHAN)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val yandeRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_YANDE)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }



    private class DMZJInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val headers: Headers = request.headers()
            val builder = request.newBuilder()
            val origUrl = request.url().toString()

            LogUtils.w("DMZJInterceptor intercept origUrl:$origUrl")
            for (dmzjImage in DMZJ_IMAGES) {
                if (!origUrl.isNullOrEmpty() && origUrl.startsWith(dmzjImage)) {
                    builder.header("Referer", API_IMAGE_BASE)
                }
            }
            val isDmzj = headers.get("dmzj")
            if (isDmzj?.toLowerCase()?.trim() == "true") {
                val dmzjBuilder = request.url().newBuilder()
                    .addQueryParameter("terminal_model", "MI 9")
                    .addQueryParameter("channel", "Android")
                    .addQueryParameter("_debug", "0")
                    .addQueryParameter("version", "2.7.031")
                    .addQueryParameter("timestamp", (System.currentTimeMillis() / 1000).toString())
                if (headers.get("user")?.toLowerCase()?.trim() == "true") {
                    dmzjBuilder.addQueryParameter("uid", UID.toString())
                }
                builder.url(dmzjBuilder.build())
                builder.removeHeader("dmzj")
                builder.removeHeader("user")
            }

            return chain.proceed(builder.build())
        }
    }

    private class MProxyProxySelector : ProxySelector() {
        var defaultSelector: ProxySelector = ProxySelector.getDefault()

        override fun select(uri: URI?): MutableList<Proxy> {
            val url = uri?.toString().orEmpty()
            val isProxy = PROXY_URLS.any { url.contains(it) }
            LogUtils.w("MProxyProxySelector select url:$url, isProxy:$isProxy")
            if (isProxy) {
                val list = mutableListOf<Proxy>()
                list.add(httpProxy)
                list.add(socketProxy)
                list.addAll(defaultSelector.select(uri))
                return mutableListOf(socketProxy)
            }
            return defaultSelector.select(uri)
        }

        override fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?) {
            defaultSelector.connectFailed(uri, sa, ioe)
        }
    }

    /**
     * 默认信任所有的证书
     *
     * @return
     */
    @SuppressLint("TrulyRandom")
    private fun createSSLSocketFactory(): SSLSocketFactory? {
        var sSLSocketFactory: SSLSocketFactory? = null
        try {
            val sc: SSLContext = SSLContext.getInstance("TLS")
            sc.init(null, arrayOf<TrustManager>(TrustAllManager()),
                SecureRandom())
            sSLSocketFactory = sc.getSocketFactory()
        } catch (e: Exception) {
        }
        return sSLSocketFactory
    }

    private class TrustAllManager : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        }

        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate>? {
            return arrayOf();
        }
    }

    private class TrustAllHostnameVerifier : HostnameVerifier {
        override fun verify(hostname: String?, session: SSLSession?): Boolean {
            return true
        }
    }
}
