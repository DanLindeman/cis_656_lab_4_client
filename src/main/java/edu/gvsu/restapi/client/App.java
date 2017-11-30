package edu.gvsu.restapi.client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class App {

    public static void main(String args[]) {
        String myAddress = "localhost";
        try {
            myAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        BufferedReader is = new BufferedReader(new InputStreamReader(System.in));
        String username = getUsername(is);
        SampleRESTClient client = new SampleRESTClient();
        Integer listenPort = getListenPort(is);
        RegistrationInfo regInfo = new RegistrationInfo(username, myAddress, listenPort, true);
        Thread listenerThread = new Thread(new ClientListener(listenPort));
        listenerThread.start();


        try {
            RegistrationInfo extantUser = client.lookup(username);
            if (extantUser == null) {
                client.register(regInfo);
            } else {
                System.out.println("Server already has a user by that name");
                System.exit(1);
            }


            Boolean running = true;
            while (running) {
                printGreeting();
                String input = is.readLine();
                String[] inputTokens = input.split(" ");
                if (inputTokens.length < 1) {
                    inputTokens = new String[1];
                    inputTokens[0] = "";
                }
                switch (inputTokens[0]) {
                    case "friends":
                        RegistrationInfo[] users = client.listRegisteredUsers();
                        for (RegistrationInfo user : users) {
                            System.out.println(user.getUserName() + " -- online: " + user.getStatus() + "\n");
                        }
                        break;
                    case "talk":
                        if (inputTokens.length < 3) {
                            System.out.println("Must have a username and a message");
                            break;
                        }
                        String talkTarget = inputTokens[1];
                        String msg = buildMessage(inputTokens);
                        sendMessageToUser(username, msg, client, talkTarget);
                        break;
                    case "broadcast":
                        System.out.println("broadcast");
                        RegistrationInfo[] allUsers = client.listRegisteredUsers();
                        if (inputTokens.length < 2) {
                            System.out.println("Broadcast must have a message");
                            break;
                        }
                        String bcastMessage = buildBroadcastMessage(inputTokens);
                        for (RegistrationInfo aUser : allUsers) {
                            if (!aUser.getUserName().equals(username)) {
                                sendMessageToUser(username, " (broadcast) " + bcastMessage, client, aUser.getUserName());
                            }
                        }
                        break;
                    case "busy":
                        RegistrationInfo myBusyInfo = client.lookup(username);
                        if (myBusyInfo != null) {
                            regInfo.setStatus(false);
                            client.setStatus(username, false);
                            System.out.println("Set user <" + username + "> to 'busy'\n");
                        }
                        break;
                    case "available":
                        RegistrationInfo myAvailableInfo = client.lookup(username);
                        if (myAvailableInfo != null) {
                            regInfo.setStatus(true);
                            client.setStatus(username, true);
                            System.out.println("Set user <" + username + "> to 'available'\n");
                        }
                        break;
                    case "exit":
                        System.out.println("See ya!");
                        client.unregister(username);
                        System.exit(0);
                    default:
                        System.out.println("Sorry, didn't understand that\n");
                }
            }
        } catch (Exception e) {
            System.err.println("Client exception:");
            e.printStackTrace();
        }
    }

    private static String getUsername(BufferedReader is) {
        try {
            System.out.println("What is your name?");
            String input = is.readLine();
            return input;
        } catch (IOException e) {
            System.out.println("Didn't catch that, is that even a name?");
            return null;
        }
    }

    private static Integer getListenPort(BufferedReader is) {
        try {
            System.out.println("What port will you receive messages on?");
            String input = is.readLine();
            return Integer.parseInt(input);
        } catch (IOException e) {
            System.out.println("Didn't catch that, defaulting to listen port 9999");
            return 9999;
        }
    }

    private static void sendMessageToUser(String username, String trimmedMessage, SampleRESTClient client, String talkTarget) {
        try {
            RegistrationInfo talkTargetInfo = client.lookup(talkTarget);
            if (talkTargetInfo != null) {
                Boolean talkTargetStatus = talkTargetInfo.getStatus();
                if (talkTargetStatus) {
                    String talkTargetHost = talkTargetInfo.getHost();
                    int talkTargetPort = talkTargetInfo.getPort();
                    Socket clientSocket = new Socket(talkTargetHost, talkTargetPort);
                    DataOutputStream os = new DataOutputStream(clientSocket.getOutputStream());
                    os.writeBytes(username + " : " + trimmedMessage);
                    os.close();
                    clientSocket.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String buildBroadcastMessage(String[] inputTokens) {
        String message = "";
        for (int i = 1; i < inputTokens.length; i++) {
            message += inputTokens[i] + " ";
        }
        String trimmedMessage = message.trim();
        return trimmedMessage;
    }

    private static String buildMessage(String[] inputTokens) {
        String message = "";
        for (int i = 2; i < inputTokens.length; i++) {
            message += inputTokens[i] + " ";
        }
        String trimmedMessage = message.trim();
        return trimmedMessage;
    }


    private static void printGreeting() {
        System.out.println("What would you like to do?");
        System.out.println("friends - list all available users");
        System.out.println("talk {username} {message} - send a message to user");
        System.out.println("broadcast {message} - send a message to all users");
        System.out.println("busy - set your status to 'busy'");
        System.out.println("available - set your status to 'available'");
        System.out.println("exit - exit the chat\n\n");
    }

}


//  public static void main(String[] args) {
//        SampleRESTClient client = new SampleRESTClient();
//        try {
//            RegistrationInfo reg = new RegistrationInfo();
//            reg.setHost("5.5.5.5");
//            reg.setUserName("Barry");
//
//            try {
//                System.out.println("Looking up a user that doesn't exist");
//                client.lookup("Barry");
//                System.out.println("\n");
//            } catch (Exception e) {
//
//            }
//
//            System.out.println("Registering Barry");
//            client.register(reg);
//            System.out.println("\n");
//
//            System.out.println("Looking up a user that exists");
//            client.lookup("Barry");
//            System.out.println("\n");
//
//            System.out.println("Setting Barrys status to false");
//            client.setStatus("Barry", false);
//            System.out.println("\n");
//
//            System.out.println("Checking that Barrys status is false");
//            client.lookup("Barry");
//            System.out.println("\n");
//
//            System.out.println("Setting Barrys status to true");
//            client.setStatus("Barry", true);
//            System.out.println("\n");
//
//            System.out.println("Checking that Barrys status is true");
//            client.lookup("Barry");
//            System.out.println("\n");
//
//            System.out.println("List all users");
//            client.listRegisteredUsers();
//            System.out.println("\n");
//
//            System.out.println("Unregister Barry");
//            client.unregister("Barry");
//            System.out.println("\n");
//
//            System.out.println("List all users after unregistering Barry");
//            client.listRegisteredUsers();
//            System.out.println("\n");
//
//        } catch (
//                Exception e)
//
//        {
//            e.printStackTrace();
//        }
//    }
//}
