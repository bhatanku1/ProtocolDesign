package pft.file_operation;

/**
 * Created by anjum parvez ali on 5/14/16.
 */

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.regex.Pattern;

public class PftFileManager implements IFileFacade {

    private final Path path_to_file;

    /*Currently assume current working directory*/
    public PftFileManager(String path) {
        String fileSeparator =
                System.getProperty("file.separator");
        String pattern = Pattern.quote(fileSeparator);
        String[] splittedFileName = path.split(pattern);

        String directory =  "";
        for(int i = 0; i < splittedFileName.length - 1; i++ )
        {
            directory += (splittedFileName[i] + fileSeparator);
        }
        path_to_file = FileSystems.getDefault().getPath( directory, splittedFileName[splittedFileName.length-1]);
    }

    public byte[] getHash(String hashAlgo, int offset, long length)
    {
        if(Files.notExists(this.path_to_file)) {
            System.out.println("File does not exists");
            return null;
        }

        else if(!Files.isReadable(this.path_to_file)) {
            System.out.println("File not readable");

            return null;
        }


        //add more features later
        if(null == hashAlgo || hashAlgo.isEmpty())
            hashAlgo = "SHA-1";

        if(hashAlgo.compareTo("SHA-1") == 0)
        {
            byte[] originalFileHash;
            try
            {
                MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
                InputStream fis = Files.newInputStream(this.path_to_file, StandardOpenOption.READ);
                int n = 0;
                byte[] buffer = new byte[8192];

                while (n != -1) {
                    n = fis.read(buffer);
                    if (n > 0) {
                        sha1.update(buffer, 0, n);
                    }
                }
                fis.close();
                return sha1.digest();
            }
            catch (NoSuchAlgorithmException ex)
            {
                return null;
            }
            catch (FileNotFoundException fex)
            {
                return null;
            }catch (IOException ioex)
            {
                /*need to change this*/
                return null;
            }
        }

        return  null;
    }

    public OpenFileOperationStatus fileMatchDescription(byte[] hash, String hashAlgo)
    {
        if(Files.notExists(this.path_to_file))
            return OpenFileOperationStatus.FILE_DOES_NOT_EXIST;
        else if(!Files.isReadable(this.path_to_file))
            return OpenFileOperationStatus.ACCESS_RESTRICTED;

        //add more features later
        if(null == hashAlgo || hashAlgo.isEmpty())
            hashAlgo = "SHA-1";

        if(hashAlgo.compareTo("SHA-1") == 0)
        {
            byte[] originalFileHash;
            try
            {
                MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
                InputStream fis = Files.newInputStream(this.path_to_file, StandardOpenOption.READ);
                int n = 0;
                byte[] buffer = new byte[8192];

                while (n != -1) {
                    n = fis.read(buffer);
                    if (n > 0) {
                        sha1.update(buffer, 0, n);
                    }
                }
                fis.close();
                originalFileHash=  sha1.digest();
            }
            catch (NoSuchAlgorithmException ex)
            {
                return  OpenFileOperationStatus.NO_SUCH_HASH_ALGORITHM;
            }
            catch (FileNotFoundException fex)
            {
                return OpenFileOperationStatus.FILE_DOES_NOT_EXIST;
            }catch (IOException ioex)
            {
                /*need to change this*/
                return OpenFileOperationStatus.FILE_DOES_NOT_EXIST;
            }

            if(Arrays.equals(originalFileHash, hash))
                return OpenFileOperationStatus.HASH_MATCH;
            else
                return OpenFileOperationStatus.HASH_DOES_NOT_MATCH;
        }

        return OpenFileOperationStatus.NO_SUCH_HASH_ALGORITHM;
    }

    public OpenFileOperationStatus fileMatchDescription(byte[] hash, String hashAlgo, int offset, int length)
    {
        if(Files.notExists(this.path_to_file))
            return OpenFileOperationStatus.FILE_DOES_NOT_EXIST;
        else if(!Files.isReadable(this.path_to_file))
            return OpenFileOperationStatus.ACCESS_RESTRICTED;

        if(hashAlgo.compareTo("SHA-1") == 0)
        {
            byte[] originalFileHash;
            try
            {
                MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
                RandomAccessFile raf = new RandomAccessFile(new File(this.path_to_file.toString()), "r");
                raf.seek(offset);
                /*InputStream fis = Files.newInputStream(this.path_to_file, StandardOpenOption.READ);*/
                int n = 0;
                byte[] buffer = new byte[8192];
                /*fis.read(buffer, offset, 8192);
                sha1.update(buffer, 0, n);*/
                while (n != -1) {
                    n = raf.read(buffer);
                    if (n > 0) {
                        sha1.update(buffer, 0, n);
                    }
                }
                raf.close();
                originalFileHash=  sha1.digest();

            }
            catch (NoSuchAlgorithmException ex)
            {
                return  OpenFileOperationStatus.NO_SUCH_HASH_ALGORITHM;
            }
            catch (FileNotFoundException fex)
            {
                return OpenFileOperationStatus.FILE_DOES_NOT_EXIST;
            }catch (IOException ioex)
            {
                /*need to change this*/
                return OpenFileOperationStatus.FILE_DOES_NOT_EXIST;
            }

            if(Arrays.equals(originalFileHash, hash))
                return OpenFileOperationStatus.HASH_MATCH;
            else
                return OpenFileOperationStatus.HASH_DOES_NOT_MATCH;
        }

        return OpenFileOperationStatus.NO_SUCH_HASH_ALGORITHM;
    }

    private static String getFileNameFromPath(String path)
    {
        if(null == path || path.isEmpty())
            return null;

        String pattern = Pattern.quote(System.getProperty("file.separator"));
        String[] splittedFileName = path.split(pattern);

        return splittedFileName[splittedFileName.length -1];
    }

    public byte[] readFromPosition(int offset, int length)
    {
        /*assuming that file has already been verified with fileMatchDescription*/
        try
        {
            RandomAccessFile raf = new RandomAccessFile(new File(this.path_to_file.toString()), "r");
            raf.seek(offset);
            byte[] buffer = new byte[length];
            raf.read(buffer, offset, length);
            raf.close();
            return buffer;
        }
        catch (FileNotFoundException fex)
        {
            return null;
        }catch (IOException ioex)
        {
                /*need to change this*/
            return null;
        }

    }
    public int writeFromPosition(int offset, int length, byte[] data)
    {
         /*assuming that file has already been verified with fileMatchDescription*/
        try
        {
            RandomAccessFile raf = new RandomAccessFile(new File(this.path_to_file.toString()), "w");
            raf.seek(offset);
            raf.write(data);
            raf.close();
            return offset+length;
        }
        catch (FileNotFoundException fex)
        {
            return offset;
        }catch (IOException ioex)
        {
                /*need to change this*/
            return offset;
        }
    }

    public long getSize()
    {
        if(Files.notExists(this.path_to_file))
            return 0;
        else if(!Files.isReadable(this.path_to_file))
            return 0;

        File file = new File(this.path_to_file.toString());
        return file.length();
    }

    public boolean fileExits()
    {
        if(Files.notExists(this.path_to_file))
            return false;
        if(!Files.isReadable(this.path_to_file))
            return false;

        return true;
    }

}
