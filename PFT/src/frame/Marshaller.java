package frame;

public interface Marshaller<T extends Frame> {
	public T decode(byte[] arr);

	public byte[] encode(T t);

}
