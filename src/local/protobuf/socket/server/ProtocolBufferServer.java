/**
	@brief the server to stream protocol buffer message.
 */
package local.protobuf.socket.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import local.protobuf.socket.data.DataBuilder;
import local.protobuf.socket.data.Recievable;
import local.protobuf.socket.data.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @brief the server to stream or recieve protocol buffer
 * @author sugiyama
 */
public class ProtocolBufferServer extends Thread {
	/** for logging */
	protected static Logger logger = LoggerFactory.getLogger(ProtocolBufferServer.class);

	public static final int PORT = 1111;

	protected boolean terminate = false;
	protected ServerSocket serverSocket = null;
	protected DataBuilder builder = null;
	protected List<RequestHandler> requests = null;
	protected List<Recievable> recievers = null;

	/**
	 * @brief constructor
	 */
	public ProtocolBufferServer(DataBuilder builder) {
		this.builder = builder;
		this.requests = new ArrayList<RequestHandler>();
		this.recievers = new ArrayList<Recievable>();
	}

	public void register(Recievable e) {
		this.recievers.add(e);
	}

	public void shutdown() {
		try {
			for (RequestHandler handler: requests) {
				handler.shutdown();
			}
			this.terminate = true;
			this.serverSocket.close();
			this.serverSocket = null;
			this.join(100);
			this.terminate = false;
			logger.info("protocol buffer server terminated.");
		} catch (Exception e) {
			logger.error("{}", e);
		}
	}

	public void send(Serializable e) {
		for (RequestHandler handler: requests) {
			handler.send(e);
		}
	}

	@Override
	public void run() {
	    try {
	    	serverSocket = new ServerSocket(PORT);
	    	logger.info("ProtocolBufferServer is (port={})", serverSocket.getLocalPort());
	    	while (!terminate) {
	    		Socket socket = serverSocket.accept();
	    		RequestHandler handler = RequestHandler.create(socket, builder, recievers);
	    		requests.add(handler);
	    		handler.start();
	    		for (RequestHandler e: requests) {
	    			if (!e.isAlive()) requests.remove(handler);
	    		}
	    	}
	    	logger.info("thread terminated.");
	    } catch (IOException e) {
	    	logger.error("{}", e.getMessage());
	    } finally {
	    	try {
	    		if (serverSocket != null && !serverSocket.isClosed()) {
	    			serverSocket.close();
	    		}
	    	} catch (IOException e) {}
	    }
	}
}
