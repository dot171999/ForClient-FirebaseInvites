package in.altilogic.prayogeek;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class RemoteButtonScreen implements Parcelable {
    private String mScreenVersion = null;
    private String mScreenOrientation = null;
    private int mScreenStatus = 0;
    private List<RemoteButton> mButtons;


    public static final Creator<RemoteButtonScreen> CREATOR = new Creator<RemoteButtonScreen>() {
        @Override
        public RemoteButtonScreen createFromParcel(Parcel in) {
            return new RemoteButtonScreen(in);
        }

        @Override
        public RemoteButtonScreen[] newArray(int size) {
            return new RemoteButtonScreen[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int f) {
        parcel.writeString(mScreenVersion);
        parcel.writeString(mScreenOrientation);
        parcel.writeInt(mScreenStatus);
        List<String> bttns = new ArrayList<>();
        for(int i=0; i< mButtons.size(); i++)
            bttns.add(mButtons.get(i).toString());
        parcel.writeStringList(bttns);
    }

    RemoteButtonScreen(Parcel parcel){
        this.mScreenVersion = parcel.readString();
        this.mScreenOrientation = parcel.readString();
        this.mScreenStatus = parcel.readInt();

        List<String> bttns = new ArrayList<>();
        parcel.readStringList(bttns);
        if(bttns != null && bttns.size() > 0) {

        }
    }

    private RemoteButtonScreen(Parcelable parcelable){}

    public RemoteButtonScreen(List<String> buttons) {
        if(buttons != null && buttons.size() > 0){
            mButtons = new ArrayList<>();
            for(int i=0; i<buttons.size(); i++){
                mButtons.add(new RemoteButton(buttons.get(i), i+1) );
            }
        }
    }

    public void setVersion(String version){
        mScreenVersion = version;
    }

    public String getVersion() {
        return mScreenVersion;
    }

    public String getOrientation() {
        return mScreenOrientation;
    }

    public void setOrientation(String orientation) {
        mScreenOrientation = orientation;
    }

    public void setStatus(int status) {
        mScreenStatus = status;
    }

    public int getStatus() {
        return mScreenStatus;
    }

    public RemoteButton getButton(int id){
        for(RemoteButton butt : mButtons) {
            if(id == butt.getId())
                return butt;
        }
        return null;
    }

    public class RemoteButton {
        private String mName;
        private String mLinkName;
        private int mId;

        RemoteButton(String name, int id){
            mName = name;
            mId = id;
        }

        public void setId(int id){
            mId = id;
        }

        public int getId(){
            return mId;
        }

        public void setName(String name) {
            mName = name;
        }

        public void setLinkName(String name) {
            mLinkName = name;
        }

        public String getName() {
            return mName;
        }

        public String getLinkName() {
            return mLinkName;
        }

        public String toString() {
            return mName + "," + mLinkName + ',' + mId;
        }
    }
}
