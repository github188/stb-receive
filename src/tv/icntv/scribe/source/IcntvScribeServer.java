package tv.icntv.scribe.source;

import java.nio.channels.FileChannel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;


import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.facebook.fb303.fb_status;


public class IcntvScribeServer {
    private static final Logger LOG = LoggerFactory.getLogger(IcntvScribeServer.class);

    public static final String SCRIBE_CATEGORY = "category";

    private static final int DEFAULT_WORKERS = 10;

    private String splitter="\r\n";
    private TServer server;
    private int port = 14630;
    private int workers = DEFAULT_WORKERS;


    private class Startup extends Thread {

        public void run() {
            try {
                TProcessor processor = new scribe.Processor(new Receiver());
                TNonblockingServerTransport transport = new TNonblockingServerSocket(port);
                THsHaServer.Args args = new THsHaServer.Args(transport);

                args.workerThreads(workers);
                args.processor(processor);
                args.transportFactory(new TFramedTransport.Factory());
                args.protocolFactory(new TBinaryProtocol.Factory());

                server = new THsHaServer(args);

                LOG.info("Starting Scribe Source on port " + port);

                server.serve();
            } catch (Exception e) {
                LOG.warn("Scribe failed", e);
            }
        }
    }


    public void start() {

        Startup startupThread = new Startup();
        startupThread.start();

    }
    FileUtils fileUtils=null;

    FileChannel channel = null;
    public IcntvScribeServer(int port,int worker){
    	this.port=port;
    	this.workers=worker;
    	fileUtils=new FileUtils();
    }
    public IcntvScribeServer(){
    	this(14630,10);
    }
    class Receiver implements scribe.Iface {
    	

        @Override
        public tv.icntv.scribe.source.ResultCode Log(
                List<tv.icntv.scribe.source.LogEntry> messages)
                throws TException {
            if (messages != null) {
            	List<String> msgs=Lists.transform(messages, new Function<LogEntry, String>() {

					@Override
					public String apply(LogEntry arg0) {
						return arg0.getMessage();
					}
				});
            	String message=Joiner.on(splitter).join(msgs);
            	fileUtils.write(message+splitter);

                return ResultCode.OK;
            }
            return ResultCode.TRY_LATER;
        }

        @Override
        public String getName() throws TException {
            // TODO Auto-generated method stub
            return "icntv";
        }

        @Override
        public String getVersion() throws TException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public fb_status getStatus() throws TException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getStatusDetails() throws TException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Map<String, Long> getCounters() throws TException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getCounter(String key) throws TException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setOption(String key, String value) throws TException {
            // TODO Auto-generated method stub

        }

        @Override
        public String getOption(String key) throws TException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Map<String, String> getOptions() throws TException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getCpuProfile(int profileDurationInSec) throws TException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long aliveSince() throws TException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void reinitialize() throws TException {
            // TODO Auto-generated method stub

        }

        @Override
        public void shutdown() throws TException {
            // TODO Auto-generated method stub

        }

    }

    public static void main(String[] args) {
        new IcntvScribeServer(Integer.parseInt(args[0]),Integer.parseInt(args[1])).start();

    }
}
