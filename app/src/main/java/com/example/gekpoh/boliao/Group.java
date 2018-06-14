package com.example.gekpoh.boliao;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Group implements Parcelable{
    public static final SimpleDateFormat groupDateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private String groupid;//mGroupId is to indicate the reference to the database for this group. groupid is also used as chatid
    private String name, placename, placeid, photourl, description;
    private long startdate, enddate;//timestamp
    private int currentsize, maxsize;

    public Group(){

    }

    //Constructor for creating new Activity
    public Group(String groupid, String name, String placename, String placeid, String photourl, String description ,Long startdate, Long enddate, int maxsize) {
        this.groupid = groupid;
        this.name = name;
        this.placename = placename;
        this.placeid = placeid;
        this.photourl = photourl;
        this.description = description;
        this.startdate = startdate;
        this.enddate = enddate;
        this.currentsize = 1;
        this.maxsize = maxsize;
    }
    private Group(Parcel in) {
        name = in.readString();
        placename = in.readString();
        placeid = in.readString();
        groupid = in.readString();
        photourl = in.readString();
        description = in.readString();
        startdate = in.readLong();
        enddate = in.readLong();
        currentsize = in.readInt();
        maxsize = in.readInt();
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    public String getName(){
        return name;
    }
    public String getPlaceId(){
        return placeid;
    }
    public String getPlaceName(){ return placename; }//Might need to use google api next time
    public String getDescription(){ return description; }
    public String getPhotoUrl() {
        return photourl;
    }
    //start date and end date are number of seconds since jan 1 1970
    public Long getStartDate(){
        return startdate;
    }
    public Long getEndDate(){ return enddate; }
    public int getMaxSize(){ return maxsize; }
    public int getCurrentSize(){ return currentsize; }
    public String getGroupId(){
        return groupid;
    }

    public static void getGroupfromId(final String groupid, final ArrayList<Group> groupList){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("groups");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Need to decide how to obtain the data from database, I dont think the code below works
                //Group group = dataSnapshot.child(groupid).getValue(Group.class);
                //group.setGroupId(groupid);
                //groupList.add(group);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(placename);
        dest.writeString(placeid);
        dest.writeString(groupid);
        dest.writeString(photourl);
        dest.writeString(description);
        dest.writeLong(startdate);//Converting it to long is better than writeSerializable
        dest.writeLong(enddate);
        dest.writeInt(currentsize);
        dest.writeInt(maxsize);
    }
}