package pft.file_operation;

/**
 * Created by rabbiddog on 5/14/16.
 */
import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;
import pft.file_operation.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

public class IFileFacadeTest {

    @Test
    public void CreateFileManagerWhenFileExitsTest()
    {
        IFileFacade manager = new PftFileManager("./TestFile");

        Assert.assertNotNull(manager);
    }

    @Test
    public void CreateFileManagerWhenFileDoesNotExitsTest()
    {
        IFileFacade manager = new PftFileManager("./NoSuchFile");

        Assert.assertNotNull(manager);
    }

    @Test
    public void fileMatchDescriptionFullFileCorrectHash()
    {
        try {

            String current = new java.io.File( "." ).getCanonicalPath();
            String path = current+"/src/test/java/pft/file_operation/TestFile";
            byte[] file_hash = getFileHash(path);
            IFileFacade manager = new PftFileManager(path);

            OpenFileOperationStatus result =  manager.fileMatchDescription(file_hash, "SHA-1");

            Assert.assertSame(OpenFileOperationStatus.HASH_MATCH, result);
        }catch(IOException ex) {

        }
    }

    @Test
    public void fileMatchDescriptionFullFileIncorrectHash()
    {
        try {

            String current = new java.io.File( "." ).getCanonicalPath();
            String path = current+"/src/test/java/pft/file_operation/I Want To Break Free.mp3";
            byte[] file_hash = getFileHash(path);
            IFileFacade manager = new PftFileManager(current + "/src/test/java/pft/file_operation/TestFile");
            OpenFileOperationStatus result =  manager.fileMatchDescription(file_hash, "SHA-1");

            Assert.assertSame(OpenFileOperationStatus.HASH_DOES_NOT_MATCH, result);

        }catch(IOException ex) {

        }
    }

    private byte[] getFileHash(String path)
    {
        String file_separator = System.getProperty("file.separator");
        String pattern = Pattern.quote(file_separator);
        String[] splittedFileName = path.split(pattern);

        String directory =  "";
        for(int i = 0; i < splittedFileName.length - 1; i++ )
        {
            directory += (splittedFileName[i] + file_separator);
        }
        Path path_to_file = FileSystems.getDefault().getPath( directory, splittedFileName[splittedFileName.length-1]);

        try
        {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            InputStream fis = Files.newInputStream(path_to_file, StandardOpenOption.READ);
            int n = 0;
            byte[] buffer = new byte[8192];

            while (n != -1) {
                n = fis.read(buffer);
                if (n > 0) {
                    sha1.update(buffer, 0, n);
                }
            }
            return sha1.digest();
        }catch(NoSuchAlgorithmException ex)
        {
            return null;
        }
        catch (FileNotFoundException fex)
        {
            return null;
        }catch (IOException ioex)
        {
            return null;
        }
    }

}
