package com.fgtit.fpcore;

import com.fgtit.data.Conversions;

/**
 * The class for fingerprint matching
 */
public class FPMatch {

	private static FPMatch mMatch=null;

	public static FPMatch getInstance(){
		if(mMatch==null){
			mMatch=new FPMatch();
		}
		return mMatch;
	}

	public native int InitMatch();

	/**
	 * Method of initializing the match
	 * @param inittype Type of initialization, set for 1 as default.
	 * @param initcode The website of company set as "https://www.hfteco.com/"
	 * @return 0 for initialize ok, else for failed.
	 */
	public native int InitMatch(int inittype, String initcode);
	/**
	 * Method of matching the two template
	 * the two template should be in private standard in byte array
	 * @param piFeatureA Template 1 with length 256
	 * @param piFeatureB Template 2 with length 256
	 * @return
	 */
	public native int MatchTemplate( byte[] piFeatureA, byte[] piFeatureB);

	/**
	 * method for converting the template into private standard
	 * @param input template need to convert
	 * @param output output template byte array.
	 */
	public void ToStd(byte[] input,byte[] output){
		switch(Conversions.getInstance().GetDataType(input)){
			case 1:{
				System.arraycopy(input, 0, output, 0, 256);
			}
			break;
			case 2:{
				Conversions.getInstance().IsoToStd(1,input,output);
			}
			break;
			case 3:{
				Conversions.getInstance().IsoToStd(2,input,output);
			}
			break;
		}
	}

	/**
	 * Method for matching the two templates in any standard format.
	 * the method will check the format of template and then do the match process.
	 * @param piFeatureA template 1 for match
	 * @param piFeatureB template 2 for match
	 * @return the match score
	 */
	public int MatchFingerData(byte[] piFeatureA, byte[] piFeatureB){
		int at= Conversions.getInstance().GetDataType(piFeatureA);
		int bt= Conversions.getInstance().GetDataType(piFeatureB);
		if((at==1)&&(bt==1)){
			return MatchTemplate(piFeatureA,piFeatureB);
		}else{
			byte adat[]=new byte[512];
			byte bdat[]=new byte[512];
			if(at==1){
				System.arraycopy(piFeatureA, 0, adat, 0, 256);
			}else{
				byte tmp[]=new byte[512];
				ToStd(piFeatureA,tmp);
				Conversions.getInstance().StdChangeCoord(tmp, 256, adat, 1);
			}
			if(bt==1){
				System.arraycopy(piFeatureB, 0, bdat, 0, 256);
			}else{
				byte tmp[]=new byte[512];
				ToStd(piFeatureB,tmp);
				Conversions.getInstance().StdChangeCoord(tmp, 256, bdat, 1);
			}
			return MatchTemplate(adat,bdat);
		}
	}


	static {
		System.loadLibrary("fgtitalg");
		System.loadLibrary("fpcore");
	}
}
