
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Signature;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;
import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Mhxanismos akeraiothtas. Bhmata: 1-> Kryptografish arxeiou (SHA-3) 2->
 * Kryptografish zeugous username && digest me SHA-3 3-> Kryptografish
 * apotelesmatos vhmatos 2 me idiwtiko kleidi efarmoghs 4-> Kryptografish
 * ypografhs me AES-256
 */
public class Mechanism {

    private File file;
    private Signature sign;
    private MessageDigest digest;
    private ObjectInputStream oin;
    private ObjectOutputStream out;
    private FileOutputStream fout;
    private FileInputStream fin;
    private String result;
    private HashMap<String, byte[]> pairs;

    //Bhma 1
    public void encrypt(String user,String cardfileid, File f, byte[] IV, Key pkey, SecretKey secret) {
        System.out.println("star of encrypr");
        System.out.println("cardfileid: "+cardfileid);
        System.out.println("file f:" + f);
        try {
            fin = new FileInputStream(f);       //Dhmiourgia streams gia diabasma apo to arxeio
            oin = new ObjectInputStream(fin);

            String input = oin.readObject().toString();     //metatroph se string gia hashing
            oin.close();
            fin.close();
            digest = MessageDigest.getInstance("SHA3-256");     //epilogh algorithmou hashing
            byte[] encrypted = digest.digest(input.getBytes(StandardCharsets.UTF_8));       //kryptografhsh periexomenou arxeiou

            File encFile = new File(user + "/" + "encrypted.dat");       //dhmiourgia kainouriou arxeiou me kryptografhmena arxeia

            out = new ObjectOutputStream(new FileOutputStream(encFile));    //eggrafh kryptografhmenwn dedomenwn sto arxeio
            out.writeObject(encrypted);
            out.close();

            //apothikeush zeugous username kai arxeiou se hashmap 
            //Bhma 2
            //gia na hasharw to zeugos tha ta apothikeusw se ena string kai tha to hasharw
            System.out.println("encrypt card file complete");
            String pair = user + Arrays.toString(encrypted);;
            byte[] encstring = digest.digest(pair.getBytes(StandardCharsets.UTF_8));    //hasharisma tou zeugous

            //result = encstring.toString();
            //Bhma 3
            //kryptografw to apotelesma me to idiwtiko kleidi ths efarmoghs
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(ENCRYPT_MODE, pkey);
            byte[] ciphertext = cipher.doFinal(encstring);    //Kryptografhsh tou prohgoumenou apotelesmatos me idiwtiko kleidi ths efarmoghs

            //Apo bhmata 1+2+3 pairnoume thn psifiakh ypografh kai thn kryptografoume me to asymmetro kleidi tou xrhsth
            //Apothikeuw thn psifiakh upografh se hashmap
            //Bhma 4
            //Kryptografhsh me asymetro kleidi
            System.out.println("rsa encrypt complete");

            IvParameterSpec IVSpec = new IvParameterSpec(IV);
            Cipher ascipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getEncoded(), "AES");
            ascipher.init(ENCRYPT_MODE, keySpec, IVSpec);
            byte[] finalcipher = ascipher.doFinal(ciphertext);

            System.out.println("aes encrypt complete");
            File mechFile = new File(user + "/" + cardfileid +"mechFile.dat");
            try ( ObjectOutputStream out9 = new ObjectOutputStream(new FileOutputStream(mechFile))) {
                out9.writeObject(finalcipher);
                out9.flush();
                out9.close();
                System.out.println("to arxeio dimioyrgirhike");

            } catch (FileNotFoundException ex) {
                Logger.getLogger(Mechanism.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Mechanism.class.getName()).log(Level.SEVERE, null, ex);
            }
            //Telos krypotgrafishs

        } catch (IOException | ClassNotFoundException | InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            System.out.println(e);
        }
    }

    /*
    Mhxanismos apokryptografhshs: Bhma 1: Hashing opws prin
                                  Bhma 2: Apokryptografish psifiakhs ypografhs me asymetro kleidi
                                  Bhma 3: Apokryptografish psifiakhs ypografhs me idiwtiko kleidi
                                  Bhma 4: Sygkrish me apotelesma hash apo prin
     */
    public boolean decryption(String user,String cardfileid, File f, byte[] IV, SecretKey skey, Key pkey) {
        boolean flag = false;
        System.out.println("decript start");
        try {
            //Bhma 1: Hashing arxeio, apothikeush se hashmap san zeugos kai meta hashing zeugos
            fin = new FileInputStream(f);
            oin = new ObjectInputStream(fin);

            String input = oin.readObject().toString();
            oin.close();
            fin.close();
            digest = MessageDigest.getInstance("SHA3-256");
            byte[] encrypted = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            String pair = user + Arrays.toString(encrypted);
            byte[] encstring = digest.digest(pair.getBytes(StandardCharsets.UTF_8));
            String a = Arrays.toString(encstring);
            System.out.println("a: "+a);
            // Vriskw thn psifiakh ypografh pou antistoixei ston xrhsth 
            File mechPath = new File(user + "/"+cardfileid + "mechFile.dat");
            byte[] signiture=null;
            try ( ObjectInputStream in1334 = new ObjectInputStream(new FileInputStream(mechPath))) {
                try {
                    signiture = (byte[]) in1334.readObject();

                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Mechanism.class.getName()).log(Level.SEVERE, null, ex);
                }
                in1334.close();
                
                //Bhma 2: Apokryptografhsh me asymetro kleidi
                IvParameterSpec ivSpec = new IvParameterSpec(IV);
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                SecretKeySpec keySpec = new SecretKeySpec(skey.getEncoded(), "AES");
                cipher.init(DECRYPT_MODE, keySpec, ivSpec);
                byte[] decrypted = cipher.doFinal(signiture);
                String dec = Arrays.toString(decrypted);

                //Bhma 3: Apokryptografhsh me symmetriko kleidi
                Cipher scipher = Cipher.getInstance("RSA");
                scipher.init(DECRYPT_MODE, pkey);
                byte[] plainsig = scipher.doFinal(decrypted);
                String b = Arrays.toString(plainsig);
                System.out.println("b: "+b);
                //Sygkrish psifiakhs ypografhs me hashed timh
                if (a.equals(b)) {
                    System.out.println("File Authentication Successful");
                    flag = true;
                } else {
                    System.out.println("File Authentication Failed\nFile has been tampered");
                    flag = false;
                }
            } catch (IOException ex) {
                //Logger.getLogger(Mechanism.class.getName()).log(Level.SEVERE, null, ex);
                 System.out.println(ex);
            }

        } catch (Exception ex) {
            System.out.println(ex);
        }
        return flag;
    }

}
