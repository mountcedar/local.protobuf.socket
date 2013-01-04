package local.protobuf.socket.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import local.protobuf.socket.data.DataBuilder;
import local.protobuf.socket.data.Recievable;
import local.protobuf.socket.data.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
	/** for logging */
	protected static Logger logger = LoggerFactory.getLogger(RequestHandler.class);

	protected Socket socket = null;
	protected DataBuilder builder = null;
	protected ObjectInputStream in = null;
	protected ObjectOutputStream out = null;
	protected boolean terminate = false;
	protected List<Recievable> recievers = null;

	public static RequestHandler create(Socket socket, DataBuilder builder, List<Recievable> recievers) {
		try {
			RequestHandler instance = new RequestHandler(socket, builder, recievers);
			instance.in = new ObjectInputStream(socket.getInputStream());
			instance.out = new ObjectOutputStream(socket.getOutputStream());
			return instance;
		} catch (IOException e) {
			logger.error("{}", e);
			return null;
		}
	}

	protected RequestHandler (Socket socket, DataBuilder builder, List<Recievable> recirvers) {
		this.socket = socket;
		this.builder = builder;
		this.recievers = recirvers;
	    logger.info("Connected: {}", socket.getRemoteSocketAddress());
	}

	public void shutdown() {
		try {
			terminate = true;
			this.socket.close();
			this.join(100);
			terminate = false;
		} catch (Exception e) {
			logger.error("{}", e);
		}
	}

	protected boolean send(Serializable data) {
		try {
			byte size = data.getSerializedSize();
			out.writeByte(size);
			out.write(data.serialize());
			return true;
		} catch (Exception e) {
			logger.error("{}", e);
			return false;
		}
	}

	public void run() {
		try {
			while (!terminate) {
				byte size = in.readByte();
				//logger.debug("data size: {}", size);
				byte[] buf = new byte[size];
				in.readFully(buf);
				//logger.debug("data serial: {}", buf);
				Serializable data = builder.create(buf);
				//logger.debug("data: {}", data);
				if (data == null) continue;
				for (Recievable reciever: recievers) {
					//logger.debug("calling reciever: {}", reciever);
					reciever.onRecv(data);
				}
			}
	    } catch (IOException e) {
	    	logger.error("{}", e.getMessage());
	    } finally {
	    	try {
	    		if (socket != null && !socket.isClosed()) {
	    			socket.close();
	    		}
	    	} catch (IOException e) {}

	    	logger.info("Disconnected: {}", socket.getRemoteSocketAddress());
	    }
	}
}