package com.hexing.libhexbase.tools.file;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.text.TextUtils;

import com.hexing.libhexbase.log.HexLog;
import com.hexing.libhexbase.tools.TimeUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author caibinglong
 *         date 2018/1/3.
 *         desc desc
 */

public class FileUtil {

    private static List<Map<String, Object>> fileList = new ArrayList<>();

    /**
     * 搜索 sd 卡文件
     *
     * @param context  上下文
     * @param keyword  关键字
     * @param filePath 目录
     * @return List<Map<String, Object>>
     */
    public static List<Map<String, Object>> searchFile(Context context, String keyword, String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            filePath = getSDCardDir(context, "");
        }
        fileList.clear();
        return searchFile(keyword, new File(filePath));
    }


    /**
     * 搜索文件
     *
     * @param keyword 关键字
     * @param file    文件
     * @return List<Map<String, Object>>
     */
    private static List<Map<String, Object>> searchFile(String keyword, File file) {
        File[] files = file.listFiles();
        int index = 0;
        Map<String, Object> fileItem;
        if (files.length > 0) {
            for (File item : files) {
                if (item.isDirectory()) {
                    //目录
                    if (item.canRead()) {
                        searchFile(keyword, item);
                    }
                } else {
                    //文件
                    try {
                        if (item.getName().toUpperCase().contains(keyword.toUpperCase())) {
                            fileItem = new HashMap<>();
                            fileItem.put("number", index);
                            fileItem.put("name", item.getName());
                            fileItem.put("path", item.getPath());
                            fileItem.put("length", item.length());
                            fileList.add(fileItem);
                            index++;
                        }

                    } catch (Exception ex) {
                        HexLog.e("search file error=" + ex.getMessage());
                    }
                }
            }
        }
        return fileList;
    }


    public static String getSDCardDir(Context context, String uniqueName) {
        String sdPath;
        // 判断外存SD卡挂载状态，如果挂载正常，创建SD卡缓存文件夹
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            sdPath = Environment.getExternalStorageDirectory().getPath() + File.separator + uniqueName;
        } else {
            // SD卡挂载不正常，获取本地缓存文件夹（应用包所在目录）
            sdPath = context.getCacheDir().getPath() + File.separator + uniqueName;
        }
        return sdPath;
    }

    /**
     * 读取文件 输出String
     *
     * @param filePath path
     * @return String
     */
    public static String readFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder("");
        try {
            FileInputStream inputStream = new FileInputStream(filePath);
            byte[] temp = new byte[1024];
            int len;
            try {
                while ((len = inputStream.read(temp)) > 0) {
                    stringBuilder.append(new String(temp, 0, len));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    /**
     * 检查SD卡是否存在
     *
     * @return bool
     */
    public static boolean checkSDCard() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 从asset路径下读取对应文件转String输出
     *
     * @param mContext 上下文
     * @return String
     */
    public static String getJson(Context mContext, String fileName) {
        // TODO Auto-generated method stub
        StringBuilder sb = new StringBuilder();
        AssetManager am = mContext.getAssets();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(am.open(fileName)));
            String next;
            while (null != (next = br.readLine())) {
                sb.append(next);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            sb.delete(0, sb.length());
        }
        return sb.toString().trim();
    }

    public static byte[] getBytesFromFile(File file) throws IOException {
        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
            throw new IOException("File is too large!");
        }

        if (length == 0) {
            return null;
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead;

        InputStream is = new FileInputStream(file);
        try {
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
        } finally {
            is.close();
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        return bytes;
    }

    /**
     * InputStream --> File
     *
     * @param ins  InputStream
     * @param file File
     */
    public static File inputStreamToFile(InputStream ins, File file) {
        return inputStreamToFile(ins, file, "UTF-8", true);
    }

    /**
     * InputStream --> File
     *
     * @param ins  InputStream
     * @param file File
     */
    public static File inputStreamToFile(InputStream ins, File file, String encoding, boolean isExcel) {
        FileWriter filerWriter;
        try {
            filerWriter = new FileWriter(file, true);//后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
            BufferedWriter bufWriter = new BufferedWriter(filerWriter);
            String tmp = InputStreamTOString(ins, encoding);
            if (isExcel) {
                //excel 采用 ASNI编码
                bufWriter.write(0xFEFF);
            }
            bufWriter.write(tmp);
            bufWriter.newLine();
            bufWriter.close();
            filerWriter.close();
            ins.close();
            //os = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
            return file;
        }

        return file;
    }

    /**
     * 将InputStream转换成某种字符编码的String
     *
     * @param in
     * @param encoding
     * @return
     * @throws Exception
     */

    public static String InputStreamTOString(InputStream in, String encoding) throws Exception {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        byte[] data = new byte[8192];

        int count;

        while ((count = in.read(data, 0, 8192)) != -1) {

            outStream.write(data, 0, count);
        }
        return new String(outStream.toByteArray(), encoding);

    }

    /**
     * 按行读取文件
     *
     * @param file file
     */
    public static List<String> readFileOnLine(File file) {
        List<String> dataList = new ArrayList<>();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (inputStream == null) {
            System.out.print("读取文本内容,文件解析失败");
            return dataList;
        }
        StringBuilder strBuilder = new StringBuilder();
        InputStreamReader streamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(streamReader);
        String strLine;
        try {
            //通过read line按行读取
            while ((strLine = bufferedReader.readLine()) != null) {
                //strLine就是一行的内容
                if (!TextUtils.isEmpty(strLine)) {
                    strBuilder.append(strLine);
                    dataList.add(strBuilder.toString());
                    strBuilder = new StringBuilder();
                }
            }
            bufferedReader.close();
            streamReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataList;
    }

    public static List<File> getFileListByDirPath(String path, FileFilter filter) {
        File directory = new File(path);
        File[] files = directory.listFiles(filter);
        if (files == null) {
            return new ArrayList<>();
        }
        List<File> result = Arrays.asList(files);
        Collections.sort(result, new FileComparator());
        return result;
    }

    public static String cutLastSegmentOfPath(String path) {
        return path.substring(0, path.lastIndexOf("/"));
    }

    /**
     * 计算文件大小
     *
     * @param size size
     * @return 返回  MB 带单位的
     */
    public static String getReadableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * 计算  文件大小
     *
     * @param size long
     * @return String
     */
    public static String formatFromSize(long size) {
        String suffix = null;
        if (size >= 1024) {
            suffix = "KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MB";
                size /= 1024;
            }
        }
        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));
        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null)
            resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    /**
     * 打开 open assets
     *
     * @param context  上下文
     * @param fileName 文件名
     * @return InputStream
     */
    public static InputStream openAssetsFile(Context context, String fileName) {
        try {
            return context.getAssets().open(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 删除目录（文件夹）以及目录下的文件
     *
     * @param sPath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String sPath) {
        if (TextUtils.isEmpty(sPath)) {
            return false;
        }

        boolean flag;
        // 如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        final File dirFile = new File(sPath);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        // 删除文件夹下的所有文件(包括子目录)
        final File[] files = dirFile.listFiles();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                // 删除子文件
                if (files[i].isFile()) {
                    flag = deleteFile(files[i].getAbsolutePath());
                    if (!flag)
                        break;
                } // 删除子目录
                else {
                    flag = deleteDirectory(files[i].getAbsolutePath());
                    if (!flag)
                        break;
                }
            }
        }
        if (!flag)
            return false;
        // 删除当前目录
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 删除文件夹内所有文件
     *
     * @param delPath delPath path of file
     * @return boolean the result
     */
    public static boolean deleteAllFile(String delPath) {
        try {
            // create file
            final File file = new File(delPath);
            if (!file.isDirectory()) {
                file.delete();
            } else if (file.isDirectory()) {
                final String[] fileList = file.list();
                final int size = fileList.length;
                for (int i = 0; i < size; i++) {
                    // create new file
                    final File delFile = new File(delPath + "/" + fileList[i]);
                    if (!delFile.isDirectory()) {
                        delFile.delete();
                    } else if (delFile.isDirectory()) {
                        // digui
                        deleteFile(delPath + "/" + fileList[i]);
                    }
                }
                file.delete();
            }
        } catch (Exception ex) {
            HexLog.e(ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return bool
     */
    public static boolean deleteFile(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            final File file = new File(filePath);
            if (file.exists()) {
                return file.delete();
            }
        }
        return false;
    }


    public static String getExceptionMessage(Exception ex) {
        String result = "";
        StackTraceElement[] stes = ex.getStackTrace();
        for (int i = 0; i < stes.length; i++) {
            result = result + stes[i].getClassName()
                    + "." + stes[i].getMethodName()
                    + "  " + stes[i].getLineNumber() + "line"
                    + "\r\n";
        }
        return result;
    }
}
