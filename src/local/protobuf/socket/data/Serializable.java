package local.protobuf.socket.data;

public interface Serializable {
	public byte[] serialize();
	public boolean deserialize(byte[] binaries);
	public byte getSerializedSize();
}
