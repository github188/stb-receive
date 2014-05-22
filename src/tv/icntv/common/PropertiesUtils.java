package tv.icntv.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtils {
	public static Properties getProperties(String file){
		InputStream in=PropertiesUtils.class.getClassLoader().getResourceAsStream(file);
		Properties pro=new Properties();
		try {
			pro.load(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pro;
	}
	
	public static Properties getProperties(){
		return getProperties("icntvStb.properties");
	}

}
