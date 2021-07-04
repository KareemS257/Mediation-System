/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package generationnode;
import com.jcraft.jsch.*;
import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.util.logging.Level;

/**
 *
 * @author anton
 */
public class GNSCPClient {
    private static Session session;
    public static int connectToServer(String hostName,int port, String userName, String password) {
        try {
            JSch jsch2=new JSch();
            session=jsch2.getSession(userName, hostName, port);
            
            // username and password will be given via UserInfo interface.
            UserInfo ui=new MyUserInfo(password);
            session.setUserInfo(ui);
            session.connect();
            return 1;
        } catch (JSchException ex) {
            java.util.logging.Logger.getLogger(GNSCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }
    
    /*public static void disconnectFromServer(){
        session.disconnect();
    }*/
      static int checkAck(InputStream in) throws IOException{
    int b=in.read();
    // b may be 0 for success,
    //          1 for error,
    //          2 for fatal error,
    //          -1
    if(b==0) return b;
    if(b==-1) return b;

    if(b==1 || b==2){
      StringBuffer sb=new StringBuffer();
      int c;
      do {
	c=in.read();
	sb.append((char)c);
      }
      while(c!='\n');
      if(b==1){ // error
	System.out.print(sb.toString());
      }
      if(b==2){ // fatal error
	System.out.print(sb.toString());
      }
    }
    return b;
  }
  public static int uploadFile(String lfile,String rfile){
  FileInputStream fis=null;
    try{  
              boolean ptimestamp = true;//preserve timestamp?? never false in the whole code
    rfile=rfile.replace("'", "'\"'\"'"); //replace ' with "'"'
    rfile="'"+rfile+"'";
    String command="scp " + (ptimestamp ? "-p" :"") +" -t "+rfile; //if ptimestamp is true add -p option and -t rfile to scp
    Channel channel=session.openChannel("exec");//a session can contain multiple channels. channel with value exec is channel connected to a remotely executing program
    ((ChannelExec)channel).setCommand(command);

    // get I/O streams for remote scp
    OutputStream out=channel.getOutputStream();
    InputStream in=channel.getInputStream();

    channel.connect();//make ssh connection

    if(checkAck(in)!=0){
//System.exit(0);
    }

    File _lfile = new File(lfile);

    if(ptimestamp){
      command="T"+(_lfile.lastModified()/1000)+" 0";
      // The access time should be sent here,
      // but it is not accessible with JavaAPI ;-<
      command+=(" "+(_lfile.lastModified()/1000)+" 0\n"); 
      out.write(command.getBytes()); out.flush();
      if(checkAck(in)!=0){
//    System.exit(0);
      }
    }

    // send "C0644 filesize filename", where filename should not include '/'
    long filesize=_lfile.length();
    command="C0644 "+filesize+" ";
    if(lfile.lastIndexOf('/')>0){
      command+=lfile.substring(lfile.lastIndexOf('/')+1);
    }
    else{
      command+=lfile;
    }
    command+="\n";
    out.write(command.getBytes()); out.flush();
    if(checkAck(in)!=0){
//System.exit(0);
    }

    // send a content of lfile
    fis=new FileInputStream(lfile);
    byte[] buf=new byte[1024];
    while(true){
      int len=fis.read(buf, 0, buf.length);
if(len<=0) break;
      out.write(buf, 0, len); //out.flush();
    }
    fis.close();
    fis=null;
    // send '\0'
    buf[0]=0; out.write(buf, 0, 1); out.flush();
    if(checkAck(in)!=0){
//System.exit(0);
    }
    out.close();

    channel.disconnect();
    session.disconnect();

//    System.exit(0);
    return 1;
  }
  catch(Exception e){
    System.out.println(e);
    try{if(fis!=null)fis.close();}catch(Exception ee){}
    return 0;
  }

  }
    public static int downloadFile(String lfile,String rfile){
        String prefix=null;
        if(new File(lfile).isDirectory()){
            prefix=lfile+File.separator;
        }
        FileOutputStream fos=null;
        try{ 
              rfile=rfile.replace("'", "'\"'\"'");
      rfile="'"+rfile+"'";
      String command="scp -f "+rfile;
      Channel channel=session.openChannel("exec");
      ((ChannelExec)channel).setCommand(command);

      // get I/O streams for remote scp
      OutputStream out=channel.getOutputStream();
      InputStream in=channel.getInputStream();

      channel.connect();

      byte[] buf=new byte[1024];

      // send '\0'
      buf[0]=0; out.write(buf, 0, 1); out.flush();

      while(true){
	int c=checkAck(in);
        if(c!='C'){
	  break;
	}

        // read '0644 '
        in.read(buf, 0, 5);

        long filesize=0L;
        while(true){
          if(in.read(buf, 0, 1)<0){
            // error
            break; 
          }
          if(buf[0]==' ')break;
          filesize=filesize*10L+(long)(buf[0]-'0');
        }

        String file=null;
        for(int i=0;;i++){
          in.read(buf, i, 1);
          if(buf[i]==(byte)0x0a){
            file=new String(buf, 0, i);
            break;
  	  }
        }

	//System.out.println("filesize="+filesize+", file="+file);

        // send '\0'
        buf[0]=0; out.write(buf, 0, 1); out.flush();

        // read a content of lfile
        fos=new FileOutputStream(prefix==null ? lfile : prefix+file);
        int foo;
        while(true){
          if(buf.length<filesize) foo=buf.length;
	  else foo=(int)filesize;
          foo=in.read(buf, 0, foo);
          if(foo<0){
            // error 
            break;
          }
          fos.write(buf, 0, foo);
          filesize-=foo;
          if(filesize==0L) break;
        }
        fos.close();
        fos=null;

	if(checkAck(in)!=0){
	  System.exit(0);
	}

        // send '\0'
        buf[0]=0; out.write(buf, 0, 1); out.flush();
      }

      session.disconnect();

//      System.exit(0);
      return 1;
    }
    catch(Exception e){
      System.out.println(e);
      try{if(fos!=null)fos.close();}catch(Exception ee){}finally{ return 0;}
      
    }

        
        
    }
    
    
    public static class MyUserInfo implements UserInfo, UIKeyboardInteractive{
    String passwd;
    public MyUserInfo(String password){
    passwd=password;
    }
    public String getPassword(){ return passwd; }
    public boolean promptYesNo(String str){
      return true;
    }
    public String getPassphrase(){ return null; }
    public boolean promptPassphrase(String message){ return true; }
    public void showMessage(String message){
      //JOptionPane.showMessageDialog(null, message);
  }
  public boolean promptPassword(String message){
      return true;
    }
     public String[] promptKeyboardInteractive(String destination,
                                              String name,
                                              String instruction,
                                              String[] prompt,
                                              boolean[] echo){
                                                return null;
    }
  }
    
    
}
