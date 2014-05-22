package tv.icntv.lzo;/*
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


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by leixw
 * <p/>
 * Author: leixw
 * Date: 2014/04/14
 * Time: 14:01
 */
public abstract class AbstractReCompress implements ReCompress {
    private int BUFFER = 128 * 1024* 1024;
    private String sourceFile;
    private String targetFile;

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getTargetFile() {
        return targetFile;
    }

    public void setTargetFile(String targetFile) {
        this.targetFile = targetFile;
    }

    public AbstractReCompress(String sourceFile, String targetFile) {
        this.sourceFile = sourceFile;
        this.targetFile = targetFile;
    }

    public abstract InputStream  getInputStream() throws IOException;
    public abstract OutputStream getOutputStream() throws IOException;
    @Override
    public boolean reCompress() throws IOException {
        InputStream inputStream=getInputStream();
        OutputStream outputStream = getOutputStream();
        try {
            int count;
            byte data[] = new byte[BUFFER];
            while ((count = inputStream.read(data, 0, BUFFER)) != -1) {
                outputStream.write(data, 0, count);
            }
        } catch (Exception e) {
            return false;
        } finally {
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        }
        return true;

    }
}
