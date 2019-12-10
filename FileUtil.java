

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class FileUtil {
	
	private static AES aes;

    public static BufferedReader read(String path) throws IOException {
    	//decrypt(path, secret);
    	// This will reference one line at a time
        BufferedReader bufferedReader = null;

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = 
                new FileReader(path);

            // Always wrap FileReader in BufferedReader.
            bufferedReader = 
                new BufferedReader(fileReader);
 

            // Always close files.
            //bufferedReader.close();         
        }
        catch(FileNotFoundException ex) {
                         
        }
        catch(IOException ex) {
          
        }
        
        //encrypt(path, secret);
        
		return bufferedReader;
    }
    
    public static void write(String entry, String path, boolean mode) throws IOException {
    	//decrypt(path, secret);
        try {
            // Assume default encoding.
            FileWriter fileWriter =
                new FileWriter(path, mode);

            // Always wrap FileWriter in BufferedWriter.
            BufferedWriter bufferedWriter =
                new BufferedWriter(fileWriter);

            // Note that write() does not automatically
            // append a newline character.
            bufferedWriter.write(entry);
            bufferedWriter.newLine();

            // Always close files.
            bufferedWriter.close();
        }
        catch(IOException ex) {
      
        }
        //encrypt(path, secret);
    }
    
    private static void encrypt(String path, String secret) throws IOException {
    	File file = new File(path);
    	FileInputStream fis = new FileInputStream(file);
    	byte[] data = new byte[(int) file.length()];
    	fis.read(data);
    	fis.close();
    	String str = new String(data);
    	AES aes = new AES();
    	String crypted = aes.encrypt(str, secret);
    	
    	try {
            // Assume default encoding.
            FileWriter fileWriter =
                new FileWriter(path, true);

            // Always wrap FileWriter in BufferedWriter.
            BufferedWriter bufferedWriter =
                new BufferedWriter(fileWriter);

            bufferedWriter.write(crypted);
            bufferedWriter.newLine();

            // Always close files.
            bufferedWriter.close();
        }
        catch(IOException ex) {
      
        }
    	
    }
    
    private static void decrypt(String path, String secret) throws IOException {
    	File file = new File(path);
    	FileInputStream fis = new FileInputStream(file);
    	byte[] data = new byte[(int) file.length()];
    	fis.read(data);
    	fis.close();
    	String str = new String(data);
    	
    	AES aes = new AES();
    	String encrypted = aes.decrypt(str, secret);
    	
    	try {
            // Assume default encoding.
            FileWriter fileWriter =
                new FileWriter(path, true);

            // Always wrap FileWriter in BufferedWriter.
            BufferedWriter bufferedWriter =
                new BufferedWriter(fileWriter);

            bufferedWriter.write(encrypted);
            bufferedWriter.newLine();

            // Always close files.
            bufferedWriter.close();
        }
        catch(IOException ex) {
      
        }
    }
    
    private byte[] converyToBinary(String string) throws IOException {
    	return string.getBytes(StandardCharsets.UTF_8); 
    }
    
    public static boolean exists(String path) {
    	File tempFile = new File(path);
    	boolean exists = tempFile.exists();
    	return exists;
    }
    
    public static boolean delete(String path) throws IOException {
    	File file = new File(path);
    	boolean deleted = file.delete();
    	file.createNewFile();
    	return deleted;
    }
}
