package com.fgtit.fingermap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import com.fgtit.models.AdminData;
import com.fgtit.models.SessionManager;
import com.fgtit.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class UserList extends AppCompatActivity {

    DBHandler myDB = new DBHandler(this);
    private MyAppAdapter myAppAdapter;
    ListAdapter adapter;
    private ArrayList<User> userList;
    Cursor cs;
    ListView list1;
    SessionManager session;
    AdminData adminData;
    int compID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);


        this.getSupportActionBar().setBackgroundDrawable(new
                ColorDrawable(Color.parseColor("#020969")));

        session = new SessionManager(getApplicationContext());
        HashMap<String, String> manager = session.getUserDetails();
        compID = Integer.parseInt(manager.get(SessionManager.KEY_COMPID));

        adminData = new AdminData(this);

        // Get User records from SQLite DB
        userList = myDB.getAllUsers();
        if (userList.size() != 0) {
        // Set the User Array list in ListView
       /* adapter = new SimpleAdapter(UserList.this, userList, R.layout.user_entry, new String[]{
                "userId", "name"}, new int[]{R.id.empId, R.id.nam}); */

         list1 = findViewById(R.id.uList);
            myAppAdapter=new MyAppAdapter(userList,UserList.this);
        list1.setAdapter(myAppAdapter);

            Toast.makeText(getApplicationContext(), userList.size() +" employees",Toast.LENGTH_SHORT).show();
    }

        else{

            Toast.makeText(getApplicationContext(),"SQLite is empty",Toast.LENGTH_SHORT).show();
        }

  list1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

          TextView c = view.findViewById(R.id.empId);
          TextView Uid = view.findViewById(R.id.userId);
          String idnum = c.getText().toString();
          String userId = Uid.getText().toString();

      /*    if(compID == 3 || compID == 8){

              Bundle dataBundle = new Bundle();
              dataBundle.putString("uid", userId);
              Intent intent = new Intent(getApplicationContext(), EnrollActivity.class);
              intent.putExtras(dataBundle);
              startActivity(intent);
          }else{

              passwordDialog(userId);
          }*/

      if(adminData.isLocked()){
          passwordDialog(userId);
      }else{

          Bundle dataBundle = new Bundle();
          dataBundle.putString("uid", userId);
          Intent intent = new Intent(getApplicationContext(), EnrollActivity.class);
          intent.putExtras(dataBundle);
          startActivity(intent);
      }

      }
  });
    }

    public class MyAppAdapter extends BaseAdapter {

        public class ViewHolder {
            TextView txtTitle,txtSubTitle,txtUserId;


        }

        public List<User> parkingList;

        public Context context;
        ArrayList<User> arraylist;

        private MyAppAdapter(List<User> apps, Context context) {
            this.parkingList = apps;
            this.context = context;
            arraylist = new ArrayList<User>();
            arraylist.addAll(parkingList);

        }

        @Override
        public int getCount() {
            return parkingList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {


            View rowView = convertView;
            ViewHolder viewHolder;

            if (rowView == null) {
                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(R.layout.user_entry, null);
                // configure view holder
                viewHolder = new ViewHolder();
                viewHolder.txtTitle = rowView.findViewById(R.id.nam);
                viewHolder.txtSubTitle = rowView.findViewById(R.id.empId);
                viewHolder.txtUserId = rowView.findViewById(R.id.userId);
                rowView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

                viewHolder.txtTitle.setText(parkingList.get(position).getuName() + "");
                viewHolder.txtSubTitle.setText(parkingList.get(position).getIdNum() + "");
                viewHolder.txtUserId.setText(parkingList.get(position).getuId() + "");
            //Toast.makeText(getApplicationContext(),"Position error",Toast.LENGTH_SHORT).show();
            return rowView;


        }

       public void filter(String charText) {

            charText = charText.toLowerCase(Locale.getDefault());

            parkingList.clear();
            if (charText.length() == 0) {
                parkingList.addAll(arraylist);

            } else {
                for (User us : arraylist) {
                    if (charText.length() != 0 && us.getuName().toLowerCase(Locale.getDefault()).contains(charText)) {
                        parkingList.add(us);
                    }

                    else if (charText.length() != 0 && us.getIdNum().toLowerCase(Locale.getDefault()).contains(charText)) {
                        parkingList.add(us);
                    }
                }
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.userslist, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        //*** setOnQueryTextFocusChangeListener ***
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                myAppAdapter.filter(searchQuery.trim());
                list1.invalidate();
                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Do something when collapsed
                return true;  // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Do something when expanded
                return true;  // Return true to expand action view
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {

            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void passwordDialog(final String uid){

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.client_name,null);
        dialogBuilder.setView(dialogView);
        HashMap<String, String> dataManager = adminData.getDataDetails();
       final EditText edtClient = dialogView.findViewById(R.id.edtClientName);
        edtClient.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
       final String defaultPassword;
       if(dataManager.get(AdminData.PASSWORD) != null){
           defaultPassword = dataManager.get(AdminData.PASSWORD);
       }else{
           defaultPassword = "Admin2018";

       }
       //final String password = dataManager.get(AdminData.PASSWORD);//"Admin2018";


        dialogBuilder.setTitle("Password");
        dialogBuilder.setMessage("Enter password below");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                if(edtClient.getText().length() > 0 ){

                    String userInput = edtClient.getText().toString();
                    if(userInput.equals(defaultPassword)){

                        Bundle dataBundle = new Bundle();
                        dataBundle.putString("uid", uid);
                        Intent intent = new Intent(getApplicationContext(), EnrollActivity.class);
                        intent.putExtras(dataBundle);
                        startActivity(intent);
                    }else{
                        Toast.makeText(getApplicationContext(),"Incorrect password",Toast.LENGTH_SHORT).show();
                    }


                }else{

                    Toast.makeText(getApplicationContext(),"Please enter password to proceed",Toast.LENGTH_SHORT).show();
                }

            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

}
