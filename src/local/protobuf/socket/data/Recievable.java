package local.protobuf.socket.data;

public interface Recievable {
	boolean onRecv(Serializable data);
}
