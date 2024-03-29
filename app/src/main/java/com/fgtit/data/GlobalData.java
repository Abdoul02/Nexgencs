package com.fgtit.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.fgtit.fpcore.FPMatch;
import com.fgtit.utils.ExtApi;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Base64;

public class GlobalData {
	
	private static GlobalData instance;
	private Context pcontext=null;
	
	public List<UserItem> userList=null;
	public List<RecordItem> recordList=null;
	
	public List<WorkItem> workList=null;
	public List<LineItem> lineList=null;
	public List<DeptItem> deptList=null;
	
	private String	sDir=Environment.getExternalStorageDirectory() + "/OnePass";
	private List<Uri> fsList;
	
	public boolean glocal=false;
	public double glat=0.0;
	public double glng=0.0;
	
	public String	UpdateAddr;
	public String	UpdateUrl;
	public String	WebAddr;
	public String	WebService;	
	public boolean	isonline=false;
	public String 	DefaultUser;
	
	public String	AdminFingerprint="";
	public String	AdminPassword="1010";
	
	public static GlobalData getInstance() {
    	if(null == instance) {
    		instance = new GlobalData();
    	}
    	return instance;
    }
	
	public void SetContext(Context pc){
		pcontext=pc;
	}
	
	public boolean IsHaveSdCard() {
    	String status = Environment.getExternalStorageState();
    	if (status.equals(Environment.MEDIA_MOUNTED)) 	{
    	   return true;
    	}else{
    	   return false;
    	}
    }
    
	public String GetDir(){
		return sDir;
	}
	
    public void CreateDir() {
    	if(IsHaveSdCard()){    		
    		File destDir = new File(sDir);
    		if (!destDir.exists()) {
    			destDir.mkdirs();
    		}
    	}else{
    	}
    	
    	RecordFile.CreateFile(sDir+"/recordslist.dat");
    }
    
    public void LoadFileList() {
    	File file = new File(sDir);

    	fsList = new ArrayList<Uri>();
        if(file.isDirectory()) {
        	File[] all_file = file.listFiles();
            if (all_file != null) {
            	for(int i=0;i<all_file.length;i++){
            		fsList.add(Uri.parse(all_file[i].toString()));
            	}
             }
         } 
    }
    
    public boolean FileIsExists(Uri fsname) {
    	for(int i=0;i<fsList.size();i++)
    	{
    		if(fsList.get(i).equals(fsname))
    			return true;
    	}
    	return false;
    }
    
    public boolean IsFileExists(String filename){
		File f=new File(filename);
		if(f.exists()){
			return true;
		}
		return false;
	}
	
	public void DeleteFile(String filename){
		File f=new File(filename);
		if(f.exists()){
			f.delete();
		} 
	}
	

  	public void SaveConfig(){
      	SharedPreferences sp;
  		sp = PreferenceManager.getDefaultSharedPreferences(pcontext);
  		Editor edit=sp.edit();
  		
  		WebService=WebAddr+"BioWebApp/BioWebService.asmx";
  		UpdateUrl=UpdateAddr+"BioWebApp/apk/update.xml";
  		
  		edit.putString("WebAddr",WebAddr);
  		edit.putString("UpdateAddr",UpdateAddr);
  		edit.putString("DefaultUser",DefaultUser);
  		edit.putBoolean("IsOnline", isonline);
  		
  		edit.commit();

      }
      
  	//��ȡ����
      public void LoadConfig(){
      	SharedPreferences sp;
  		sp = PreferenceManager.getDefaultSharedPreferences(pcontext);

  		WebAddr=sp.getString("WebAddr","http://120.24.250.83/");		
  		UpdateAddr=sp.getString("UpdateAddr","http://120.24.250.83/");  		
  		WebService=WebAddr+"BioWebApp/BioWebService.asmx";
  		UpdateUrl=UpdateAddr+"BioWebApp/apk/update.xml";
  		
  		DefaultUser=sp.getString("DefaultUser","admin");
  		isonline=sp.getBoolean("IsOnline", false);
      }
      
    public void LoadWorkList(){
    	String filename=GlobalData.getInstance().GetDir()+"/work.xml";
    	if(GlobalData.getInstance().IsFileExists(filename)){
    		workList=XmlParase.paraseWorkItemList(XmlParase.ReadXmlFile(filename));
   	 	}
    }
    
    public void LoadLineList(){
    	String filename=GlobalData.getInstance().GetDir()+"/line.xml";
    	if(GlobalData.getInstance().IsFileExists(filename)){
    		lineList=XmlParase.paraseLineItemList(XmlParase.ReadXmlFile(filename));
   	 	}
    }
    
    public void LoadDeptList(){
    	String filename=GlobalData.getInstance().GetDir()+"/dept.xml";
    	if(GlobalData.getInstance().IsFileExists(filename)){
    		deptList=XmlParase.paraseDeptItemList(XmlParase.ReadXmlFile(filename));
   	 	}
    }
        
    public void LoadUsersList() {
    	userList = new ArrayList<UserItem>();
    	if(FileIsExists(Uri.parse(sDir+"/userslist.xml")))
    	{
    		List<UserItem> useritems=XmlParase.paraseUserItemList(XmlParase.ReadXmlFile(sDir+"/userslist.xml"));
			for(int i=0;i<useritems.size();i++)
			{
				userList.add(useritems.get(i));
			}
			//Toast.makeText(MainActivity.this, "Count:"+String.valueOf(personList.size()), Toast.LENGTH_SHORT).show();
    	}    	
    }
    
    public boolean IsHaveUserItem(String id){
    	for(int i=0;i<userList.size();i++){
    		if(userList.get(i).id.equals(id))
    			return true;
    	}
    	return false;
    }
    
    public UserItem FindUserItemByCard(String card){
    	for(int i=0;i<userList.size();i++){
    		if(userList.get(i).cardsn.equals(card))
    			return userList.get(i);
    	}
    	return null;
    }
    
    public UserItem FindUserItemByID(String id){
    	for(int i=0;i<userList.size();i++){
    		if(userList.get(i).id.equals(id))
    			return userList.get(i);
    	}
    	return null;
    }
    
    public UserItem FindUserItemByFp(byte[] fpdat){
    	for(int i=0;i<userList.size();i++){
    		if(userList.get(i).template1.length()>512){
				byte[] ref=android.util.Base64.decode(userList.get(i).template1,Base64.DEFAULT);
				if(FPMatch.getInstance().MatchTemplate(fpdat, ref)>60){
					return userList.get(i);						
				}
			}
			if(userList.get(i).template2.length()>512){
				byte[] ref=android.util.Base64.decode(userList.get(i).template2,Base64.DEFAULT);
				if(FPMatch.getInstance().MatchTemplate(fpdat, ref)>60){
					return userList.get(i);
				}
			}
    	}
    	return null;
    }
    
    
    public void SaveUsersList() {
    	//XmlParase.UserItemToXml(userList);
    	XmlParase.WriteXmlFile(XmlParase.UserItemToXml(userList), sDir+"/userslist.xml", "utf-8");
    }
 
    ///
    public void LoadRecordsList(){
    	recordList=RecordFile.ReadFromFile(sDir+"/recordslist.dat");
    }
    
    public void AppendRecord(RecordItem rs){
    	RecordFile.AppendToFile(sDir+"/recordslist.dat",rs);
    }
    
    public void AppendLocalRecord(UserItem person,int type){
    	RecordItem rs=new RecordItem();
    	rs.id=person.id;
		rs.name=person.name;
		rs.datetime=ExtApi.getStringDate();
		if(glocal){
			rs.lat=String.valueOf(glat);
			rs.lng=String.valueOf(glng);
		}
		rs.type=String.valueOf(type);
		rs.worktype=person.worktype;
		rs.linetype=person.linetype;
		rs.depttype=person.depttype;
    	RecordFile.AppendToFile(sDir+"/recordslist.dat",rs);
    }
    
    public RecordItem AppendRemoteRecord(UserItem ui,int type){
    	RecordItem rs=new RecordItem();
    	rs.id=ui.id;
		rs.name=ui.name;
		rs.worktype=ui.worktype;
		rs.linetype=ui.linetype;
		rs.depttype=ui.depttype;
		rs.datetime=ExtApi.getStringDate();
		if(glocal){
			rs.lat=String.valueOf(glat);
			rs.lng=String.valueOf(glng);
		}
		rs.type=String.valueOf(type);
		return rs;
    }
    
    public void ClearRecordsList(){
    	recordList.clear();
    	RecordFile.ReCreate(sDir+"/recordslist.dat");
    }
}
