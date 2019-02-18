import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.util.Scanner;

public class Serveur {

    public static void main(String[] test) throws NoSuchAlgorithmException {

        final ServerSocket serveurSocket  ;
        final Socket clientSocket ;
        final BufferedReader in;
        final InputStream myIn;
        final OutputStream myOut;
        final PrintWriter Out;
        final Scanner sc=new Scanner(System.in);
        final KeyPair keyPair = GenerateurKey.generateKeyPair();
        final PrivateKey clePrivee = keyPair.getPrivate();
        final PublicKey clePublique = keyPair.getPublic();
        final Key publicKeyRecue;


        try {
            serveurSocket = new ServerSocket(1234);
            clientSocket = serveurSocket.accept();

            //Out = new PrintWriter(clientSocket.getOutputStream());
            //in = new BufferedReader (new InputStreamReader (clientSocket.getInputStream()));
            myIn = new ObjectInputStream(clientSocket.getInputStream());
            myOut = new ObjectOutputStream(clientSocket.getOutputStream());
            myOut.write(clePublique.getEncoded());
            //myOut.println(clePublique);
            myOut.flush();
            byte[]keybyte = in.readLine().getBytes();
            publicKeyRecue = new SecretKeySpec(keybyte, "RSA");
            Thread envoi= new Thread(new Runnable() {
                String msg;
                @Override
                public void run() {
                    while(true){
                        msg = sc.nextLine();
                        byte[] msgEnByte = msg.getBytes();
                        try {
                            byte[] msgEncode = Codage.encodeMessage(publicKeyRecue, msgEnByte);

                            myOut.println(msgEncode);
                            myOut.flush();
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
            envoi.start();

            Thread recevoir= new Thread(new Runnable() {
                String msg ;
                @Override
                public void run() {
                    try {
                        msg = in.readLine();
                        //tant que le client est connecté
                        while(msg!=null){
                            byte[]msgRecueEncode = msg.getBytes();
                            byte[]msgRecueDecodede = Decodage.decodeMessage(clePrivee, msgRecueEncode);
                            System.out.println("Client : "+msgRecueDecodede);
                            msg = in.readLine();
                        }
                        //sortir de la boucle si le client a déconecté
                        System.out.println("Client déconecté");
                        //fermer le flux et la session socket
                        myOut.close();
                        clientSocket.close();
                        serveurSocket.close();
                    } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                        e.printStackTrace();
                    }
                }
            });
            recevoir.start();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}