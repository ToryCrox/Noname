package com.tory.dmzj.dbase.net;

import com.tory.library.log.LogUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

public class ProxySSLSocketFactory extends SSLSocketFactory {
    private Proxy proxy;
    private SSLSocketFactory socketFactory;

    public ProxySSLSocketFactory(Proxy proxy, SSLSocketFactory socketFactory) {
        this.proxy = proxy;
        this.socketFactory = socketFactory;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return socketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return socketFactory.getSupportedCipherSuites();
    }

    public Socket createSocket()
            throws IOException {
        LogUtils.d("createSocket...." +proxy);
        if (proxy != null) {
            return new Socket(proxy);
        } else {
            return new Socket();
        }
    }

    public Socket createSocket(String host, int port)
            throws IOException {
        Socket socket = createSocket();
        try {
            return socketFactory.createSocket(socket, host, port, true);
        } catch (IOException e) {
            socket.close();
            throw e;
        }
    }

    public Socket createSocket(Socket s, String host,
                               int port, boolean autoClose)
            throws IOException {
        LogUtils.d("createSocket....11111, host:" +host +", port:" + port);
        //TODO 无法代理
        return socketFactory.createSocket(s, host, port, autoClose);
    }

    public Socket createSocket(InetAddress address, int port)
            throws IOException {
        Socket socket = createSocket();
        try {
            return socketFactory.createSocket(socket, address.getHostAddress(), port, true);
        } catch (IOException e) {
            socket.close();
            throw e;
        }
    }

    public Socket createSocket(String host, int port,
                               InetAddress clientAddress, int clientPort)
            throws IOException {
        Socket socket = createSocket();
        try {
            socket.bind(new InetSocketAddress(clientAddress, clientPort));
            return socketFactory.createSocket(socket, host, port, true);
        } catch (IOException e) {
            socket.close();
            throw e;
        }
    }

    public Socket createSocket(InetAddress address, int port,
                               InetAddress clientAddress, int clientPort)
            throws IOException {
        Socket socket = createSocket();
        try {
            socket.bind(new InetSocketAddress(clientAddress, clientPort));
            return socketFactory.createSocket(socket, address.getHostAddress(), port, true);
        } catch (IOException e) {
            socket.close();
            throw e;
        }
    }
}
