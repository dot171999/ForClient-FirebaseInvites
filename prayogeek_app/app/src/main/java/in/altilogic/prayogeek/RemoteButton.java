package in.altilogic.prayogeek;

import android.widget.Button;

import java.util.Map;

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

    RemoteButton(String params) {
        String[] parts = params.split(",");
        if(parts.length == 6) {
            mName = parts[0];
            mCollection = parts[1];
            mDocument = parts[2];
            mField = parts[3];
            try{
                mId = Integer.getInteger(parts[4]);
            }catch (Exception e) {
                e.printStackTrace();
            }
            mType = parts[5];
        }
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
        return mName + "," + mCollection + ','+ mDocument + ',' + mField + ','+ mId + "," + mType;
    }

    public Button getButton() {
        return mButton;
    }

    public void setButton(Button button) {
        mButton = button;
        mButton.setId(mId);
        mButton.setText(mName);
    }

    public void setParameters(Map<String, Object> dataMap){
        if(dataMap != null) {
            String collection = (String) dataMap.get((Object) "collection");
            String document = (String) dataMap.get((Object) "document");
            String field = (String) dataMap.get((Object) "field");
            String type = (String) dataMap.get((Object) "type");
            setCollection(collection);
            setDocument(document);
            setField(field);
            setType(type);
        }
    }
}
