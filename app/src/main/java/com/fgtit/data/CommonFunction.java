package com.fgtit.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommonFunction {

    private static CommonFunction instance;

    private CommonFunction(){
    }

    public static CommonFunction getInstance() {
        if(null == instance) {
            instance = new CommonFunction();
        }
        return instance;
    }

  public List<String> RetriveCapturedImagePath(String path) {
        List<String> tFileList = new ArrayList<String>();
        File f = new File(path);
        if (f.exists()) {
            File[] files=f.listFiles();
            Arrays.sort(files);

            for(int i=0; i<files.length; i++){
                File file = files[i];
                if(file.isDirectory())
                    continue;
                tFileList.add(file.getPath());
            }
        }
        return tFileList;
    }


}
