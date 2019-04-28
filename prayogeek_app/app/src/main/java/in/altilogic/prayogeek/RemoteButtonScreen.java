package in.altilogic.prayogeek;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class RemoteButtonScreen implements Parcelable {
    private String mScreenName = null;
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

    private RemoteButtonScreen(Parcel parcel){
        this.mScreenVersion = parcel.readString();
        this.mScreenOrientation = parcel.readString();
        this.mScreenStatus = parcel.readInt();

        List<String> bttns = new ArrayList<>();
        parcel.readStringList(bttns);
        if(bttns.size() > 0) {
            this.mButtons = new ArrayList<>();
            for (String remoteButtonParams: bttns) {
                RemoteButton rb = new RemoteButton(remoteButtonParams);
                mButtons.add(rb);
            }
        }
    }

    private RemoteButtonScreen(Parcelable parcelable){}

    public RemoteButtonScreen(String screenName, List<String> buttons) {
        mScreenName = screenName;
        if(buttons != null && buttons.size() > 0){
            mButtons = new ArrayList<>(buttons.size());
            for(int i=0; i<buttons.size(); i++){
                mButtons.add(new RemoteButton(buttons.get(i), i+1) );
            }
        }
    }

    public String getScreenName() {
        return mScreenName;
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

    public RemoteButton getRemoteButton(int id){
        for(RemoteButton butt : mButtons) {
            if(id == butt.getId())
                return butt;
        }
        return null;
    }

    public RemoteButton getRemoteButton(String buttname){
        for(RemoteButton butt : mButtons) {
            if(buttname.equals(butt.getName()))
                return butt;
        }
        return null;
    }

    public int buttonsSize() {
        return mButtons.size();
    };

    public RemoteButtonScreen(String params){
        String[] parts = params.split(",");
        if(parts.length >= 4){
            mScreenName = parts[0];
            mScreenVersion = parts[1];
            mScreenOrientation = parts[2];
            try{
                int status = Integer.parseInt(parts[3]);
                mScreenStatus = status;
            }catch (Exception e) {
                e.printStackTrace();
                mScreenStatus = 0;
            }

            if(mButtons != null)
                mButtons.clear();

            mButtons = new ArrayList<>();
            for(int i=4; i<parts.length; i+=6){
                RemoteButton rb = new RemoteButton(parts[i], i-3);
                if(parts.length > i+5){
                    rb.setCollection(parts[i+1]);
                    rb.setDocument(parts[i+2]);
                    rb.setField(parts[i+3]);
                    int id = 0;
                    try{
                        id = Integer.parseInt(parts[i+4]);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    rb.setId(id);
                    rb.setType(parts[i+5]);
                }
                mButtons.add(rb);
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mScreenName).append(",")
                .append(mScreenVersion).append(",")
                .append(mScreenOrientation).append(",")
                .append(mScreenStatus).append(",");

        if(mButtons != null) {
            for (int i=0;i<mButtons.size()-1; i++ ){
                sb.append( mButtons.get(i).toString());
                sb.append(",");
            }
            if(mButtons.size() > 0) {
                int i = mButtons.size()-1;
                sb.append(mButtons.get(i).toString());
            }
        }

        return sb.toString();
    }
}
