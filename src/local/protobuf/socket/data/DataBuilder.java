package local.protobuf.socket.data;

public interface DataBuilder {
	public Serializable create(byte[] binary);
}
