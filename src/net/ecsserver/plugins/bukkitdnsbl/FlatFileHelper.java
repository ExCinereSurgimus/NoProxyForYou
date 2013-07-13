package net.ecsserver.plugins.bukkitdnsbl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FlatFileHelper 
{
	/**
	 * Loads an array of integers from a flat-file.
	 * 
	 * int[7] (Standard Array using this helper class)
	 * [0] - playersKicked
	 * [1] - droneblKicks
	 * [2] - proxyblKicks
	 * [3] - spamhausKicks
	 * [4] - sectoorKicks
	 * [5] - sorbsKicks
	 * [6] - tornevall
	 * [7] - unknownKicks
	 * @param File file
	 * @return
	 * @throws IOException 
	 */
	public static int[] getFlatFile(File file) throws IOException
	{
		List<Integer> listInts = new ArrayList<Integer>();
		
		FileInputStream fis = null;
        BufferedInputStream bis = null;
        DataInputStream dis = null;
		
		//Initialize dis bitch
		fis = new FileInputStream(file);
        bis = new BufferedInputStream(fis);
        dis = new DataInputStream(bis);
        
        int integer = -1;
        while((integer = dis.readInt()) != -1)
        {
        	listInts.add(new Integer(integer));
        }
        
        int[] integers = new int[listInts.size()];
        for(int x = 0; x < integers.length; x++)
        {
        	integers[x] = listInts.get(x);
        }
        
        dis.close();
        bis.close();
        fis.close();
		
		return integers;
	}
	
	/**
	 * Saves an array of integers to a flat-file.
	 * 
	 * int[7] (Standard Array using this helper class)
	 * [0] - playersKicked
	 * [1] - droneblKicks
	 * [2] - proxyblKicks
	 * [3] - spamhausKicks
	 * [4] - sectoorKicks
	 * [5] - sorbsKicks
	 * [6] - tornevall
	 * [7] - unknownKicks
	 * @param integers
	 * @return
	 */
	public static boolean saveFlatFile(File file, int[] integers)
	{
		if(!file.exists())
		{
			file.mkdirs(); //makeDURRRSS
			try {
				file.createNewFile();
			} catch (IOException e) {
				BukkitDNSBL.log.info(BukkitDNSBL.logPrefix + " Couldn't create metrics database...");
			}
		}
		FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        DataOutputStream dos = null;
        try {
          fos = new FileOutputStream(file);
          bos = new BufferedOutputStream(fos);
          dos = new DataOutputStream(bos);
          for(int x = 0; x < integers.length; x++)
          {
        	  dos.writeInt(integers[x]);
          }
          dos.flush();
          bos.flush();
          fos.flush();
          dos.close();
          bos.close();
          fos.close();
         }
         catch(Exception e) {
           System.out.println(e);
           return false;
        }      
		return true;
	}
}
