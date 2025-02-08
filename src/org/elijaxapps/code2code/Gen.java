package org.elijaxapps.code2code;
public class Gen {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		for(Integer i = 0; i < 256; i++) {			
			System.out.println("#^x"+i+" 0h0001"+(i < 16 ?"0"+Integer.toHexString(i) : Integer.toHexString(i)) + " " + Integer.toHexString(i));
		}
		
		for(Integer i = 0; i < 256; i++) {			
			System.out.println("#^b"+Integer.toBinaryString(i)+" 0h0002"+(i < 16 ?"0"+Integer.toHexString(i) : Integer.toHexString(i)) + " " + Integer.toHexString(i));
		}
		
		for(Integer i = 0; i < 256; i++) {			
			System.out.println("#"+Integer.toOctalString(i)+" 0h0003"+(i < 16 ?"0"+Integer.toHexString(i) : Integer.toHexString(i)) + " " + Integer.toHexString(i));
		}
	}

}
