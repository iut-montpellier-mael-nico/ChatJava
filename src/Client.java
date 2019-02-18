import sun.jvm.hotspot.utilities.ObjectReader;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws NoSuchAlgorithmException, ClassNotFoundException {

        final Socket clientSocket;
        final BufferedReader in;
        final ObjectInputStream myIn;
        final ObjectOutputStream myOut;
        final PrintWriter out;
        final Scanner sc = new Scanner(System.in);//pour lire à partir du clavier
        final KeyPair keyPair = GenerateurKey.generateKeyPair();
        final PrivateKey clePrivee = keyPair.getPrivate();
        final PublicKey clePublique = keyPair.getPublic();
        final Key publicKeyRecue;

        try {
            /*
             * les informations du serveur ( port et adresse IP ou nom d'hote
             * 127.0.0.1 est l'adresse local de la machine
             */
            clientSocket = new Socket("127.0.0.1",1234);

            //flux pour envoyer
            //out = new PrintWriter(clientSocket.getOutputStream());
            myOut = new ObjectOutputStream(clientSocket.getOutputStream());
            //myOut.write();
            //flux pour recevoir
            //in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            myIn = new ObjectInputStream(clientSocket.getInputStream());
            //inkey = new ObjectInputStream(new InputStreamReader(clientSocket.getInputStream()));
            byte[]keybyte = myIn.readObject().toString().getBytes();
            publicKeyRecue = new SecretKeySpec(keybyte, "RSA");

            //out.println(clePublique);
            out.flush();

            Thread envoyer = new Thread(new Runnable() {
                String msg;
                @Override
                public void run() {
                    while(true){
                        msg = sc.nextLine();
                        byte[] msgEnByte = msg.getBytes();
                        try {
                            byte[] msgEncode = Codage.encodeMessage(publicKeyRecue, msgEnByte);
                            out.println(msg);
                            out.flush();
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (NoSuchPaddingException e) {
                            e.printStackTrace();
                        } catch (InvalidKeyException e) {
                            e.printStackTrace();
                        } catch (IllegalBlockSizeException e) {
                            e.printStackTrace();
                        } catch (BadPaddingException e) {
                            e.printStackTrace();
                        }


                    }
                }
            });
            envoyer.start();

            Thread recevoir = new Thread(new Runnable() {
                String msg;
                @Override
                public void run() {
                    try {
                        msg = in.readLine();
                        while(msg!=null){
                            byte[]msgRecueEncode = msg.getBytes();
                            byte[]msgRecueDecodede = Decodage.decodeMessage(clePrivee, msgRecueEncode);
                            System.out.println("Serveur : "+msgRecueDecodede);
                            msg = in.readLine();
                        }
                        System.out.println("Serveur déconecté");
                        out.close();
                        clientSocket.close();
                    } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                        e.printStackTrace();
                    }
                }
            });
            recevoir.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
