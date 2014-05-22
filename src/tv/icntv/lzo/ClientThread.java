package tv.icntv.lzo;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;

import tv.icntv.common.PropertiesUtils;
import tv.icntv.lzo.compression.LzoCompressImpl;
import tv.icntv.lzo.decompression.DefaultUnCompress;
import tv.icntv.lzo.push.HdfsDispatcher;

public class ClientThread extends Thread{
//    private  String lzoDirectory = "/data/hadoop/icntv/log/lzoData/";
	static Properties pro=PropertiesUtils.getProperties();
	private Logger logger = LoggerFactory.getLogger("thread-hdfs");
	private static String suffix=pro.getProperty("icntv.stb.compress.log.suffix",".lzo");
	private String source;
	private String lzoFile;
	private String hdfsUrl=null;
	private String lzoFileName="";

	public ClientThread(String source){
	
		this(source,pro.getProperty("icntv.stb.compress.log.path","/data/hadoop/icntv/log/lzoData"),pro.getProperty("icntv.stb.hdfs.url","hdfs://icntv/icntv/log/stb"));
	}
	
	public ClientThread(String source,String lzoPath,String hdfs){
		this.source=source;
		if(!new File(source).exists()){
			throw new NullPointerException("source log not exit name="+source);
		}
//		logger.info("lzo path:{}",lzoPath);
		File lzo = new File(lzoPath);
		if(!lzo.exists()){
			lzo.mkdirs();
		}
		List<String> sources=Splitter.on(File.separator).splitToList(source);
		String fileName=sources.get(sources.size()-1);
		lzoFileName=fileName.substring(0,fileName.lastIndexOf("."))+suffix;
		lzoFile=lzoPath+File.separator+lzoFileName;
//		logger.info("lzoFile path:{}",lzoFile);
		this.hdfsUrl=hdfs;
//		logger.info("source ="+source+"\r\n lzo="+lzoFile+"\r\n hdfsurl="+hdfs);
	}
	@Override
	public void run() {
		 long start=System.nanoTime();
         ReCompress reCompress = new DefaultCompress(source,lzoFile,new LzoCompressImpl(),new DefaultUnCompress());
        try {
            reCompress.reCompress();
        } catch (IOException e) {
            System.out.println(e);
            return;

        }
        logger.info("use time = "+(System.nanoTime()-start)/Math.pow(10,9));

        //get target
        logger.info(lzoFile+"\t"+hdfsUrl+File.separator+lzoFileName);
        HdfsDispatcher hdfsDispatcher = new HdfsDispatcher(lzoFile, hdfsUrl+File.separator+lzoFileName);
        if(hdfsDispatcher.send()){
        	logger.info("send hdfs success");
        	//new File(source).delete();
        };
        
	}
	
	public static void main(String[] args) {
		new ClientThread(args[0],args[1],args[2]).start();
	}

}
