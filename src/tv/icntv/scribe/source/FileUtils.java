package tv.icntv.scribe.source;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tv.icntv.common.PropertiesUtils;
import tv.icntv.lzo.ClientThread;


public class FileUtils {

	Properties pro = PropertiesUtils.getProperties();
	private Logger logger = LoggerFactory.getLogger(FileUtils.class);
	private String path=pro.getProperty("icntv.stb.log.path","/data/hadoop/icntv/log/data/");

	private String baseFileName="stb-";
	
	private String suffix=pro.getProperty("icntv.stb.log.file.suffix", ".log");
	private int rollInterval=Integer.parseInt(pro.getProperty("icntv.stb.log.rollInterval", "1"));
	ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
	private String hdfs=pro.getProperty("icntv.stb.hdfs.url","hdfs://icntv/icntv/log/stb");
	private String lzoPath=pro.getProperty("icntv.stb.compress.log.path","/data/hadoop/icntv/log/lzoData");
	public String fileName="";
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public FileUtils (){
		if(!new File(path).exists()){
			new File(path).mkdirs();
		}
		scheduler.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				String tmp=path+File.separator+getCurrentDate();
				File tmpFile=new File(tmp);
				if(!tmpFile.exists()){
					tmpFile.mkdirs();
				}
				
				setFileName(tmp+File.separator+baseFileName+getCurrentDate()+"-"+String.format("%tH",new Date()) +suffix);
				logger.info("set basename from "+currentFile+" to "+getFileName());
			}
		}, 0, rollInterval, TimeUnit.HOURS);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
	}
	
    private String currentFile="";
	public FileChannel getOutChannel(){
		try {
			
				if(!currentFile.equals(getFileName())){
					if(!currentFile.equals("")){
						logger.info("start compress and send to hdfs");
						new ClientThread(currentFile,lzoPath+File.separator+day(),hdfs+File.separator+day()).start();;
					}
					currentFile=getFileName();
				}
					
			return new RandomAccessFile(currentFile,"rw").getChannel();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	};
	
	private String getCurrentDate(){
		return String.format("%tF", new Date());
	}
	
	private String day(){
		String tmp = currentFile.substring(currentFile.lastIndexOf(File.separator)+1);
		tmp=tmp.replace(baseFileName, "").replace(suffix, "");
		return tmp.substring(0,tmp.lastIndexOf("-"));
	}
	private String charset=pro.getProperty("icntv.stb.charset", "utf-8");
	public void write(String message){
		FileChannel channel=getOutChannel();
		try {
			channel.position(channel.size());
			channel.write(ByteBuffer.wrap(message.getBytes(charset)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			channel.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
