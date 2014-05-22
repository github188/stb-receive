package tv.icntv.lzo.compression;/*
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

import com.hadoop.compression.lzo.LzopCodec;

import org.apache.hadoop.conf.Configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by leixw
 * <p/>
 * Author: leixw
 * Date: 2014/04/04
 * Time: 11:48
 */
public class LzoCompressImpl implements Compress {


    private LzopCodec lzoCodec;

    public LzoCompressImpl() {
        init();
    }

    private void init() {
        lzoCodec = new LzopCodec();
        lzoCodec.setConf(getConfiguration());
    }

    protected Configuration getConfiguration() {
        Configuration conf = new Configuration();
        conf.set("mapred.job.tracker", "local");
        conf.set("io.compression.codecs", "com.hadoop.compression.lzo.LzopCodec");
        return conf;
    }

    @Override
    public OutputStream getOutputStream(String target) throws IOException {
        File file = new File(target);
        if (!file.exists()) {
            file.createNewFile();
        }
        return lzoCodec.createOutputStream(new FileOutputStream(file));
    }

}
