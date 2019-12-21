/*package g53sqm.chat.server;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class ServerTest {

    private Server test_server;
    private int test_port_no;
    private Thread test_server_thread;

    public class test_runnable implements Runnable{


        public void run() {

            System.out.println("Test server thread started");
            test_server.listen();
        }
    }
    private Socket createMockUsers(String username, int portNo){
        Socket user = null;
        try{
            user = new Socket("localhost",test_port_no);
            userEnterCommandAndText(user, "IDEN " + username);
        }catch (IOException e) {
            Assert.fail("Mock user setup failed");
        }
        return user;
    }

    private void userEnterCommandAndText(Socket user, String text){
        try{
            PrintWriter user_out = new PrintWriter(user.getOutputStream(), true);
            user_out.println(text);
            Thread.sleep(1000);
        }catch(IOException |InterruptedException ie) {
            Assert.fail("Failed to send command and text");
        }
    }

    private String userReceiveMessage(Socket user){
    	
        String text = "";
        try{
        	BufferedReader sInput  = new BufferedReader(new InputStreamReader(user.getInputStream()));
        	text=sInput.readLine();
        }catch(IOException e) {
            e.printStackTrace();
        }

        return text;
    
    }

    // Initialise server
    @Before
    public void initialiseServer(){

        test_server = new Server(0);
        test_port_no = test_server.getPortNo();
        Runnable runnable = new test_runnable();
        test_server_thread = new Thread(runnable);

        // Set thread as Daemon to automatically terminate when JVM terminates
        test_server_thread.setDaemon(true);
        test_server_thread.start();


        // Let it sleep for 1 second to ensure thread executed
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    

    @Test
    public void getUserList_noUsersOnline_returnEmptyList(){
        ArrayList<String> actual_users_online = test_server.getUserList();
        String[] expected_users_online = new String[]{};
        assertArrayEquals(actual_users_online.toArray(),expected_users_online);
    }

    @Test
    public void getUserList_multipleUsersOnline_returnCorrectUsernameList(){
        //Create mock clients
        Socket client1 = createMockUsers("client1",test_port_no);
        Socket client2 = createMockUsers("client2",test_port_no);

        ArrayList<String> actual_users_online = test_server.getUserList();
        String[] expected_users_online = new String[2];
        expected_users_online[0] = "client1";
        expected_users_online[1] = "client2";

        assertEquals(expected_users_online[0],actual_users_online.get(0));
        assertEquals(expected_users_online[1],actual_users_online.get(1));
        assertArrayEquals(expected_users_online,actual_users_online.toArray());

    }

    @Test
    public void getUserList_usersOnlineQuit_returnUsernameListWithoutQuit() {
        //Create mock clients
        Socket client1 = createMockUsers("client1",test_port_no);
        Socket client2 = createMockUsers("client2",test_port_no);

        //client1 quits
        userEnterCommandAndText(client1, "QUIT");
        ArrayList<String> actual_users_online = test_server.getUserList();
        String[] expected_users_online = new String[1];
        expected_users_online[0] = "client2";
        assertEquals(expected_users_online[0],actual_users_online.get(0));
        assertArrayEquals(expected_users_online,actual_users_online.toArray());
    }

    @Test
    public void doesUserExist_userOnline_returnTrue(){
        Socket client1 = createMockUsers("existing_client1",test_port_no);

        boolean userFound = test_server.doesUserExist("existing_client1");
        assertTrue(userFound);
    }

    @Test
    public void doesUserExist_multipleUserOnline_returnTrue(){
        Socket client1 = createMockUsers("existing_client1",test_port_no);
        Socket client2 = createMockUsers("existing_client2",test_port_no);

        boolean userFound = test_server.doesUserExist("existing_client1");
        assertTrue(userFound);
        boolean user2Found = test_server.doesUserExist("existing_client2");
        assertTrue(user2Found);
    }

    @Test
    public void doesUserExist_multipleUserOnlineThenQuit_returnCorrectExist(){
        Socket client1 = createMockUsers("existing_client1",test_port_no);
        Socket client2 = createMockUsers("existing_client2",test_port_no);

        boolean userFound = test_server.doesUserExist("existing_client1");
        assertTrue(userFound);
        boolean user2Found = test_server.doesUserExist("existing_client2");
        assertTrue(user2Found);

        userEnterCommandAndText(client1, "QUIT");
        userFound = test_server.doesUserExist("existing_client1");
        assertFalse(userFound);
        user2Found = test_server.doesUserExist("existing_client2");
        assertTrue(user2Found);

        userEnterCommandAndText(client2, "QUIT");
        userFound = test_server.doesUserExist("existing_client1");
        assertFalse(userFound);
        user2Found = test_server.doesUserExist("existing_client2");
        assertFalse(user2Found);
    }
    
    @Test
    public void should_send_broadcast(){
    	Socket client1 = createMockUsers("existing_client1",test_port_no);
    	
        Socket client2 = createMockUsers("existing_client2",test_port_no);
        userReceiveMessage(client2);
        
        Socket client3 = createMockUsers("existing_client2",test_port_no);
        userReceiveMessage(client3);

        
        userEnterCommandAndText(client1, "HAIL hi");

        String reply1 = userReceiveMessage(client2);
        String reply2 = userReceiveMessage(client3);


        assertEquals("Broadcast from existing_client1: "+"hi",reply1);
        assertEquals("Broadcast from existing_client1: "+"hi",reply2);

        
    }
    
    @Test
    public void should_send_list(){
    	Socket client1 = createMockUsers("existing_client1",test_port_no);
    	userReceiveMessage(client1);
        
        Socket client2 = createMockUsers("existing_client2",test_port_no);
       
        Socket client3 = createMockUsers("existing_client3",test_port_no);

        
        userEnterCommandAndText(client1, "LIST");

        String reply1 = userReceiveMessage(client1);
      
        ArrayList<String> actual_users_online = test_server.getUserList();      
        

        String namelist=(actual_users_online.toString().substring(1, actual_users_online.toString().length() - 1));
        String s = "Users online: "+namelist+", " ;

        assertEquals(s,reply1);
       

        
    }
    
    @Test
    public void should_send_BAD_command(){
        Socket client1 = createMockUsers("existing_client1",test_port_no);
        userReceiveMessage(client1);
        userEnterCommandAndText(client1, "JUNK TEZXT");
        String reply1 = userReceiveMessage(client1);

        assertEquals("BAD command not recognised", reply1);
    }
    
    
    @Test
    public void should_send_STAT_single_user(){
        int msgCount=0;
        Socket client1 = createMockUsers("existing_client1",test_port_no);
        userReceiveMessage(client1);   
        
        userEnterCommandAndText(client1, "STAT");
        String actualReply = userReceiveMessage(client1);
        
        
        ArrayList<String> actual_users_online = test_server.getUserList();  
        String expected = "OK There are currently "+actual_users_online.size()+" user(s) on the server You are logged in and have sent "+msgCount+ " message(s)";
        

        assertEquals(expected, actualReply);
    }
    
    
    @Test
    public void should_send_STAT_with_multiuser(){
        int msgCount=0;
        Socket client1 = createMockUsers("existing_client1",test_port_no);
        userReceiveMessage(client1);
        
        Socket client2 = createMockUsers("existing_client2",test_port_no);


        
        userEnterCommandAndText(client1, "STAT");
        String actualReply = userReceiveMessage(client1);
        
        
        ArrayList<String> actual_users_online = test_server.getUserList();  
        String expected = "OK There are currently "+actual_users_online.size()+" user(s) on the server You are logged in and have sent "+msgCount+ " message(s)";
        System.out.println(expected);

        System.out.println(actualReply);
        

        assertEquals(expected, actualReply);
    }
    
    

    @Test
    public void should_send_STAT_msg_sent(){
        int msgCount=0;
        Socket client1 = createMockUsers("existing_client1",test_port_no);
        userReceiveMessage(client1);   
        
        
        userEnterCommandAndText(client1, "HAIL hi");
        msgCount++;
        userReceiveMessage(client1);

        
        userEnterCommandAndText(client1, "HAIL hi");
        msgCount++;
        userReceiveMessage(client1);
        
        
        userEnterCommandAndText(client1, "STAT");
        String actualReply = userReceiveMessage(client1);
        
        
        ArrayList<String> actual_users_online = test_server.getUserList();  
        String expected = "OK There are currently "+actual_users_online.size()+" user(s) on the server You are logged in and have sent "+msgCount+ " message(s)";
        

        assertEquals(expected, actualReply);
    }
    
    
    @Test
    public void should_user_register(){
        Socket user = null;
        String username = "User_1";
        try{
            user = new Socket("localhost",test_port_no);
            userReceiveMessage(user);
            userReceiveMessage(user);

            userEnterCommandAndText(user, "IDEN " + username);
        }catch (IOException e) {

        }
        String reply = userReceiveMessage(user);
        String expected = "OK Welcome to the chat server "+ username;
        System.out.println(reply);
        System.out.println(expected);

        assertEquals(expected,reply);
    }
    
    @Test
    public void should_send_private_message_receive(){
        Socket client1 = createMockUsers("existing_client1",test_port_no);
        Socket client2 = createMockUsers("existing_client2",test_port_no);
        
        userReceiveMessage(client2);


        userEnterCommandAndText(client1, "MESG existing_client2 hi");

        String reply1 = userReceiveMessage(client2);
        String expected = "PM from existing_client1:hi";

        assertEquals(expected, reply1);
    }

    @Test
    public void should_send_private_message_response(){
        Socket client1 = createMockUsers("existing_client1",test_port_no);
        Socket client2 = createMockUsers("existing_client2",test_port_no);
        
        userReceiveMessage(client1);


        userEnterCommandAndText(client1, "MESG existing_client2 hi");

        String reply1 = userReceiveMessage(client1);
        String expected = "OK your message has been sent";

        assertEquals(expected, reply1);
    }
    
    

    

    
    @Test
    public void should_send_QUIT(){
    	int messageCount=0;
        Socket client1 = createMockUsers("existing_client1",test_port_no);
        
        userReceiveMessage(client1);


        userEnterCommandAndText(client1, "QUIT");

        String reply1 = userReceiveMessage(client1);
        String expected = "OK thank you for sending " + messageCount + " message(s) with the chat service, goodbye. ";

        assertEquals(expected, reply1);
    }
    
    
    @Test
    public void should_send_QUIT_with_msg_sent(){
    	int messageCount=0;
        Socket client1 = createMockUsers("existing_client1",test_port_no);
        
        userReceiveMessage(client1);
        
        userEnterCommandAndText(client1, "HAIL hi");
        messageCount++;
        userReceiveMessage(client1);


        userEnterCommandAndText(client1, "QUIT");

        String reply = userReceiveMessage(client1);
        String expected = "OK thank you for sending " + messageCount + " message(s) with the chat service, goodbye. ";
        System.out.println(reply);
        System.out.println(expected);

        assertEquals(expected, reply);
    }
    
    

    
    
    @Test
    public void should_send_private_without_login(){
        Socket client1 = createMockUsers("existing_client1",test_port_no);
        Socket user = null;
        try{
            user = new Socket("localhost",test_port_no);
            userReceiveMessage(user);
            userReceiveMessage(user);

        }catch (IOException e) {

        }
       


        
        userEnterCommandAndText(user, "MESG existing_client1 hi");

        String reply = userReceiveMessage(user);
        String expected = "BAD You have not logged in yet";
        System.out.println(reply);
        System.out.println(expected);

        assertEquals(expected, reply);
    }
    
   
    
    @Test
    public void should_STAT_without_login(){
         Socket user = null;
    

         try{
             user = new Socket("localhost",test_port_no);
             userReceiveMessage(user);
             userReceiveMessage(user);




         }catch (IOException e) {

         }

         
         userEnterCommandAndText(user, "STAT");
         ArrayList<String> actual_users_online = test_server.getUserList();  
         int size=actual_users_online.size();
         int actualSize=size+1;

         
         String previousString = "OK There are currently "+actualSize+" user(s) on the server ";
         String reply = userReceiveMessage(user);
         String expected = previousString+"You have not logged in yet";
         System.out.println(reply);
         System.out.println(expected);

         assertEquals(expected, reply);
    
    
    
} 
    
    
    
    @Test
    public void should_LIST_without_login(){
         Socket user = null;
        

         try{
             user = new Socket("localhost",test_port_no);
             userReceiveMessage(user);
             userReceiveMessage(user);




         }catch (IOException e) {

         }

         
         userEnterCommandAndText(user, "LIST");

         
         String reply = userReceiveMessage(user);
         String expected = "BAD You have not logged in yet";
         System.out.println(reply);
         System.out.println(expected);

         assertEquals(expected, reply);
    
    
    
}
    
    @Test
    public void should_user_QUIT_without_login(){
        Socket user = null;
        try{
            user = new Socket("localhost",test_port_no);
            userReceiveMessage(user);
            userReceiveMessage(user);

        }catch (IOException e) {

        }
        userEnterCommandAndText(user, "QUIT");

        String reply = userReceiveMessage(user);
        String expected = "OK goodbye";
        System.out.println(reply);
        System.out.println(expected);

        assertEquals(expected,reply);
    }
    
  
    
   
    
    @Test
    public void should_send_command_less_than_4_char(){
        Socket client1 = createMockUsers("existing_client1",test_port_no);
        userReceiveMessage(client1);
        userEnterCommandAndText(client1, "oof");
        String reply = userReceiveMessage(client1);
        String expected = "BAD invalid command to server";
        System.out.println(reply);
        assertEquals(expected, reply);
    }
   
    
    @Test
    public void should_user_register_twice(){
        Socket user1 = null;
        String username = "User";
        String username2 = "User2";

        try{
            user1 = new Socket("localhost",test_port_no);
            userReceiveMessage(user1);
            userReceiveMessage(user1);
            userEnterCommandAndText(user1, "IDEN " + username);
            userReceiveMessage(user1);

           

            userEnterCommandAndText(user1, "IDEN " + username2);
        }catch (IOException e) {

        }
        String reply = userReceiveMessage(user1);
        String expected = "BAD you are already registerd with username "+username;
        
        System.out.println(reply);
        System.out.println(expected);

        assertEquals(expected,reply);
    }
    @Test
    public void upon_initialise(){
        Socket user1 = null;


        try{
            user1 = new Socket("localhost",test_port_no);
            userReceiveMessage(user1);
        
        }catch (IOException e) {

        }
        String reply = userReceiveMessage(user1);
        ArrayList<String> actual_users_online = test_server.getUserList();  
        int size=actual_users_online.size();
        int actualSize=size+1;
        String expected = "OK Welcome to the chat server, there are currelty "+ actualSize +" user(s) online";
        
        System.out.println(reply);
        System.out.println(expected);

        assertEquals(expected,reply);
    }

    @Test
    public void should_user_register_same_username(){
        Socket user1 = null;
        String username = "User";
        Socket user2 = null;
        try{
            user1 = new Socket("localhost",test_port_no);
            userEnterCommandAndText(user1, "IDEN " + username);

            user2 = new Socket("localhost",test_port_no);
            userReceiveMessage(user2);
            userReceiveMessage(user2);

            userEnterCommandAndText(user2, "IDEN " + username);
        }catch (IOException e) {

        }
        String reply = userReceiveMessage(user2);
        String expected = "BAD username is already taken";
        
        System.out.println(reply);
        System.out.println(expected);

        assertEquals(expected,reply);
    }
   
   
    @Test
    public void should_send_private_message_nonuser(){
        Socket client1 = createMockUsers("existing_client1",test_port_no);
        Socket client2 = createMockUsers("existing_client2",test_port_no);
        
        userReceiveMessage(client1);


        userEnterCommandAndText(client1, "MESG existing_client2hi");

        String reply1 = userReceiveMessage(client1);
        String expected = "BAD Your message is badly formatted";

        assertEquals(expected, reply1);
    }
     
    
    
    @Test
    public void should_getNumOfUsers(){
        Socket client1 = createMockUsers("existing_client1",test_port_no);
        Socket client2 = createMockUsers("existing_client2",test_port_no);

        
        
        assertEquals(2,test_server.getNumberOfUsers());
    }
    


    
    
}
*/