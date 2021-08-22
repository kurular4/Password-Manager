package com.omer.pm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

// TODO NEEDS A COMPLETE REFACTOR
public class Main {
	
	private static AES aes;
	private static String key;
	private final static String path = "passwd";

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
		aes = new AES();
		startConsole();
	}

	private static void startConsole() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
		Scanner k = new Scanner(System.in);		
		
		if(!FileUtil.exists(path)) {
			File f = new File(path);
			f.createNewFile();
			System.out.println("Please, choose a master password for authentication");
	        String password = k.nextLine();
	        key = password;
	        String hashed = PBKDF2.generateStrongPasswordHash(password);
	        FileUtil.write(hashed, "mpwd", true);
	    	updateHMAC(getHMAC());
		}
		
		System.out.println("Please, enter master password");
		String passwordVerify = k.nextLine();
		String line = null;
		BufferedReader bufferedReader = FileUtil.read("mpwd");
		line = bufferedReader.readLine();
		bufferedReader.close();
		
		if(!PBKDF2.validatePassword(passwordVerify, line)) {
			System.out.println("Wrong password. Aborted..");
			System.exit(0);
		}
		
        key = passwordVerify;
		AES.setKey(key);
		
		while(true) {
			System.out.println("Pick an option to go");
			System.out.println("....................\n");
			System.out.println("1 - Create Entry");
			System.out.println("2 - Delete Entry");
			System.out.println("3 - List entries");
			System.out.println("4 - Check Integrity");
			System.out.println("5 - Generate Password");
			System.out.println("6 - Files");
			System.out.println("7 - Exit");
        
			int option = k.nextInt();        	
        
			if (option == 1) {
				createEntry();
			} else if (option == 2) {
				deleteEntry();
			} else if (option == 3) {
				listEntries();
			} else if (option == 4) {
				checkIntegrity();
			} else if (option == 5) {
				generatePassword();
			} else if (option == 6) {
				fileOptions();
			} else if(option == 7) {
				System.out.println("System shut down");
				System.exit(0);
			} else {
				System.out.println("No such option!");
			}
		}
        
	}
	

	private static void generatePassword() {
		Scanner k = new Scanner(System.in);		
		System.out.println("Enter desired password length");
		int length = k.nextInt();
		String generatedpassword = PasswordGenerator.generateRandomPassword(length);
		System.out.println(generatedpassword);
	}

	private static void listEntries() throws IOException {
		BufferedReader bufferedReader = FileUtil.read(path);
		
		String line = null;
		String decryptedLine = null;
		int index = 0;
		System.out.println("Entries\n.................................................");
		
    	while((line = bufferedReader.readLine()) != null) {
            decryptedLine = aes.decrypt(line, key);
            String[] token = decryptedLine.split("!");
            
            try {
            	System.out.print(++index + ") Domain: " + token[0]);
            	System.out.print("  Account: " + token[1]);
            	System.out.println("  Password: *****");
            } catch(Exception e) {
            	
            }
        }   
    	
    	System.out.println();
    	bufferedReader.close();
    	
    	while(true) {
    		System.out.println("Pick an option to go");
    		System.out.println("....................\n");
    		System.out.println("1 - Reveal password of specified index\n");
    		System.out.println("2 - Back to main menu\n");
		
    		Scanner k = new Scanner(System.in);		
    		int option = k.nextInt();
    		
    		if(option == 1) {
        		System.out.println("Specify index\n");
        		int entryIndex = k.nextInt();
        		if(entryIndex <= index) {
         			String entry = revealPassword(entryIndex);
        			String[] token = entry.split("!");
                    System.out.print(entryIndex + ") Domain: " + token[0]);
                    System.out.print("  Account: " + token[1]);
                    System.out.println("  Password: " + token[2] + "\n");
        		} else {
        			System.out.println("Invalid index\n");
        		}
    		} else if(option == 2) {
    			break;
    		}
		
    	}
    	
	}
	
	private static String revealPassword(int index) throws IOException {
		BufferedReader bufferedReader = FileUtil.read(path);
		int count = 0;
		String line = null;
		String decryptedLine = null;
		
		while((line = bufferedReader.readLine()) != null && count++ != index ) {
            decryptedLine = aes.decrypt(line, key);
        }   
    	
    	bufferedReader.close();
    	
    	return decryptedLine;
	}

	private static void deleteEntry() throws IOException, InvalidKeyException, SignatureException, NoSuchAlgorithmException {
		BufferedReader bufferedReader = FileUtil.read(path);
		
		String line = null;
		String decryptedLine = null;
		int index = 0;
		
		ArrayList<String[]> list = new ArrayList<>();
		
		System.out.println("Entries\n.................................................");
		
    	while((line = bufferedReader.readLine()) != null) {
            decryptedLine = aes.decrypt(line, key);
            String[] token = decryptedLine.split("!");
            list.add(token);
            System.out.print(++index + ") Domain: " + token[0]);
            System.out.print("  Account: " + token[1]);
            System.out.println("  Password: *****");
        }
    	
		bufferedReader.close();
    	
        System.out.println("Specify index to remove\n");
        Scanner k = new Scanner(System.in);		
		int selectedIndex = k.nextInt();
		
		bufferedReader = FileUtil.read(path);

		int count = 1;
		
		while((line = bufferedReader.readLine()) != null && count <= index) {
			if(count != selectedIndex) {
	        	String entry = list.get(count - 1)[0] + "!" + list.get(count - 1)[1] + "!" + list.get(count - 1)[2];
	        	System.out.println(entry);
	        	String encrypted = aes.encrypt(entry,  key);
				FileUtil.write(encrypted, path, count != 1);
			}
			
			count++;
        }
		
		if(index == 1 && selectedIndex == 1) {
			FileUtil.delete(path);
		}
		
        System.out.println("Entry removed successfully\n");

    	System.out.println();
    	bufferedReader.close();
    	
    	updateHMAC(getHMAC());
	}

	private static int createEntry() throws IOException, InvalidKeyException, SignatureException, NoSuchAlgorithmException {
		Scanner k = new Scanner(System.in);
		
		System.out.println("Enter domain name");
		String domain = k.nextLine();
		
		System.out.println("Enter account identifier (username, mail ..) ");
		String accountName = k.nextLine();

		System.out.println("Enter password ");
		String password = k.nextLine();
		
		System.out.println("Enter password again");
		String passwordCheck = k.nextLine();
		
		while(!password.equals(passwordCheck)) {
			System.out.println("Passwords do not match, try again!");
			
			System.out.println("Enter password ");
		    password = k.nextLine();
			
			System.out.println("Enter password again");
			passwordCheck = k.nextLine();
		}
		
		
		
		BufferedReader bufferedReader = FileUtil.read(path);
			        
        boolean accountexists = accountExists(accountName, domain, bufferedReader);
        
        if(accountexists) {
        	System.out.println("Account alreadys exists in this domain!");
        } else {
        	String entry = domain + "!" + accountName + "!" + password;
        	String encrypted = aes.encrypt(entry,  key);
        	FileUtil.write(encrypted, "passwd", true);
        	System.out.println("Domain: " + domain + "\nAccount identifier: " + accountName);
			System.out.println("Entry created successfully...");
        }
        
    	bufferedReader.close();
    	
    	updateHMAC(getHMAC());

		return 0;
	}
	
    private static boolean accountExists(String user, String domain, BufferedReader bufferedReader) throws IOException {
    	String line = null;
    	String decryptedLine = null;
    	
    	boolean exists = false;
    	
    	while((line = bufferedReader.readLine()) != null) {
            decryptedLine = aes.decrypt(line, key);
            String[] token = decryptedLine.split("!");
            
            if(domain.equalsIgnoreCase(token[0]) && user.equals(token[1])) {
            	exists = true;
            	break;
            }
        }   
    	
    	return exists;
    }
    
    private static void checkIntegrity() throws InvalidKeyException, SignatureException, NoSuchAlgorithmException, IOException {
  	  	BufferedReader bufferedReader = FileUtil.read("mpwd");
    	bufferedReader.readLine();
    	
    	String macVerify = bufferedReader.readLine();
    	bufferedReader.close();
    	
    	if(getHMAC().equals(macVerify)) {
    		System.out.println("Integrity Check: Positive");
    	} else {
    		System.out.println("Integrity Check: Negative");
    		System.out.println("It is adviced not to use data as it may be changed. Aborting..");
    		System.exit(0);
    	}
    }
    
    private static String getHMAC() throws IOException, InvalidKeyException, SignatureException, NoSuchAlgorithmException {
    	File file = new File(path);
    	FileInputStream fis = new FileInputStream(file);
    	byte[] data = new byte[(int) file.length()];
    	fis.read(data);
    	fis.close();
    	String str = new String(data, StandardCharsets.UTF_8);
		return HMAC.calculateHMAC(str, key);
    }
    
    private static void updateHMAC(String hmac) throws IOException {
    	BufferedReader bufferedReader = FileUtil.read("mpwd");
    	String hash = bufferedReader.readLine();
    	bufferedReader.close();
    	
    	FileUtil.write(hash, "mpwd", false);
    	FileUtil.write(hmac, "mpwd", true);
    }
    
	private static void fileOptions() throws IOException {
		Scanner k = new Scanner(System.in);

		while(true) {
			System.out.println("Pick an option to go");
			System.out.println("....................\n");
			System.out.println("1 - Decrypt File");
			System.out.println("2 - Secure File");
    		System.out.println("3 - Back to main menu\n");
		
			int option = k.nextInt();
		
			if(option == 1) {
				decryptFile();
			} else if (option == 2) {
				encryptFile();
			} else if (option == 3){
				break;
			} else {
				System.out.println("No such option!");
			}
		}
		
	}
	
	private static void encryptFile() throws IOException {
		Scanner k = new Scanner(System.in);
		System.out.println("Enter file path");
		String path = k.nextLine();	
    	
    	BufferedReader bufferedReader = FileUtil.read(path);
		
		String line = null;		
		ArrayList<String> list = new ArrayList<>();
		
    	while((line = bufferedReader.readLine()) != null) {
            list.add(line);
        }
    	
		bufferedReader.close();
    			
		bufferedReader = FileUtil.read(path);

		int count = 1;
		
		for(String linet : list) {
        	String encrypted = aes.encrypt(linet,  key);
			FileUtil.write(encrypted, path, count != 1);
        	count++;
		}
		
    	bufferedReader.close();
	}
	
	private static void decryptFile() throws IOException {
		Scanner k = new Scanner(System.in);
		System.out.println("Enter file path");
		String path = k.nextLine();	
    	
    	BufferedReader bufferedReader = FileUtil.read(path);
		
		String line = null;		
		String decryptedLine = null;
		
		ArrayList<String> list = new ArrayList<>();
		
    	while((line = bufferedReader.readLine()) != null) {
            decryptedLine = aes.decrypt(line, key);
            list.add(decryptedLine);
        }
    	
		bufferedReader.close();
    			
		bufferedReader = FileUtil.read(path);

		int count = 1;
		
		for(String linet : list) {
        	if(count == 1) {
        		FileUtil.write(linet, path, false);
        	} else {
        		FileUtil.write(linet, path, true);
        	}
        	count++;
		}
		
    	bufferedReader.close();
	}

}
