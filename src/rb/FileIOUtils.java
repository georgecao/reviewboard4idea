package rb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileIOUtils {
    public static <T> void fileWrite(T testResult, String fileName, Long start) {
        String startTime = formatNowDate(start);
        String dir = System.getProperty("java.io.tmpdir");
        fileName = dir + File.pathSeparator + fileName + "_" + startTime + ".txt";
        try {
            File file = new File(fileName);
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(testResult.toString());
            bw.newLine();
            bw.close();
        } catch (Exception e) {
            System.out.println("fileWrite error:" + e);
        }
    }

    public static String formatNowDate(Long time) {
        Date nowTime = new Date(time);
        SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd hh-mm");
        String retStrFormatNowDate = sdFormatter.format(nowTime);
        return retStrFormatNowDate;
    }
}
