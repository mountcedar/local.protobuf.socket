package local.protobuf.socket.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import local.protobuf.socket.data.DataBuilder;
import local.protobuf.socket.data.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtocolBufferClient {
	/** for logging */
	protected static Logger logger = LoggerFactory.getLogger(ProtocolBufferClient.class);

	public static final int PORT =  1111;

	protected DataBuilder builder = null;
	protected Socket socket = null;
	protected String host = null;
	protected boolean isConnected = false;

	protected ObjectInputStream in = null;
	protected ObjectOutputStream out = null;

	public static ProtocolBufferClient create(String hostname, DataBuilder builder) {
		try {
			ProtocolBufferClient instance = new ProtocolBufferClient(hostname, builder);
			instance.socket = new Socket(hostname, PORT);
			logger.info("Connected: {}", instance.socket.getInetAddress().getHostName());
			return instance;
		} catch (Exception e) {
			logger.error("{}", e);
			return null;
		}
	}

	protected ProtocolBufferClient(String hostname, DataBuilder builder) {
		this.host = hostname;
		this.builder = builder;
	}

	public boolean send(Serializable data) {
		try {
			if (out == null) out = new ObjectOutputStream(socket.getOutputStream());

			byte size = data.getSerializedSize();
			out.writeByte(size);
			out.flush();
			out.write(data.serialize());
			out.flush();
			return true;
		} catch (Exception e) {
			logger.error("{}", e);
			return false;
		}
	}

	@SuppressWarnings("finally")
	public Serializable recv() {
		try {
			if (in == null) in = new ObjectInputStream(socket.getInputStream());

			byte size = in.readByte();
			byte[] buf = new byte[size];
			in.readFully(buf);
			return builder.create(buf);
		} catch (IOException e) {
			logger.error("{}", e);
		} finally {
		      try {
		    	  if (socket != null) {
		    		  socket.close();
		    		  in = null;
		    		  out = null;
		    	  }
		      } catch (IOException e) {}
		      logger.info("Disconnected: {}", socket.getRemoteSocketAddress());
		      return null;
		}
	}
}
