package com.github.stuxuhai.hdata.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.regex.Pattern;

public class FTPUtils {

//	private static final Logger LOGGER = LogManager.getLogger(FTPUtils.class);

    public static FTPClient getFtpClient(String host, int port, String username, String password)
            throws SocketException, IOException {
        String LOCAL_CHARSET = "GB18030";
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(host, port);
        // 检测服务器是否支持UTF-8编码，如果支持就用UTF-8编码，否则就使用本地编码GB18030
        if (FTPReply.isPositiveCompletion(ftpClient.sendCommand("OPTS UTF8", "ON"))) {
            LOCAL_CHARSET = "UTF-8";
        }
        ftpClient.setControlEncoding(LOCAL_CHARSET);
        ftpClient.login(username, password);
        ftpClient.setBufferSize(1024 * 1024);
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.setControlKeepAliveTimeout(60);
        return ftpClient;
    }

    /**
     * 获取FTP目录下的文件
     *
     * @param files
     * @param ftpClient
     * @param path           FTP目录
     * @param filenameRegexp 文件名正则表达式
     * @param recursive      是否递归搜索
     * @throws IOException
     */
    public static void listFile(List<FtpFile> files, FTPClient ftpClient, String path, String filenameRegexp,
                                boolean recursive) throws IOException {
        String _path = new String(path.getBytes("UTF-8"), "iso-8859-1");
        for (FTPFile ftpFile : ftpClient.listFiles(_path)) {
            if (ftpFile.isFile()) {
                if (Pattern.matches(filenameRegexp, ftpFile.getName())) {
                    files.add(new FtpFile(path + "/" + ftpFile.getName(), ftpFile.getSize(), ftpFile.getTimestamp().getTimeInMillis()));
                }
            } else if (recursive && ftpFile.isDirectory()) {
                listFile(files, ftpClient, path + "/" + ftpFile.getName(), filenameRegexp, recursive);
            }
        }
    }

    /**
     * 关闭FTP客户端连接
     *
     * @param ftpClient
     */
    public static void closeFtpClient(FTPClient ftpClient) {
        if (ftpClient != null) {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
//				LOGGER.error(Throwables.getStackTraceAsString(e));
            }
        }
    }


    public static void main(String[] args) {

        try {
            FTPClient client = getFtpClient("39.106.143.200", 21, "wordpress", "123456");
            FTPFile[] files = client.listFiles(".");
            for (FTPFile f: files){
                System.out.println(f.toFormattedString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        String filenameRegexp = "([\\w\\W]*).csv";
//        String[] filenames = new String[]{"ftp_demo.csv", "demo.csv"};
//
//        for (String name : filenames) {
//            System.out.println(Pattern.matches(filenameRegexp, name));
//        }
    }
}
