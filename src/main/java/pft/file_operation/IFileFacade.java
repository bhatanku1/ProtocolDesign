package pft.file_operation;

/**
 * Created by anjum parvez ali  on 5/14/16.
 */


public interface IFileFacade {

    public byte[] getHash(String hashAlgo, int offset, int length);
    public OpenFileOperationStatus fileMatchDescription(byte[] hash, String hashAlgo);
    public OpenFileOperationStatus fileMatchDescription(byte[] hash, String hashAlgo, int offset, int length);
    public byte[] readFromPosition(int offset, int length);
    public long writeFromPosition(long offset, long length, byte[] data);
    public long getSize();
    public boolean fileExits();
    public String getFileName();
    public boolean deleteFile();
}
