package main;

import local.protobuf.socket.client.ProtocolBufferClient;
import local.protobuf.socket.data.DataBuilder;
import local.protobuf.socket.data.Recievable;
import local.protobuf.socket.data.Serializable;
import local.protobuf.socket.server.ProtocolBufferServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sample.Sample.Message;

public class App {
	/** for logging */
	protected static Logger logger = LoggerFactory.getLogger(App.class);

	protected static class Data implements Serializable {
		public Message data = null;

		public Data() {}

		public Data(Message data) {
			this.data = data;
		}

		@Override
		public byte[] serialize() {
			if (data == null) return null;
			return data.toByteArray();
		}

		@Override
		public boolean deserialize(byte[] binaries) {
			try {
				this.data = Message.parseFrom(binaries);
				return true;
			} catch (Exception e) {
				logger.error("{}", e);
				return false;
			}
		}

		@Override
		public byte getSerializedSize() {
			if (data == null) return 0;
			return (byte)data.getSerializedSize();
		}

	}

	public static void main(String[] args) {
		try {
			Message msg = Message.newBuilder()
					.setId(1)
					.setData("hogehoge")
					.build();

			DataBuilder builder = new DataBuilder() {
				@Override
				public Serializable create(byte[] binary) {
					try {
						return new Data(Message.parseFrom(binary));
					} catch (Exception e) {
						logger.error("{}", e);
						return null;
					}
				}
			};

			Recievable reciever = new Recievable() {
				@Override
				public boolean onRecv(Serializable data) {
					Data _data = (Data)data;
					logger.debug("data:");
					logger.debug("\tid: {}", _data.data.getId());
					logger.debug("\tdata: {}", _data.data.getData());
					return true;
				}
			};

			ProtocolBufferServer server = new ProtocolBufferServer(builder);
			server.register(reciever);
			server.start();

			ProtocolBufferClient client = ProtocolBufferClient.create("localhost", builder);
			if (client == null) {
				logger.error("cannot connect to the server.");
				server.shutdown();
				return;
			}
			for (int i = 0; i < 10; i++) {
				logger.debug("sending data [{}]", i);
				client.send(new Data(msg));
				logger.debug("sended data [{}]", i);
			}

			server.shutdown();
			logger.info("all process completed.");
			return;
		} catch (Exception e) {
			logger.error("{}", e);
		}
	}
}
