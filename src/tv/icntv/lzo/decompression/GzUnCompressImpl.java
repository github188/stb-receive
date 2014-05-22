package tv.icntv.lzo.decompression;/*
 * Copyright 2014 Future TV, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * Created by leixw
 * <p/>
 * Author: leixw
 * Date: 2014/04/04
 * Time: 13:35
 */
public class GzUnCompressImpl implements UnCompress {
   public GzUnCompressImpl() {
    }

    @Override
    public InputStream getInputStream(String source) throws IOException {
        File file = new File(source);
        if (!file.exists()) {
            throw new NullPointerException("file=" + file.getPath() + " not exist ;");
        }
        return new GZIPInputStream(new FileInputStream(file));  //To change body of implemented methods use File | Settings | File Templates.
    }

//    @Override
//    public String sourcePath() {
//        return sourcePath;  //To change body of implemented methods use File | Settings | File Templates.
//    }
}
