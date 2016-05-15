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

    public byte[] getHash(String hashAlgo, int offset, int length)
    {
        if(Files.notExists(this.path_to_file))
            return null;
        else if(Files.isReadable(this.path_to_file))
            return null;

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

    public byte[] getDataBytes(int offset, int length)
    {
        return new byte[0];
    }

    public OpenFileOperationStatus fileMatchDescription(byte[] hash, String hashAlgo)
    {
        if(Files.notExists(this.path_to_file))
            return OpenFileOperationStatus.FILE_DOES_NOT_EXIST;
        /*else if(Files.isReadable(this.path_to_file))
            return OpenFileOperationStatus.ACCESS_RESTRICTED;*/

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
        else if(Files.isReadable(this.path_to_file))
            return OpenFileOperationStatus.ACCESS_RESTRICTED;

        if(hashAlgo.compareTo("SHA-1") == 0)
        {
            byte[] originalFileHash;
            try
            {
                MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
                InputStream fis = Files.newInputStream(this.path_to_file, StandardOpenOption.READ);
                int n = 0;
                byte[] buffer = new byte[8192];
                fis.read(buffer, offset, 8192);
                sha1.update(buffer, 0, n);
                while (n != -1) {
                    n = fis.read(buffer);
                    if (n > 0) {
                        sha1.update(buffer, 0, n);
                    }
                }
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
}
