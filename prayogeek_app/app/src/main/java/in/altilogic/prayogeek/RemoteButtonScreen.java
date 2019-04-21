package in.altilogic.prayogeek;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Button;

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

    private RemoteButtonScreen(Parcel parcel){
        this.mScreenVersion = parcel.readString();
        this.mScreenOrientation = parcel.readString();
        this.mScreenStatus = parcel.readInt();

        List<String> bttns = new ArrayList<>();
        parcel.readStringList(bttns);
        if(bttns.size() > 0) {
            this.mButtons = new ArrayList<>();
            for (String remoteButton: bttns) {
                String[] parts = remoteButton.split(",");
                if(parts.length == 5) {
                    RemoteButton rb = new RemoteButton(parts[0], Integer.getInteger(parts[4]));
                    rb.setCollection(parts[1]);
                    rb.setDocument(parts[2]);
                    rb.setField(parts[3]);
                    mButtons.add(rb);
                }
            }
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

    public class RemoteButton {
        private String mName;
        private String mCollection;
        private String mDocument;
        private String mField;
        private int mId;
        private Button mButton;
        private String mType;

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

        public void setType(String type){
            mType = type;
        }

        public String getType(){
            return mType;
        }

        public void setName(String name) {
            mName = name;
        }

        public void setCollection(String name) {
            mCollection = name;
        }

        public void setDocument(String document) {
            mDocument = document;
        }

        public String getDocument() {
            return mDocument;
        }

        public void setField(String field) {
            mField = field;
        }

        public String getField() {
            return mField;
        }


        public String getName() {
            return mName;
        }

        public String getCollection() {
            return mCollection;
        }

        public String toString() {
            return mName + "," + mCollection + ','+ mDocument + ',' + mField + ','+ mId;
        }

        public Button getButton() {
            return mButton;
        }

        public void setButton(Button button) {
            mButton = button;
            mButton.setId(mId);
            mButton.setText(mName);
        }
    }
}
